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
package cn.kstry.framework.core.component.bpmn;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.SequenceFlow;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.bpmn.impl.*;
import cn.kstry.framework.core.bus.InstructContent;
import cn.kstry.framework.core.component.bpmn.builder.InclusiveJoinPointBuilder;
import cn.kstry.framework.core.component.bpmn.builder.ParallelJoinPointBuilder;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessBuilder;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.joinpoint.InclusiveJoinPoint;
import cn.kstry.framework.core.component.bpmn.joinpoint.ParallelJoinPoint;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.bpmn.link.StartProcessLink;
import cn.kstry.framework.core.component.limiter.RateLimiterBuilder;
import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.constant.BpmnElementProperties;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Camunda 解析器实现
 *
 * @author lykan
 */
public class CamundaProcessModelTransfer implements ProcessModelTransfer<BpmnModelInstance> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamundaProcessModelTransfer.class);

    /**
     * Camunda 中定义的 ServiceTask、Task 类型常量
     */
    private final List<String> CAMUNDA_TASK_TYPE_LIST = Lists.newArrayList(BpmnModelConstants.BPMN_ELEMENT_SERVICE_TASK, BpmnModelConstants.BPMN_ELEMENT_TASK);

    @Override
    public Optional<ProcessLink> getProcessLink(ConfigResource config, BpmnModelInstance instance, String startId) {
        AssertUtil.notNull(config, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
        if (instance == null || StringUtils.isBlank(startId)) {
            return Optional.empty();
        }
        org.camunda.bpm.model.bpmn.instance.StartEvent camundaStartEvent = instance.getModelElementById(startId);
        if (camundaStartEvent == null || camundaStartEvent.getParentElement() == null || camundaStartEvent.getParentElement().getElementType() == null
                || Objects.equals(BpmnModelConstants.BPMN_ELEMENT_SUB_PROCESS, camundaStartEvent.getParentElement().getElementType().getTypeName())) {
            return Optional.empty();
        }
        List<org.camunda.bpm.model.bpmn.instance.Process> processes = Lists.newArrayList(instance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class));
        StartProcessLink processLink = StartProcessLink.build(camundaStartEvent.getId(), camundaStartEvent.getName(), processes.get(0).getId(), processes.get(0).getName());
        doGetStartProcessLink(config, camundaStartEvent, processLink);
        return Optional.of(processLink);
    }

    public List<SubProcessLink> getSeparatedSubProcessLinks(ConfigResource config, BpmnModelInstance instance) {
        AssertUtil.notNull(config, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
        if (instance == null) {
            return Lists.newArrayList();
        }
        Collection<org.camunda.bpm.model.bpmn.instance.SubProcess> subProcesses = instance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.SubProcess.class);
        if (CollectionUtils.isEmpty(subProcesses)) {
            return Lists.newArrayList();
        }

        Set<String> subProcessIdSet = Sets.newHashSet();
        List<SubProcessLink> subProcessLinks = Lists.newArrayList();
        subProcesses.forEach(sp -> {
            Collection<org.camunda.bpm.model.bpmn.instance.StartEvent> childStartEvent = sp.getChildElementsByType(org.camunda.bpm.model.bpmn.instance.StartEvent.class);
            AssertUtil.oneSize(childStartEvent, ExceptionEnum.CONFIGURATION_SUBPROCESS_ERROR,
                    "SubProcesses are only allowed to also have a start event. subProcessId: {}, fileName: {}", sp.getId(), config.getConfigName());
            org.camunda.bpm.model.bpmn.instance.StartEvent startEvent = childStartEvent.iterator().next();
            SubProcessLink subProcessLink = SubProcessLink.build(sp.getId(), sp.getName(), startEvent.getId(), startEvent.getName(), subBpmnLink -> {
                doGetStartProcessLink(config, startEvent, subBpmnLink);
                SubProcessImpl subProcess = subBpmnLink.getElement();
                subBpmnLink.getStartEvent().setConfig(subProcess.getConfig());
                subProcess.setStartEvent(subBpmnLink.getStartEvent());
            });
            fillIterableProperty(config, sp, subProcessLink::setElementIterable);
            ElementPropertyUtil.getNodeProperty(sp, BpmnElementProperties.TASK_TIMEOUT).map(s -> NumberUtils.toInt(s, -1)).filter(i -> i >= 0).ifPresent(subProcessLink::setTimeout);
            ElementPropertyUtil.getNodeProperty(sp, BpmnElementProperties.TASK_STRICT_MODE).map(BooleanUtils::toBooleanObject).filter(b -> !b).ifPresent(b -> subProcessLink.setStrictMode(false));
            ElementPropertyUtil.getNodeProperty(sp, BpmnElementProperties.EXPRESSION_NOT_SKIP_EXPRESSION).ifPresent(subProcessLink::setNotSkipExp);
            AssertUtil.notTrue(subProcessIdSet.contains(sp.getId()), ExceptionEnum.ELEMENT_DUPLICATION_ERROR,
                    "There are duplicate SubProcess ids defined! subProcessId: {}, fileName: {}", sp.getId(), config.getConfigName());
            subProcessLink.buildSubDiagramBpmnLink(config);
            subProcessLinks.add(subProcessLink);
            subProcessIdSet.add(sp.getId());
        });
        return subProcessLinks;
    }

    @Override
    public Optional<SubProcessLink> getSubProcessLink(ConfigResource config, BpmnModelInstance instance, String subProcessId) {
        AssertUtil.notNull(config, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
        if (instance == null) {
            return Optional.empty();
        }

        Collection<org.camunda.bpm.model.bpmn.instance.SubProcess> subProcesses = instance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.SubProcess.class);
        if (CollectionUtils.isEmpty(subProcesses)) {
            return Optional.empty();
        }
        List<SubProcess> subProcessList = subProcesses.stream().filter(subProcess -> Objects.equals(subProcess.getId(), subProcessId)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(subProcessList)) {
            return Optional.empty();
        }
        AssertUtil.oneSize(subProcessList, ExceptionEnum.ELEMENT_DUPLICATION_ERROR,
                "There are duplicate SubProcess ids defined! subProcessId: {}, fileName: {}", subProcessId, config.getConfigName());
        SubProcess sp = subProcessList.get(0);
        Collection<org.camunda.bpm.model.bpmn.instance.StartEvent> childStartEvent = sp.getChildElementsByType(org.camunda.bpm.model.bpmn.instance.StartEvent.class);
        AssertUtil.oneSize(childStartEvent, ExceptionEnum.CONFIGURATION_SUBPROCESS_ERROR,
                "SubProcesses are only allowed to also have a start event! subProcessId: {}, fileName: {}", sp.getId(), config.getConfigName());
        org.camunda.bpm.model.bpmn.instance.StartEvent startEvent = childStartEvent.iterator().next();

        SubProcessLink subProcessLink = SubProcessLink.build(sp.getId(), sp.getName(), startEvent.getId(), startEvent.getName(), subBpmnLink -> doGetStartProcessLink(config, startEvent, subBpmnLink));
        ElementPropertyUtil.getNodeProperty(sp, BpmnElementProperties.TASK_STRICT_MODE).map(BooleanUtils::toBooleanObject).filter(b -> !b).ifPresent(b -> subProcessLink.setStrictMode(false));
        ElementPropertyUtil.getNodeProperty(sp, BpmnElementProperties.TASK_TIMEOUT).map(s -> NumberUtils.toInt(s, -1)).filter(i -> i >= 0).ifPresent(subProcessLink::setTimeout);
        ElementPropertyUtil.getNodeProperty(sp, BpmnElementProperties.EXPRESSION_NOT_SKIP_EXPRESSION).ifPresent(subProcessLink::setNotSkipExp);
        fillIterableProperty(config, sp, subProcessLink::setElementIterable);
        subProcessLink.buildSubDiagramBpmnLink(config).getElement();
        return Optional.of(subProcessLink);
    }

    private void doGetStartProcessLink(ConfigResource config, org.camunda.bpm.model.bpmn.instance.StartEvent camundaStartEvent, StartProcessLink processLink) {
        if (camundaStartEvent == null) {
            return;
        }

        String processId = StringUtils.EMPTY;
        FlowElement eleProcess = processLink.getElement();
        if (eleProcess instanceof cn.kstry.framework.core.bpmn.SubProcess) {
            processId = eleProcess.getId();
        } else if (eleProcess instanceof StartEventImpl) {
            processId = ((StartEventImpl) processLink.getElement()).getProcessId();
        }

        Map<ProcessLink, ProcessLink> inclusiveProcessLinkMap = Maps.newHashMap();
        Map<org.camunda.bpm.model.bpmn.instance.FlowNode, InclusiveJoinPoint> joinAssistMap = Maps.newHashMap();

        Set<org.camunda.bpm.model.bpmn.instance.FlowNode> circularDependencyCheck = Sets.newHashSet();
        Map<org.camunda.bpm.model.bpmn.instance.FlowNode, ProcessLink> nextBpmnLinkMap = Maps.newHashMap();
        nextBpmnLinkMap.put(camundaStartEvent, processLink);

        Set<String> cycleCrossFlows = getCycleCrossFlows(camundaStartEvent);
        if (CollectionUtils.isNotEmpty(cycleCrossFlows)) {
            LOGGER.info("Automatic identification of cross points in loopback links. fileName: {}, processId: {}, points: {}", config.getConfigName(), processId, cycleCrossFlows);
            AssertUtil.isTrue(cycleCrossFlows.size() == 1, ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                    "Only one link loopback and one cross point is allowed in a process! fileName: {}, processId: {}, points: {}", config.getConfigName(), processId, cycleCrossFlows);
        }
        Map<String, CycleCrossPointLink> cycleCrossPointMap = Maps.newHashMap();
        Map<org.camunda.bpm.model.bpmn.instance.FlowNode, Integer> comingCountMap = Maps.newHashMap();
        InStack<org.camunda.bpm.model.bpmn.instance.FlowNode> basicInStack = new BasicInStack<>();
        basicInStack.push(camundaStartEvent);
        while (!basicInStack.isEmpty()) {
            org.camunda.bpm.model.bpmn.instance.FlowNode element = basicInStack.pop().orElseThrow(() -> ExceptionUtil.buildException(null, ExceptionEnum.SYSTEM_ERROR, null));
            if (element instanceof org.camunda.bpm.model.bpmn.instance.EndEvent) {
                continue;
            }

            ProcessLink beforeProcessLink = nextBpmnLinkMap.get(element);
            if (element instanceof org.camunda.bpm.model.bpmn.instance.InclusiveGateway) {
                beforeProcessLink = inclusiveProcessLinkMap.getOrDefault(beforeProcessLink, beforeProcessLink);
            }
            for (org.camunda.bpm.model.bpmn.instance.SequenceFlow seq : element.getOutgoing()) {
                if (isCycleCrossPoint(cycleCrossFlows, seq)) {
                    AssertUtil.isTrue(cycleCrossFlows.contains(seq.getId()));
                    SequenceFlowImpl sequenceFlow = sequenceFlowMapping(config, seq);
                    beforeProcessLink.getElement().outing(sequenceFlow);
                    CycleCrossPointLink cycleCrossPointLink = cycleCrossPointMap.get(seq.getId());
                    if (cycleCrossPointLink != null && cycleCrossPointLink.processLink != null) {
                        cycleCrossPointLink.sequenceFlow = sequenceFlow;
                        cycleCrossPointLink.processLink.byLinkCycle(sequenceFlow);
                    } else {
                        cycleCrossPointLink = new CycleCrossPointLink();
                        cycleCrossPointLink.sequenceFlow = sequenceFlow;
                        cycleCrossPointMap.put(seq.getId(), cycleCrossPointLink);
                    }
                    continue;
                }
                FlowNode targetNode = seq.getTarget();
                if (targetNode instanceof org.camunda.bpm.model.bpmn.instance.EndEvent) {
                    beforeProcessLink.end(sequenceFlowMapping(config, seq));
                } else if (targetNode instanceof org.camunda.bpm.model.bpmn.instance.ParallelGateway) {
                    ProcessLink parallelProcessLink = nextBpmnLinkMap.computeIfAbsent(targetNode, node -> {
                        ParallelJoinPointBuilder parallelJoinPointBuilder = processLink.parallel(targetNode.getId());
                        ElementPropertyUtil.getNodeProperty(targetNode,
                                BpmnElementProperties.ASYNC_ELEMENT_OPEN_ASYNC).map(BooleanUtils::toBoolean).filter(b -> b).ifPresent(b -> parallelJoinPointBuilder.openAsync());
                        ElementPropertyUtil.getNodeProperty(targetNode,
                                BpmnElementProperties.TASK_STRICT_MODE).map(BooleanUtils::toBooleanObject).filter(b -> !b).ifPresent(b -> parallelJoinPointBuilder.notStrictMode());
                        return parallelJoinPointBuilder.build();
                    });
                    linkCycleCrossPoint(cycleCrossFlows, cycleCrossPointMap, targetNode, parallelProcessLink);
                    ProcessLink nextProcessLink = beforeProcessLink.nextParallel(sequenceFlowMapping(config, seq), (ParallelJoinPoint) parallelProcessLink);
                    nextBpmnLinkMap.put(targetNode, nextProcessLink);
                } else if (targetNode instanceof org.camunda.bpm.model.bpmn.instance.InclusiveGateway) {
                    ProcessLink inclusiveProcessLink = nextBpmnLinkMap.computeIfAbsent(targetNode, node -> {
                        ServiceTaskImpl serviceTask = getServiceTask(node, config, true);
                        InclusiveJoinPoint beforeMockLink = processLink.inclusive(GlobalUtil.uuid(BpmnTypeEnum.INCLUSIVE_GATEWAY, targetNode.getId())).build();
                        ProcessLink nextMockLink = instructWrapper(config, true, targetNode, serviceTask, null, beforeMockLink, null, cycleCrossFlows);
                        InclusiveJoinPointBuilder inclusiveJoinPointBuilder = processLink.inclusive(targetNode.getId());
                        ElementPropertyUtil.getNodeProperty(targetNode,
                                BpmnElementProperties.ASYNC_ELEMENT_OPEN_ASYNC).map(BooleanUtils::toBoolean).filter(b -> b).ifPresent(b -> inclusiveJoinPointBuilder.openAsync());
                        ElementPropertyUtil.getNodeProperty(targetNode,
                                BpmnElementProperties.INCLUSIVE_GATEWAY_COMPLETED_COUNT).map(NumberUtils::toInt).ifPresent(inclusiveJoinPointBuilder::completedCount);
                        InclusiveJoinPoint actualInclusive = inclusiveJoinPointBuilder.build();
                        if (beforeMockLink == nextMockLink) {
                            linkCycleCrossPoint(cycleCrossFlows, cycleCrossPointMap, targetNode, actualInclusive);
                            ElementPropertyUtil.getNodeProperty(targetNode, BpmnElementProperties.INCLUSIVE_GATEWAY_MIDWAY_START_ID).ifPresent(inclusiveJoinPointBuilder::midwayStartId);
                            return actualInclusive;
                        }
                        linkCycleCrossPoint(cycleCrossFlows, cycleCrossPointMap, targetNode, beforeMockLink);
                        ElementPropertyUtil.getNodeProperty(targetNode, BpmnElementProperties.INCLUSIVE_GATEWAY_MIDWAY_START_ID).ifPresent(m -> ((InclusiveGatewayImpl) beforeMockLink.getElement()).setMidwayStartId(m));
                        nextMockLink.nextInclusive(buildSequenceFlow(targetNode.getId()), actualInclusive);
                        inclusiveProcessLinkMap.put(beforeMockLink, actualInclusive);
                        return beforeMockLink;
                    });
                    beforeProcessLink.nextInclusive(sequenceFlowMapping(config, seq), (InclusiveJoinPoint) inclusiveProcessLink);
                    nextBpmnLinkMap.put(targetNode, inclusiveProcessLink);
                } else if (targetNode instanceof org.camunda.bpm.model.bpmn.instance.ExclusiveGateway) {
                    MergeIncomingRes mergeRes = tryMergeIncoming(cycleCrossFlows, processLink, config, seq, targetNode, joinAssistMap, beforeProcessLink);
                    if (mergeRes.isSkip) {
                        continue;
                    }
                    SequenceFlow sf = mergeRes.sf;
                    ServiceTaskImpl serviceTask = getServiceTask(targetNode, config, true);
                    ProcessLink nextProcessLink = instructWrapper(config, true, targetNode, serviceTask, sf, mergeRes.processLink, cycleCrossPointMap, cycleCrossFlows);
                    if (nextProcessLink != mergeRes.processLink) {
                        sf = buildSequenceFlow(targetNode.getId());
                    }
                    nextProcessLink = nextProcessLink.nextExclusive(targetNode.getId(), sf).build();
                    nextBpmnLinkMap.put(targetNode, nextProcessLink);
                    basicInStack.push(targetNode);
                } else if (targetNode instanceof org.camunda.bpm.model.bpmn.instance.Task && CAMUNDA_TASK_TYPE_LIST.contains(targetNode.getElementType().getTypeName())) {
                    MergeIncomingRes mergeRes = tryMergeIncoming(cycleCrossFlows, processLink, config, seq, targetNode, joinAssistMap, beforeProcessLink);
                    if (mergeRes.isSkip) {
                        continue;
                    }
                    ServiceTaskImpl serviceTask = getServiceTask(targetNode, config, false);
                    ProcessLink nextProcessLink = instructWrapper(config, false, targetNode, serviceTask, mergeRes.sf, mergeRes.processLink, cycleCrossPointMap, cycleCrossFlows);
                    nextBpmnLinkMap.put(targetNode, nextProcessLink);
                    basicInStack.push(targetNode);
                } else if (targetNode instanceof org.camunda.bpm.model.bpmn.instance.SubProcess || targetNode instanceof org.camunda.bpm.model.bpmn.instance.CallActivity) {
                    MergeIncomingRes mergeRes = tryMergeIncoming(cycleCrossFlows, processLink, config, seq, targetNode, joinAssistMap, beforeProcessLink);
                    if (mergeRes.isSkip) {
                        continue;
                    }
                    String subProcessId = targetNode.getId();
                    if (targetNode instanceof org.camunda.bpm.model.bpmn.instance.CallActivity) {
                        subProcessId = ((org.camunda.bpm.model.bpmn.instance.CallActivity) targetNode).getCalledElement();
                    }
                    SubProcessBuilder subProcessBuilder = mergeRes.processLink.nextSubProcess(mergeRes.sf, subProcessId);
                    fillIterableProperty(config, targetNode, subProcessBuilder::iterable);

                    ElementPropertyUtil.getNodeProperty(targetNode,
                            BpmnElementProperties.TASK_STRICT_MODE).map(BooleanUtils::toBooleanObject).filter(b -> !b).ifPresent(b -> subProcessBuilder.notStrictMode());
                    ElementPropertyUtil.getNodeProperty(targetNode, BpmnElementProperties.TASK_TIMEOUT).map(s -> NumberUtils.toInt(s, -1)).filter(i -> i >= 0).ifPresent(subProcessBuilder::timeout);
                    ElementPropertyUtil.getNodeProperty(targetNode, BpmnElementProperties.EXPRESSION_NOT_SKIP_EXPRESSION).ifPresent(subProcessBuilder::notSkipExp);

                    ProcessLink subProcessLink = subProcessBuilder.build();
                    linkCycleCrossPoint(cycleCrossFlows, cycleCrossPointMap, targetNode, subProcessLink);
                    nextBpmnLinkMap.put(targetNode, subProcessLink);
                    basicInStack.push(targetNode);
                } else {
                    throw ExceptionUtil.buildException(null, ExceptionEnum.CONFIGURATION_UNSUPPORTED_ELEMENT, GlobalUtil.format("There is an error in the bpmn file! fileName: {}, processId: {}", config.getConfigName(), processId));
                }

                if (isBpmnSupportAggregation(targetNode)) {
                    comingCountMap.merge(targetNode, 1, Integer::sum);
                    if (Objects.equals(comingCountMap.get(targetNode), getIncoming(cycleCrossFlows, targetNode).size())) {
                        basicInStack.push(targetNode);
                    }
                } else {
                    AssertUtil.notTrue(circularDependencyCheck.contains(targetNode), ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                            "Duplicate calls between elements are not allowed! fileName: {}, processId: {}, elementId: {}, elementName: {}", config.getConfigName(), processId, targetNode.getId(), targetNode.getName());
                    circularDependencyCheck.add(targetNode);
                }
            }
        }
    }

    private Set<String> getCycleCrossFlows(StartEvent startEvent) {
        if (startEvent == null) {
            return Sets.newHashSet();
        }
        Set<String> oldFlowIds = Sets.newHashSet();
        List<org.camunda.bpm.model.bpmn.instance.FlowNode> flowNodeList = Lists.newArrayList();
        flowNodeList.add(startEvent);
        while (true) {
            List<FlowNode> needRemoveList = flowNodeList.stream()
                    .filter(n -> CollectionUtils.isEmpty(n.getIncoming()) || n.getIncoming().stream().allMatch(i -> oldFlowIds.contains(i.getId())))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(needRemoveList)) {
                needRemoveList.forEach(n -> {
                    flowNodeList.remove(n);
                    Collection<org.camunda.bpm.model.bpmn.instance.SequenceFlow> outgoing = n.getOutgoing();
                    oldFlowIds.addAll(outgoing.stream().map(org.camunda.bpm.model.bpmn.instance.SequenceFlow::getId).collect(Collectors.toList()));
                    outgoing.forEach(o -> flowNodeList.add(o.getTarget()));
                });
                continue;
            }
            return flowNodeList.stream().flatMap(r -> r.getIncoming().stream())
                    .map(org.camunda.bpm.model.bpmn.instance.SequenceFlow::getId).filter(id -> !oldFlowIds.contains(id)).collect(Collectors.toSet());
        }
    }

    private MergeIncomingRes tryMergeIncoming(Set<String> cycleCrossFlows, StartProcessLink processLink, ConfigResource config,
                                              org.camunda.bpm.model.bpmn.instance.SequenceFlow seq, FlowNode targetNode, Map<FlowNode, InclusiveJoinPoint> joinAssistMap, ProcessLink beforeProcessLink) {
        MergeIncomingRes res = new MergeIncomingRes();
        if (getIncoming(cycleCrossFlows, targetNode).size() <= 1) {
            res.isSkip = false;
            res.sf = sequenceFlowMapping(config, seq);
            res.processLink = beforeProcessLink;
            return res;
        }
        InclusiveJoinPoint mockInclusive = joinAssistMap.computeIfAbsent(targetNode, n -> processLink.inclusive().build());
        res.processLink = beforeProcessLink.nextInclusive(sequenceFlowMapping(config, seq), mockInclusive);
        res.isSkip = mockInclusive.getElement().comingList().size() < getIncoming(cycleCrossFlows, targetNode).size();
        res.sf = buildSequenceFlow(targetNode.getId());
        return res;
    }

    private boolean isBpmnSupportAggregation(org.camunda.bpm.model.bpmn.instance.FlowElement element) {
        return element instanceof org.camunda.bpm.model.bpmn.instance.EndEvent
                || element instanceof org.camunda.bpm.model.bpmn.instance.ParallelGateway
                || element instanceof org.camunda.bpm.model.bpmn.instance.InclusiveGateway;
    }

    private ServiceTaskImpl getServiceTask(FlowNode flowNode, ConfigResource config, boolean decorate) {
        ServiceTaskImpl serviceTaskImpl = new ServiceTaskImpl();
        serviceTaskImpl.setId(decorate ? GlobalUtil.uuid(BpmnTypeEnum.SERVICE_TASK, flowNode.getId()) : flowNode.getId());
        serviceTaskImpl.setName(flowNode.getName());
        AssertUtil.notBlank(serviceTaskImpl.getId(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "The bpmn element id attribute cannot be empty! fileName: {}", config.getConfigName());
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_TASK_COMPONENT).ifPresent(serviceTaskImpl::setTaskComponent);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_TASK_SERVICE).ifPresent(serviceTaskImpl::setTaskService);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_TASK_PROPERTY).ifPresent(serviceTaskImpl::setTaskProperty);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_DEMOTION).ifPresent(serviceTaskImpl::setTaskDemotion);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.EXPRESSION_NOT_SKIP_EXPRESSION).ifPresent(exp -> {
            SequenceFlowExpression sequenceFlowExpression = new SequenceFlowExpression(exp);
            sequenceFlowExpression.setId(GlobalUtil.uuid(BpmnTypeEnum.EXPRESSION));
            sequenceFlowExpression.setName(exp);
            serviceTaskImpl.setExpression(sequenceFlowExpression);
        });
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_TASK_PARAMS).ifPresent(serviceTaskImpl::setTaskParams);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_RETRY_TIMES).map(NumberUtils::toInt).filter(i -> i > 0).ifPresent(serviceTaskImpl::setRetryTimes);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_CUSTOM_ROLE).flatMap(CustomRoleInfo::buildCustomRole).ifPresent(serviceTaskImpl::setCustomRoleInfo);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.TASK_ALLOW_ABSENT).map(BooleanUtils::toBooleanObject).ifPresent(serviceTaskImpl::setAllowAbsent);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.TASK_STRICT_MODE).map(BooleanUtils::toBooleanObject).ifPresent(serviceTaskImpl::setStrictMode);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.TASK_TIMEOUT).map(s -> NumberUtils.toInt(s, -1)).filter(i -> i >= 0).ifPresent(serviceTaskImpl::setTimeout);
        fillRateLimiterProperty(flowNode, serviceTaskImpl);
        fillIterableProperty(config, flowNode, serviceTaskImpl::mergeElementIterable);
        return serviceTaskImpl;
    }

    private ProcessLink instructWrapper(ConfigResource config, boolean allowEmpty, FlowNode targetNode, ServiceTaskImpl serviceTask,
                                        SequenceFlow firstSequenceFlow, ProcessLink nextProcessLink, Map<String, CycleCrossPointLink> cycleCrossPointMap,
                                        Set<String> cycleCrossFlows) {
        boolean isLink = cycleCrossPointMap != null;
        ProcessLink before = nextProcessLink;
        ServiceTaskImpl sTask = getServiceTask(targetNode, config, true);
        List<InstructContent> beforeInstructContentList = getInstructContentList(targetNode, true);
        if (CollectionUtils.isNotEmpty(beforeInstructContentList)) {
            for (InstructContent instructContent : beforeInstructContentList) {
                SequenceFlow sf;
                if (firstSequenceFlow != null) {
                    sf = firstSequenceFlow;
                    firstSequenceFlow = null;
                } else {
                    sf = buildSequenceFlow(targetNode.getId());
                }
                String instruct = instructContent.getInstruct().substring(1);
                nextProcessLink = TaskServiceUtil.instructTaskBuilder(nextProcessLink.nextInstruct(sf, instruct, instructContent.getContent()), sTask)
                        .name(instruct + "@" + serviceTask.getName()).id(GlobalUtil.uuid(BpmnTypeEnum.SERVICE_TASK, "Instruct-" + targetNode.getId())).build();
                if (isLink) {
                    linkCycleCrossPoint(cycleCrossFlows, cycleCrossPointMap, targetNode, nextProcessLink);
                    isLink = false;
                }
            }
        }

        if (serviceTask.validTask()) {
            SequenceFlow sf;
            if (firstSequenceFlow != null) {
                sf = firstSequenceFlow;
                firstSequenceFlow = null;
            } else {
                sf = buildSequenceFlow(targetNode.getId());
            }
            nextProcessLink = nextProcessLink.nextTask(sf, serviceTask);
            if (isLink) {
                linkCycleCrossPoint(cycleCrossFlows, cycleCrossPointMap, targetNode, nextProcessLink);
                isLink = false;
            }
        }

        List<InstructContent> afterInstructContentList = getInstructContentList(targetNode, false);
        if (CollectionUtils.isNotEmpty(afterInstructContentList)) {
            for (InstructContent instructContent : afterInstructContentList) {
                SequenceFlow sf;
                if (firstSequenceFlow != null) {
                    sf = firstSequenceFlow;
                    firstSequenceFlow = null;
                } else {
                    sf = buildSequenceFlow(targetNode.getId());
                }
                nextProcessLink = TaskServiceUtil.instructTaskBuilder(nextProcessLink.nextInstruct(sf, instructContent.getInstruct(), instructContent.getContent()), sTask)
                        .name(serviceTask.getName() + "@" + instructContent.getInstruct()).id(GlobalUtil.uuid(BpmnTypeEnum.SERVICE_TASK, "Instruct-" + targetNode.getId())).build();
                if (isLink) {
                    linkCycleCrossPoint(cycleCrossFlows, cycleCrossPointMap, targetNode, nextProcessLink);
                    isLink = false;
                }
            }
        }
        AssertUtil.isTrue(allowEmpty || before != nextProcessLink,
                ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "Invalid serviceNode definition, please add the necessary attributes! elementId: {}", targetNode.getId());
        if (isLink) {
            linkCycleCrossPoint(cycleCrossFlows, cycleCrossPointMap, targetNode, nextProcessLink);
        }
        return nextProcessLink;
    }

    private List<InstructContent> getInstructContentList(FlowNode flowNode, boolean isBefore) {
        List<Pair<String, String>> instructPairList =
                ElementPropertyUtil.getNodeProperty(flowNode, (isBefore ? "^" : StringUtils.EMPTY) + BpmnElementProperties.SERVICE_TASK_TASK_INSTRUCT, true, false);
        if (CollectionUtils.isEmpty(instructPairList)) {
            return Lists.newArrayList();
        }
        return instructPairList.stream().filter(instructPair -> StringUtils.isNotBlank(instructPair.getLeft()))
                .map(instructPair -> new InstructContent(Optional.of(instructPair.getLeft()).map(String::trim).orElse(null), instructPair.getRight())).collect(Collectors.toList());
    }

    private void fillRateLimiterProperty(FlowNode flowNode, ServiceTaskImpl serviceTaskImpl) {
        RateLimiterBuilder builder = new RateLimiterBuilder();
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_LIMIT_NAME).ifPresent(builder::name);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_LIMIT_PERMITS).map(NumberUtils::toDouble).ifPresent(builder::permits);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_LIMIT_WARMUP_PERIOD).map(NumberUtils::toInt).ifPresent(builder::warmupPeriod);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_LIMIT_ACQUIRE_TIMEOUT).map(NumberUtils::toInt).ifPresent(builder::acquireTimeout);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_LIMIT_FAIL_STRATEGY).ifPresent(builder::failStrategy);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_LIMIT_EXPRESSION).ifPresent(builder::expression);
        serviceTaskImpl.setRateLimiterConfig(builder.build());
    }

    private void fillIterableProperty(ConfigResource config, FlowNode flowNode, Consumer<BasicElementIterable> setConsumer) {
        BasicElementIterable elementIterable = new BasicElementIterable();
        Optional<String> iteSourceProperty = ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.ITERATE_SOURCE);
        if (iteSourceProperty.filter(s -> {
            if (ElementParserUtil.isValidDataExpression(s)) {
                return true;
            }
            LOGGER.warn("[{}] The set ite-source being iterated over is invalid. fileName: {}, calledElementId: {}", ExceptionEnum.BPMN_ATTRIBUTE_INVALID.getExceptionCode(), config.getConfigName(), flowNode.getId());
            return false;
        }).map(StringUtils::isNotBlank).orElse(false)) {
            elementIterable.setIteSource(iteSourceProperty.get());
        }
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.ITERATE_ASYNC).map(BooleanUtils::toBoolean).ifPresent(elementIterable::setOpenAsync);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.ITERATE_ALIGN_INDEX).map(BooleanUtils::toBoolean).ifPresent(elementIterable::setIteAlignIndex);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.ITERATE_STRATEGY).flatMap(IterateStrategyEnum::of).ifPresent(elementIterable::setIteStrategy);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnElementProperties.ITERATE_STRIDE).map(NumberUtils::toInt).ifPresent(elementIterable::setStride);
        setConsumer.accept(elementIterable);
    }

    private SequenceFlowImpl sequenceFlowMapping(ConfigResource config, org.camunda.bpm.model.bpmn.instance.SequenceFlow sf) {
        SequenceFlowImpl sequenceFlow = new SequenceFlowImpl();
        sequenceFlow.setId(sf.getId());
        sequenceFlow.setName(sf.getName());
        AssertUtil.notBlank(sequenceFlow.getId(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "The bpmn element id attribute cannot be empty! fileName: {}", config.getConfigName());
        if (sf.getConditionExpression() != null && StringUtils.isNotBlank(sf.getConditionExpression().getTextContent())) {
            SequenceFlowExpression sequenceFlowExpression = new SequenceFlowExpression(sf.getConditionExpression().getTextContent());
            sequenceFlowExpression.setId(sf.getConditionExpression().getId());
            sequenceFlowExpression.setName(sf.getConditionExpression().getTextContent());
            sequenceFlow.setExpression(sequenceFlowExpression);
        }
        return sequenceFlow;
    }

    private Collection<org.camunda.bpm.model.bpmn.instance.SequenceFlow> getIncoming(Set<String> cycleCrossFlows, FlowNode flowNode) {
        Collection<org.camunda.bpm.model.bpmn.instance.SequenceFlow> incoming = flowNode.getIncoming();
        if (CollectionUtils.isEmpty(incoming)) {
            return Lists.newArrayList();
        }
        return incoming.stream().filter(f -> !isCycleCrossPoint(cycleCrossFlows, f)).collect(Collectors.toList());
    }

    private boolean isCycleCrossPoint(Set<String> cycleCrossFlows, org.camunda.bpm.model.bpmn.instance.SequenceFlow seq) {
        return cycleCrossFlows.contains(seq.getId());
    }

    private void linkCycleCrossPoint(Set<String> cycleCrossFlows, Map<String, CycleCrossPointLink> cycleCrossPointMap, FlowNode targetNode, ProcessLink processLink) {
        if (targetNode == null || processLink == null) {
            return;
        }
        Collection<org.camunda.bpm.model.bpmn.instance.SequenceFlow> incoming = targetNode.getIncoming();
        if (CollectionUtils.isEmpty(incoming)) {
            return;
        }
        incoming.stream().filter(f -> isCycleCrossPoint(cycleCrossFlows, f)).forEach(seq -> {
            CycleCrossPointLink cycleCrossPointLink = cycleCrossPointMap.get(seq.getId());
            if (cycleCrossPointLink != null && cycleCrossPointLink.sequenceFlow != null) {
                processLink.byLinkCycle(cycleCrossPointLink.sequenceFlow);
                cycleCrossPointLink.processLink = processLink;
            } else {
                cycleCrossPointLink = new CycleCrossPointLink();
                cycleCrossPointLink.processLink = processLink;
                cycleCrossPointMap.put(seq.getId(), cycleCrossPointLink);
            }
        });
    }

    private SequenceFlow buildSequenceFlow(String id) {
        SequenceFlowImpl sequenceFlow = new SequenceFlowImpl();
        sequenceFlow.setId(GlobalUtil.uuid(BpmnTypeEnum.SEQUENCE_FLOW, id));
        sequenceFlow.setName(sequenceFlow.getId());
        return sequenceFlow;
    }

    private static class MergeIncomingRes {
        private SequenceFlow sf;
        private boolean isSkip;
        private ProcessLink processLink;
    }

    private static class CycleCrossPointLink {

        private ProcessLink processLink;

        private SequenceFlowImpl sequenceFlow;
    }
}