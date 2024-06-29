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
package cn.kstry.framework.core.monitor;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.SequenceFlow;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.component.event.TrackingBeginEvent;
import cn.kstry.framework.core.component.event.TrackingFinishEvent;
import cn.kstry.framework.core.component.expression.Expression;
import cn.kstry.framework.core.component.limiter.RateLimiterConfig;
import cn.kstry.framework.core.component.limiter.strategy.FailAcquireStrategy;
import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.engine.FlowRegister;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 链路追踪器
 *
 * @author lykan
 */
public class MonitorTracking {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorTracking.class);

    public static final String BAD_VALUE = "bad value!";

    public static final String BAD_TARGET = "bad target!";

    /**
     * 链路追踪类型
     */
    private final TrackingTypeEnum trackingTypeEnum;

    /**
     * 保存 node tracking
     */
    private final Map<String, NodeTracking> nodeTrackingMap = Maps.newConcurrentMap();

    /**
     * 节点计步器
     */
    private final AtomicInteger invokePedometer = new AtomicInteger(0);

    private final LocalDateTime startTime;

    private final FlowElement startEvent;

    private final SerializeTracking serializeTracking;

    private final ApplicationContext applicationContext;

    public MonitorTracking(ApplicationContext applicationContext, FlowElement startEvent, TrackingTypeEnum trackingTypeEnum, SerializeTracking serializeTracking) {
        AssertUtil.notNull(applicationContext);
        this.startTime = LocalDateTime.now();
        this.startEvent = startEvent;
        this.serializeTracking = serializeTracking;
        this.applicationContext = applicationContext;
        this.trackingTypeEnum = Optional.ofNullable(trackingTypeEnum).orElse(TrackingTypeEnum.of(GlobalProperties.STORY_MONITOR_TRACKING_TYPE));
    }

    /**
     * 创建一个带节点监控的栈结构
     *
     * @return 带节点监控的栈结构
     */
    public TrackingStack newTrackingStack() {
        return new TrackingStack(this);
    }

    /**
     * 构建监控节点
     *
     * @param prevElement 被监控的节点
     * @return 监控节点
     */
    public Optional<NodeTracking> buildNodeTracking(FlowElement prevElement) {
        if (prevElement == null || trackingTypeEnum.isNone()) {
            return Optional.empty();
        }
        return Optional.of(nodeTrackingMap.computeIfAbsent(prevElement.getId(), key -> {
            NodeTracking nt = new NodeTracking();
            nt.setNodeId(prevElement.getId());
            nt.setStartTime(LocalDateTime.now());
            nt.setNodeName(prevElement.getName());
            nt.setNodeType(prevElement.getElementType());
            return nt;
        }));
    }

    public void buildFirstNodeTracking(FlowElement prevElement) {
        NodeTracking oldTracking = nodeTrackingMap.remove(prevElement.getId());
        if (!(prevElement instanceof ServiceTask)) {
            buildNodeTracking(prevElement);
            return;
        }
        buildNodeTracking(prevElement).ifPresent(nodeTracking -> {
            if (oldTracking == null) {
                return;
            }
            CycleTracking ct = oldTracking.getCycleTracking() == null ? new CycleTracking(1L, oldTracking.getSpendTime()) : oldTracking.getCycleTracking();
            ct.setTimes(ct.getTimes() + 1);
            nodeTracking.setCycleTracking(ct);
        });
    }

    /**
     * 对 NextElement 进行监控
     *
     * @param flowElement NextElement
     * @return NextElement
     */
    public Optional<FlowElement> trackingNextElement(ScopeDataOperator scopeDataOperator, FlowElement flowElement) {
        buildNodeTracking(flowElement).ifPresent(nodeTracking -> {
            nodeTracking.setIndex(invokePedometer.incrementAndGet());
            applicationContext.publishEvent(buildTrackingBeginEvent(scopeDataOperator, nodeTracking));
        });
        return Optional.ofNullable(flowElement);
    }

    /**
     * 对 SequenceFlow List 进行监控
     *
     * @param flowList flowList
     */
    public void trackingSequenceFlow(List<FlowElement> flowList) {
        if (CollectionUtils.isEmpty(flowList) || trackingTypeEnum.isNone()) {
            return;
        }
        flowList.stream().map(f -> GlobalUtil.transferNotEmpty(f, SequenceFlow.class)).forEach(flow -> {
            AssertUtil.oneSize(flow.comingList());
            FlowElement flowElement = flow.comingList().get(0);
            buildNodeTracking(flowElement).ifPresent(t -> t.addToNodeId(flow.getId()));
        });
    }

    /**
     * 对 TaskService 入参进行监控
     *
     * @param flowElement           flowElement
     * @param paramTrackingSupplier paramTrackingSupplier
     */
    public void trackingNodeParams(FlowElement flowElement, Supplier<ParamTracking> paramTrackingSupplier) {
        AssertUtil.notNull(flowElement);
        getServiceNodeTracking(flowElement).filter(t -> trackingTypeEnum.needServiceDetailTracking()).ifPresent(tracking -> {
            ParamTracking paramTracking = paramTrackingSupplier.get();
            if (paramTracking == null) {
                return;
            }
            paramTracking.valueSerialize(serializeTracking);
            tracking.addParamTracking(paramTracking);
        });
    }

    /**
     * 对执行结果对 StoryBus 的通知进行监控
     *
     * @param flowElement            flowElement
     * @param noticeTrackingSupplier noticeTrackingSupplier
     */
    public void trackingNodeNotice(FlowElement flowElement, Supplier<NoticeTracking> noticeTrackingSupplier) {
        AssertUtil.notNull(flowElement);
        getServiceNodeTracking(flowElement).filter(t -> trackingTypeEnum.needServiceDetailTracking()).ifPresent(tracking -> {
            NoticeTracking noticeTracking = noticeTrackingSupplier.get();
            if (noticeTracking == null) {
                return;
            }
            noticeTracking.valueSerialize(serializeTracking);
            tracking.addNoticeTracking(noticeTracking);
        });
    }

    public void trackingResult(FlowElement flowElement, Object result) {
        if (result == null) {
            return;
        }
        getServiceNodeTracking(flowElement).ifPresent(tracking -> tracking.setTaskResult(result));
    }

    /**
     * TaskService 执行完成后监控记录数据
     *
     * @param flowElement flowElement
     * @param exception   exception
     */
    public void finishTaskTracking(ScopeDataOperator scopeDataOperator, FlowElement flowElement, Throwable exception) {
        getServiceNodeTracking(flowElement).ifPresent(tracking -> {
            tracking.setEndTime(LocalDateTime.now());
            tracking.setTaskException(exception);
            tracking.setSpendTime(Duration.between(tracking.getStartTime(), tracking.getEndTime()).toMillis());
            applicationContext.publishEvent(buildTrackingFinishEvent(scopeDataOperator, tracking, exception));
        });
    }

    public void confirmFinishTaskTracking(FlowRegister register, FlowElement flowElement, Throwable exception) {
        getServiceNodeTracking(flowElement).ifPresent(tracking -> {
            if (tracking.finishService()) {
                return;
            }
            tracking.setEndTime(LocalDateTime.now());
            tracking.setTaskException(exception);
            tracking.setSpendTime(Duration.between(tracking.getStartTime(), tracking.getEndTime()).toMillis());

            String startId = register.getStoryId();
            String businessId = register.getBusinessId();
            String requestId = register.getRequestId();
            applicationContext.publishEvent(new TrackingFinishEvent(startId, businessId, requestId, tracking, exception));
        });
    }

    public void expressionTracking(ScopeDataOperator scopeDataOperator, FlowElement flowElement, boolean res) {
        if (!(flowElement instanceof Expression)) {
            return;
        }
        buildNodeTracking(flowElement).ifPresent(tracking -> {
            ((Expression) flowElement).getConditionExpression().ifPresent(c -> tracking.setExpression(new ExpressionTracking(c.getPlainExpression(), String.valueOf(res))));
            if (flowElement instanceof SequenceFlow) {
                applicationContext.publishEvent(buildTrackingFinishEvent(scopeDataOperator, tracking, null));
            }
        });
    }

    public void limiterTracking(ServiceTask serviceTask, RateLimiterConfig rateLimiterConfig, FailAcquireStrategy failAcquireStrategy) {
        getServiceNodeTracking(serviceTask).ifPresent(tracking -> {
            LimiterTracking limiterTracking = new LimiterTracking();
            limiterTracking.setLimited(true);
            tracking.setLimiter(limiterTracking);
            if (rateLimiterConfig == null) {
                return;
            }
            limiterTracking.setLimiterName(rateLimiterConfig.getName());
            limiterTracking.setAcquireTimeout(rateLimiterConfig.getAcquireTimeout());
            limiterTracking.setFailStrategy(Optional.ofNullable(failAcquireStrategy).map(FailAcquireStrategy::name).orElse(rateLimiterConfig.getFailStrategy()));
            limiterTracking.setMaxPermits(rateLimiterConfig.getPermits());
            if (StringUtils.isNotBlank(rateLimiterConfig.getExpression())) {
                limiterTracking.setExpression(new ExpressionTracking(rateLimiterConfig.getExpression(), "true"));
            }
        });
    }

    public void demotionTaskTracking(FlowElement flowElement, DemotionInfo demotionInfo) {
        getServiceNodeTracking(flowElement).ifPresent(tracking -> tracking.setDemotionInfo(demotionInfo));
    }

    public void timeoutTaskTracking(FlowElement flowElement, Integer timeout) {
        getServiceNodeTracking(flowElement).ifPresent(tracking -> tracking.setTimeout(timeout));
    }

    public void iterateCountTracking(FlowElement flowElement, int count, int stride) {
        if (count <= 0) {
            return;
        }
        getServiceNodeTracking(flowElement).ifPresent(tracking -> {
            IterateInfo iterateInfo = new IterateInfo();
            iterateInfo.setIterateCount(count);
            iterateInfo.setIterateStride(stride);
            tracking.setIterateInfo(iterateInfo);
        });
    }

    public Optional<NodeTracking> getServiceNodeTracking(FlowElement flowElement) {
        if (flowElement == null || trackingTypeEnum.notNeedServiceTracking()) {
            return Optional.empty();
        }
        NodeTracking nodeTracking = nodeTrackingMap.get(flowElement.getId());
        return Optional.ofNullable(nodeTracking);
    }

    public List<NodeTracking> getStoryTracking() {
        if (trackingTypeEnum.isNone()) {
            return Lists.newArrayList();
        }

        if (trackingTypeEnum.isServiceTracking()) {
            nodeTrackingMap.values().stream().filter(nodeTracking -> nodeTracking.getNodeType() == BpmnTypeEnum.SERVICE_TASK)
                    .forEach(serviceTracking -> {
                        if (CollectionUtils.isEmpty(serviceTracking.getToNodeIds())) {
                            return;
                        }
                        Set<String> idSet = new HashSet<>();
                        InStack<String> toNodeIdStack = new BasicInStack<>();
                        toNodeIdStack.pushCollection(serviceTracking.getToNodeIds());
                        while (!toNodeIdStack.isEmpty()) {
                            String id = toNodeIdStack.pop().orElseThrow(() -> ExceptionUtil.buildException(null, ExceptionEnum.SYSTEM_ERROR, null));
                            NodeTracking nodeTracking = nodeTrackingMap.get(id);
                            if (nodeTracking == null) {
                                continue;
                            }
                            if (nodeTracking.getNodeType() == BpmnTypeEnum.SERVICE_TASK) {
                                idSet.add(nodeTracking.getNodeId());
                                continue;
                            }
                            toNodeIdStack.pushCollection(nodeTracking.getToNodeIds());
                        }
                        serviceTracking.refreshToNodeIds(idSet);
                    });
        }
        return nodeTrackingMap.values().stream().peek(nodeTracking -> {
                    if (trackingTypeEnum.notNeedServiceTracking() || nodeTracking.finishService()) {
                        return;
                    }
                    nodeTracking.setEndTime(LocalDateTime.now());
                    nodeTracking.setSpendTime(Duration.between(nodeTracking.getStartTime(), nodeTracking.getEndTime()).toMillis());
                })
                .filter(nodeTracking -> nodeTracking.getIndex() != null)
                .filter(nodeTracking -> !trackingTypeEnum.isServiceTracking() || nodeTracking.getNodeType() == BpmnTypeEnum.SERVICE_TASK)
                .sorted(Comparator.comparing(NodeTracking::getIndex))
                .collect(Collectors.toList());
    }

    public Map<String, NodeTracking> getRawStoryTracking() {
        return nodeTrackingMap;
    }

    public List<NodeTracking> getFlowExpressionTracking() {
        return nodeTrackingMap.values().stream()
                .filter(n -> n.getNodeType() == BpmnTypeEnum.SEQUENCE_FLOW && n.getExpression() != null)
                .sorted(Comparator.comparing(NodeTracking::getIndex)).collect(Collectors.toList());
    }

    public long getSpendTime() {
        return Duration.between(startTime, LocalDateTime.now()).toMillis();
    }

    public void trackingLog() {
        if (!GlobalProperties.KSTRY_STORY_TRACKING_LOG) {
            return;
        }
        List<NodeTracking> storyTracking = getStoryTracking();
        if (CollectionUtils.isNotEmpty(storyTracking)) {
            LOGGER.info("[{}] startId: {}, spend {}ms: {}",
                    ExceptionEnum.STORY_TRACKING_CODE.getExceptionCode(), startEvent.getId(), getSpendTime(), JSON.toJSONString(storyTracking, SerializerFeature.DisableCircularReferenceDetect));
        }
    }

    private TrackingBeginEvent buildTrackingBeginEvent(ScopeDataOperator scopeDataOperator, NodeTracking nodeTracking) {
        String startId = scopeDataOperator.getStartId();
        String businessId = scopeDataOperator.getBusinessId().orElse(null);
        String requestId = scopeDataOperator.getRequestId();
        return new TrackingBeginEvent(startId, businessId, requestId, nodeTracking);
    }

    private TrackingFinishEvent buildTrackingFinishEvent(ScopeDataOperator scopeDataOperator, NodeTracking nodeTracking, Throwable exception) {
        String startId = scopeDataOperator.getStartId();
        String businessId = scopeDataOperator.getBusinessId().orElse(null);
        String requestId = scopeDataOperator.getRequestId();
        return new TrackingFinishEvent(startId, businessId, requestId, nodeTracking, exception);
    }
}
