/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.engine;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.ParallelGateway;
import cn.kstry.framework.core.bpmn.SequenceFlow;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bus.ContextStoryBus;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.component.hook.FlowElementHook;
import cn.kstry.framework.core.component.strategy.PeekStrategy;
import cn.kstry.framework.core.component.strategy.PeekStrategyRepository;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.monitor.TrackingStack;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementPropertyUtil;
import cn.kstry.framework.core.util.GlobalUtil;

/**
 * 流程寄存器
 *
 * @author lykan
 */
public class FlowRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowRegister.class);

    /**
     * 请求 ID
     */
    private String requestId;

    /**
     * FlowRegister 开始节点
     */
    private FlowElement startElement;

    /**
     * 上一个执行的 FlowElement
     */
    private FlowElement prevElement;

    /**
     * 执行栈
     */
    private TrackingStack flowElementStack;

    /**
     * 用来保存 并行网关或者包含网关 与 入度的对应关系。
     *     - 遇到 并行网关或者包含网关 时，保存对应关系。K：网关  V：网关入度列表
     *     - 网关入度到来且执行成功，Set<FlowElement> 移除当前入度。直到Set成为空集，即可继续执行
     */
    private ConcurrentHashMap<FlowElement, List<FlowElement>> joinGatewayComingMap = new ConcurrentHashMap<>();

    /**
     * 流程阻塞器
     */
    private AsyncTaskCell asyncTaskCell;

    /**
     * 链路追踪器
     */
    private MonitorTracking monitorTracking;

    /**
     * 任务节点在执行中
     */
    private boolean taskServiceDoing = false;

    private FlowRegister() {
    }

    public FlowRegister(FlowElement startEvent, StoryRequest<?> storyRequest) {
        AssertUtil.isTrue(startEvent instanceof StartEvent);

        // init start event
        startElement = startEvent;

        // init requestId
        requestId = GlobalUtil.getOrSetRequestId(storyRequest);

        // init monitor tracking
        monitorTracking = new MonitorTracking(startEvent, storyRequest.getTrackingType());

        // init stack
        flowElementStack = monitorTracking.newTrackingStack();
        flowElementStack.push(null, startEvent);

        // init asyncTaskCell
        asyncTaskCell = new AsyncTaskCell((StartEvent) startElement);
    }

    public FlowRegister asyncFlowRegister(FlowElement startFlowElement) {
        AssertUtil.isTrue(startFlowElement instanceof SequenceFlow);
        FlowRegister asyncFlowRegister = new FlowRegister();
        asyncFlowRegister.startElement = startFlowElement;
        asyncFlowRegister.monitorTracking = this.monitorTracking;
        asyncFlowRegister.joinGatewayComingMap = this.joinGatewayComingMap;
        asyncFlowRegister.asyncTaskCell = this.asyncTaskCell;
        asyncFlowRegister.requestId = this.requestId;

        // init stack
        asyncFlowRegister.flowElementStack = asyncFlowRegister.monitorTracking.newTrackingStack();

        //  push start node to stack
        asyncFlowRegister.flowElementStack.push(null, startFlowElement);
        return asyncFlowRegister;
    }

    public FlowElement getStartFlowElement() {
        AssertUtil.notNull(this.startElement);
        return this.startElement;
    }

    public Optional<FlowElement> nextElement(StoryBus storyBus) {
        Optional<FlowElement> flowElementOptional = doNextElement(storyBus);
        if (validServiceTask(flowElementOptional.orElse(null))) {
            return nextElement(storyBus);
        }
        return monitorTracking.trackingNextElement(flowElementOptional.orElse(null));
    }

    public AsyncTaskCell getAsyncTaskCell() {
        AssertUtil.notNull(asyncTaskCell);
        return asyncTaskCell;
    }

    public MonitorTracking getMonitorTracking() {
        AssertUtil.notNull(monitorTracking);
        return monitorTracking;
    }

    public void addTaskFuture(Future<AsyncTaskState> taskFuture) {
        AssertUtil.notNull(taskFuture);
        if (asyncTaskCell.isCancelled()) {
            LOGGER.info("[{}] Task interrupted. Story task was cancelled! startId: {}", ExceptionEnum.TASK_CANCELLED.getExceptionCode(), getStartFlowElement().getId());
            return;
        }
        asyncTaskCell.addTaskFuture(taskFuture);
    }

    public String getRequestId() {
        AssertUtil.notBlank(requestId);
        return requestId;
    }

    private Optional<FlowElement> doNextElement(StoryBus storyBus) {
        if (asyncTaskCell.isCancelled()) {
            LOGGER.info("[{}] Task interrupted. Story task was cancelled! startId: {}", ExceptionEnum.TASK_CANCELLED.getExceptionCode(), getStartFlowElement().getId());
            return Optional.empty();
        }

        AssertUtil.notNull(storyBus);
        Optional<FlowElement> peekElementOptional = flowElementStack.peek();
        if (validServiceTask(peekElementOptional.orElse(null))) {
            taskServiceDoing = true;
            return peekElementOptional;
        }
        taskServiceDoing = false;

        Optional<FlowElement> elementOptional = flowElementStack.pop();
        if (!elementOptional.isPresent()) {
            return Optional.empty();
        }

        // 获取当前执行节点
        FlowElement currentFlowElement = elementOptional.get();
        monitorTracking.buildNodeTracking(currentFlowElement);

        // 获取执行决策
        PeekStrategy peekStrategy = getPeekStrategy(currentFlowElement);

        // build context scope data
        ContextStoryBus contextScopeData = new ContextStoryBus(storyBus);
        contextScopeData.setPrevElement(prevElement);
        contextScopeData.setJoinGatewayComingMap(joinGatewayComingMap);
        contextScopeData.setAsyncTaskCell(getAsyncTaskCell());

        // 是否跳过当前节点继续执行下一个
        if (peekStrategy.skip(currentFlowElement, contextScopeData)) {
            prevElement = currentFlowElement;
            return nextElement(storyBus);
        }

        // 匹配可参与执行的子分支
        List<FlowElement> flowList = elementOptional.get()
                .outingList().stream().filter(flow -> peekStrategy.needPeek(flow, contextScopeData)).collect(Collectors.toList());
        if (!peekStrategy.allowOutingEmpty(currentFlowElement)) {
            AssertUtil.notEmpty(flowList, ExceptionEnum.STORY_FLOW_ERROR,
                    "Match to the next process node as empty! taskId: {}", currentFlowElement.getId());
        }

        // 开启异步流程
        if (ElementPropertyUtil.needOpenAsync(currentFlowElement)) {
            monitorTracking.trackingSequenceFlow(flowList);
            FlowElementHook<List<FlowElement>> asyncElementHook = new FlowElementHook<>(flowList);
            asyncElementHook.setId(currentFlowElement.getId());
            asyncElementHook.hook(list -> {
                prevElement = currentFlowElement;
                if (!Objects.equals(flowList.size(), elementOptional.get().outingList().size())) {
                    processNotMatchElement(contextScopeData, flowList, elementOptional.get());
                }
            });
            return Optional.of(asyncElementHook);
        }

        prevElement = currentFlowElement;
        flowElementStack.pushList(elementOptional.get(), flowList);

        // 无需执行的子流程，可能会参与驱动之后的流程
        if (!Objects.equals(flowList.size(), elementOptional.get().outingList().size())) {
            processNotMatchElement(contextScopeData, flowList, elementOptional.get());
        }
        return elementOptional;
    }

    private PeekStrategy getPeekStrategy(FlowElement currentFlowElement) {
        Optional<PeekStrategy> peekStrategyOptional =
                PeekStrategyRepository.getPeekStrategy().stream().filter(peekStrategy -> peekStrategy.match(currentFlowElement)).findFirst();
        return peekStrategyOptional.orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
    }

    private void processNotMatchElement(ContextStoryBus contextScopeData, List<FlowElement> flowList, FlowElement element) {
        List<FlowElement> notNeedPeekList = Lists.newArrayList(element.outingList());
        notNeedPeekList.removeAll(flowList);
        notNeedPeekList.forEach(notNeedPeekElement -> {
            SequenceFlow notNeedPeekSequenceFlow = GlobalUtil.transferNotEmpty(notNeedPeekElement, SequenceFlow.class);
            notNeedPeekSequenceFlow.getEndElementList().forEach(endElement -> {
                List<FlowElement> comingList = endElement.comingList().stream().filter(e ->
                        e.getFlowTrack().contains(notNeedPeekSequenceFlow.getIndex()) || Objects.equals(e.getIndex(), notNeedPeekSequenceFlow.getIndex())
                ).collect(Collectors.toList());
                if (endElement instanceof ParallelGateway && ((ParallelGateway) endElement).isStrictMode() && CollectionUtils.isNotEmpty(comingList)) {
                    KstryException.throwException(ExceptionEnum.STORY_FLOW_ERROR, GlobalUtil.format(
                            "A process branch that cannot reach the ParallelGateway appears! sequenceFlowId: {}", notNeedPeekSequenceFlow.getId()));
                }
                comingList.forEach(coming -> {
                    if (!PeekStrategyRepository.existExpectedComing(endElement, coming, contextScopeData)) {
                        AssertUtil.isTrue(coming instanceof SequenceFlow);
                        flowElementStack.push(coming.comingList().get(0), coming);
                        LOGGER.debug("The last incoming degree is executed, opening the next event flow! eventId: {}, comingId: {}",
                                endElement.getId(), coming.getId());
                    }
                });
            });
        });
    }

    private boolean validServiceTask(FlowElement flowElement) {
        if (flowElement == null) {
            return false;
        }
        if (!(flowElement instanceof ServiceTask)) {
            return false;
        }
        return !taskServiceDoing;
    }
}
