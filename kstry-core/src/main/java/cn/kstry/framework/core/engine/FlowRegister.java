/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
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

import cn.kstry.framework.core.bpmn.*;
import cn.kstry.framework.core.bus.ContextStoryBus;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.component.hook.AsyncFlowHook;
import cn.kstry.framework.core.component.strategy.PeekStrategy;
import cn.kstry.framework.core.component.strategy.PeekStrategyRepository;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.future.AdminFuture;
import cn.kstry.framework.core.engine.thread.InvokeMethodThreadLocal;
import cn.kstry.framework.core.enums.ElementAllowNextEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.monitor.SerializeTracking;
import cn.kstry.framework.core.monitor.TrackingStack;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementPropertyUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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
     * 锁
     */
    private ReentrantLock reentrantLock = new ReentrantLock();

    /**
     * 用来保存 并行网关或者包含网关 与 入度的对应关系。
     * - 遇到 并行网关或者包含网关 时，保存对应关系。K：网关  V：网关入度列表
     * - 网关入度到来且执行成功，Set<FlowElement> 移除当前入度。直到Set成为空集，即可继续执行
     */
    private ConcurrentHashMap<FlowElement, List<ContextStoryBus.ElementArriveRecord>> joinGatewayComingMap = new ConcurrentHashMap<>();

    /**
     * 链路追踪器
     */
    private MonitorTracking monitorTracking;

    /**
     * TaskFuture 管理类
     */
    private AdminFuture adminFuture;

    /**
     * 流程开始事件的ID
     */
    private String startEventId;

    /**
     * 主任务开始事件的ID
     */
    private String storyId;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 中途执行流程的开始元素
     */
    private FlowElement midwayStartElement;

    /**
     * 循环次数
     */
    private long cycleTimes;

    private FlowRegister() {

    }

    public FlowRegister(ApplicationContext applicationContext, StartEvent startEvent, StoryRequest<?> storyRequest, SerializeTracking serializeTracking) {
        // init start event
        startElement = startEvent;
        startEventId = startEvent.getId();
        storyId = startEvent.getId();
        businessId = storyRequest.getBusinessId();
        cycleTimes = 1L;

        // init requestId
        requestId = GlobalUtil.getOrSetRequestId(storyRequest);

        // init monitor tracking
        monitorTracking = new MonitorTracking(applicationContext, startEvent, storyRequest.getTrackingType(), serializeTracking);

        // init stack
        flowElementStack = monitorTracking.newTrackingStack();

        FlowElement start = startEvent;
        String midwayStartId = storyRequest.getMidwayStartId();
        if (StringUtils.isNotBlank(midwayStartId)) {
            Optional<FlowElement> midwayStartElementOptional = startEvent.getMidwayStartElement(midwayStartId);
            AssertUtil.isTrue(midwayStartElementOptional.isPresent(), ExceptionEnum.PARAMS_ERROR,
                    "There is no node element in the process that matches midwayStartId. startEventId: {}, midwayStartId: {}", startEventId, midwayStartId);
            midwayStartElement = midwayStartElementOptional.orElse(null);
            start = midwayStartElement;
        }
        flowElementStack.push(null, start);
    }

    public FlowRegister asyncFlowRegister(FlowElement startFlowElement) {
        AssertUtil.isTrue(startFlowElement instanceof SequenceFlow);
        FlowRegister asyncFlowRegister = new FlowRegister();
        asyncFlowRegister.startElement = startFlowElement;
        asyncFlowRegister.startEventId = this.startEventId;
        asyncFlowRegister.storyId = this.storyId;
        asyncFlowRegister.monitorTracking = this.monitorTracking;
        asyncFlowRegister.reentrantLock = this.reentrantLock;
        asyncFlowRegister.joinGatewayComingMap = this.joinGatewayComingMap;
        asyncFlowRegister.requestId = this.requestId;
        asyncFlowRegister.cycleTimes = this.cycleTimes;
        asyncFlowRegister.businessId = this.businessId;
        asyncFlowRegister.adminFuture = this.adminFuture;

        // init stack
        asyncFlowRegister.flowElementStack = asyncFlowRegister.monitorTracking.newTrackingStack();

        //  push start node to stack
        asyncFlowRegister.flowElementStack.push(null, startFlowElement);
        return asyncFlowRegister;
    }

    public FlowRegister cloneSubFlowRegister(StartEvent startEvent) {
        AssertUtil.notNull(startEvent);
        FlowRegister subFlowRegister = new FlowRegister();
        subFlowRegister.startElement = startEvent;
        subFlowRegister.startEventId = startEvent.getId();
        subFlowRegister.storyId = storyId;
        subFlowRegister.cycleTimes = 1L;
        subFlowRegister.monitorTracking = this.monitorTracking;
        subFlowRegister.requestId = this.requestId;
        subFlowRegister.businessId = this.businessId;
        subFlowRegister.adminFuture = this.adminFuture;
        subFlowRegister.flowElementStack = this.monitorTracking.newTrackingStack();
        subFlowRegister.flowElementStack.push(null, startEvent);
        return subFlowRegister;
    }

    public MonitorTracking getMonitorTracking() {
        AssertUtil.notNull(monitorTracking);
        return monitorTracking;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public AdminFuture getAdminFuture() {
        return adminFuture;
    }

    public long getCycleTimes() {
        return cycleTimes;
    }

    public FlowElement getStartElement() {
        return startElement;
    }

    public void setAdminFuture(AdminFuture adminFuture) {
        AssertUtil.isNull(this.adminFuture);
        this.adminFuture = adminFuture;
    }

    public String getStoryId() {
        return storyId;
    }

    public String getStartEventId() {
        return startEventId;
    }

    public Optional<FlowElement> nextElement(ContextStoryBus contextStoryBus) {
        StoryBus storyBus = contextStoryBus.getStoryBus();
        return monitorTracking.trackingNextElement(storyBus.getScopeDataOperator(), doNextElement(contextStoryBus).orElse(null));
    }

    private Optional<FlowElement> doNextElement(ContextStoryBus contextStoryBus) {
        Optional<FlowElement> elementOptional = flowElementStack.pop();
        if (!elementOptional.isPresent()) {
            return Optional.empty();
        }

        // 获取当前执行节点
        String taskName = GlobalUtil.getTaskName(getStartElement(), getRequestId());
        FlowElement currentFlowElement = elementOptional.get();
        AssertUtil.notTrue(adminFuture.isCancelled(startEventId), ExceptionEnum.ASYNC_TASK_INTERRUPTED,
                "Task interrupted. Story task was interrupted! taskName: {}, identity: {}", taskName, currentFlowElement.identity());
        AssertUtil.isTrue(cycleTimes == 1L || cycleTimes <= GlobalProperties.KSTRY_STORY_MAX_CYCLE_COUNT, ExceptionEnum.CYCLE_TIMES_OVER_LIMIT,
                "{} cycleTimes: {}, taskName: {}, identity: {}", ExceptionEnum.CYCLE_TIMES_OVER_LIMIT.getDesc(), cycleTimes, taskName, currentFlowElement.identity());

        // 处理回环流程
        if (currentFlowElement instanceof SequenceFlow) {
            FlowElement cycleBeginElement = ((SequenceFlow) currentFlowElement).getCycleBeginElement();
            if (cycleBeginElement != null) {
                joinGatewayComingMap.keySet().forEach(k -> {
                    if (k instanceof EndEvent) {
                        return;
                    }
                    joinGatewayComingMap.remove(k);
                });
                flowElementStack.clear();
                cycleTimes++;
                monitorTracking.buildFirstNodeTracking(cycleTimes, cycleBeginElement);
                return Optional.of(cycleBeginElement);
            }
        }

        // 获取执行决策
        monitorTracking.buildFirstNodeTracking(cycleTimes, currentFlowElement);
        PeekStrategy peekStrategy = getPeekStrategy(currentFlowElement);

        // 填充 ContextStoryBus
        contextStoryBus.setPrevElement(prevElement);
        contextStoryBus.setCycleTimes(cycleTimes);
        contextStoryBus.setReentrantLock(reentrantLock);
        contextStoryBus.setJoinGatewayComingMap(joinGatewayComingMap);
        contextStoryBus.setEndTaskPedometer(adminFuture.getEndTaskPedometer(startEventId));

        // 是否跳过当前节点继续执行下一个
        if (midwayStartElement != currentFlowElement && peekStrategy.skip(currentFlowElement, contextStoryBus)) {
            prevElement = currentFlowElement;
            return doNextElement(new ContextStoryBus(contextStoryBus.getStoryBus()));
        }
        return elementOptional;
    }

    public Optional<AsyncFlowHook<List<FlowElement>>> predictNextElement(ContextStoryBus contextStoryBus, FlowElement currentFlowElement) {
        if (contextStoryBus.getEndTaskPedometer() == null) {
            contextStoryBus.setPrevElement(prevElement);
            contextStoryBus.setCycleTimes(cycleTimes);
            contextStoryBus.setReentrantLock(reentrantLock);
            contextStoryBus.setJoinGatewayComingMap(joinGatewayComingMap);
            contextStoryBus.setEndTaskPedometer(adminFuture.getEndTaskPedometer(startEventId));
        }

        // 匹配可参与执行的子分支
        List<FlowElement> flowList;
        PeekStrategy peekStrategy = getPeekStrategy(currentFlowElement);
        if (currentFlowElement instanceof SequenceFlow) {
            flowList = currentFlowElement.outingList();
        } else {
            flowList = contextStoryBus.getStoryBus().getScopeDataOperator().serialRead(opt -> {
                List<SequenceFlow> sequenceFlowList = currentFlowElement.outingList().stream().map(f -> (SequenceFlow) f).collect(Collectors.toList());
                OrderComparator.sort(sequenceFlowList);
                try {
                    InvokeMethodThreadLocal.setCycleTimes(cycleTimes);
                    return sequenceFlowList.stream().filter(flow -> peekStrategy.needPeek(monitorTracking, flow, contextStoryBus)).map(f -> (FlowElement) f).collect(Collectors.toList());
                } finally {
                    InvokeMethodThreadLocal.clearCycleTimes();
                }
            }).orElse(Lists.newArrayList());
        }
        if (!peekStrategy.allowOutingEmpty(currentFlowElement)) {
            AssertUtil.isTrue(CollectionUtils.isNotEmpty(flowList), ExceptionEnum.STORY_FLOW_ERROR,
                    "Match to the next process node as empty! current node identity: {}, desired list of possible nodes for later execution: {}", () -> {
                        List<String> identityList = currentFlowElement.outingList().stream().map(flow -> flow.outingList().get(0)).map(FlowElement::identity).collect(Collectors.toList());
                        return Lists.newArrayList(currentFlowElement.identity(), String.join(", ", identityList));
                    });
        }

        // 开启异步流程
        if (ElementPropertyUtil.needGatewayOpenAsync(currentFlowElement) && CollectionUtils.size(flowList) > 1) {
            monitorTracking.trackingSequenceFlow(flowList);
            AsyncFlowHook<List<FlowElement>> asyncElementHook = new AsyncFlowHook<>(flowList);
            asyncElementHook.hook(list -> {
                prevElement = currentFlowElement;
                if (!Objects.equals(flowList.size(), currentFlowElement.outingList().size())) {
                    processNotMatchElement(contextStoryBus, flowList, currentFlowElement);
                }
            });
            return Optional.of(asyncElementHook);
        }

        prevElement = currentFlowElement;
        flowElementStack.pushList(currentFlowElement, flowList);

        // 无需执行的子流程，可能会参与驱动之后的流程
        if (!Objects.equals(flowList.size(), currentFlowElement.outingList().size())) {
            processNotMatchElement(contextStoryBus, flowList, currentFlowElement);
        }
        return Optional.empty();
    }

    private PeekStrategy getPeekStrategy(FlowElement currentFlowElement) {
        Optional<PeekStrategy> peekStrategyOptional = PeekStrategyRepository.getPeekStrategy().stream().filter(peekStrategy -> peekStrategy.match(currentFlowElement)).findFirst();
        return peekStrategyOptional.orElseThrow(() -> ExceptionUtil.buildException(null, ExceptionEnum.CONFIGURATION_UNSUPPORTED_ELEMENT, null));
    }

    private void processNotMatchElement(ContextStoryBus contextStoryBus, List<FlowElement> flowList, FlowElement element) {
        List<FlowElement> notNeedPeekList = Lists.newArrayList(element.outingList());
        notNeedPeekList.removeAll(flowList);
        notNeedPeekList.forEach(notNeedPeekElement -> {
            SequenceFlow notNeedPeekSequenceFlow = GlobalUtil.transferNotEmpty(notNeedPeekElement, SequenceFlow.class);
            notNeedPeekSequenceFlow.getEndElementList().forEach(endElement -> {
                List<FlowElement> comingList = endElement.comingList().stream().filter(e -> e.getFlowTrack().contains(
                        notNeedPeekSequenceFlow.getIndex()) || Objects.equals(e.getIndex(), notNeedPeekSequenceFlow.getIndex())).collect(Collectors.toList());
                if (endElement instanceof ParallelGateway && ((ParallelGateway) endElement).isStrictMode() && CollectionUtils.isNotEmpty(comingList)) {
                    throw ExceptionUtil.buildException(null, ExceptionEnum.STORY_FLOW_ERROR, GlobalUtil.format(
                            "A process branch that cannot reach the ParallelGateway appears! sequence flow: {}, parallel gateway: {}", notNeedPeekSequenceFlow.identity(), endElement.identity()));
                }
                comingList.forEach(coming -> {
                    ElementAllowNextEnum allowNextEnum = PeekStrategyRepository.allowDoNext(endElement, coming, contextStoryBus, false);
                    if (allowNextEnum == ElementAllowNextEnum.ALLOW_NEX) {
                        AssertUtil.isTrue(coming instanceof SequenceFlow);
                        flowElementStack.push(coming.comingList().get(0), coming);
                        LOGGER.debug("The last incoming degree is executed, opening the next event flow! event: {}, coming: {}", endElement.identity(), coming.identity());
                    } else if (allowNextEnum == ElementAllowNextEnum.NOT_ALLOW_NEX_NEED_COMPENSATE) {
                        processNotMatchElement(contextStoryBus, Lists.newArrayList(), endElement);
                    }
                });
            });
        });
    }
}
