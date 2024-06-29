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
package cn.kstry.framework.core.component.jsprocess.transfer;

import cn.kstry.framework.core.bpmn.SequenceFlow;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.bpmn.impl.*;
import cn.kstry.framework.core.bus.InstructContent;
import cn.kstry.framework.core.component.bpmn.ProcessModelTransfer;
import cn.kstry.framework.core.component.bpmn.builder.InclusiveJoinPointBuilder;
import cn.kstry.framework.core.component.bpmn.builder.ParallelJoinPointBuilder;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessBuilder;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.joinpoint.InclusiveJoinPoint;
import cn.kstry.framework.core.component.bpmn.joinpoint.ParallelJoinPoint;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.bpmn.link.StartProcessLink;
import cn.kstry.framework.core.component.jsprocess.metadata.JsonNode;
import cn.kstry.framework.core.component.jsprocess.metadata.JsonProcess;
import cn.kstry.framework.core.component.jsprocess.metadata.JsonPropertySupport;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JSON 解析器实现
 *
 * @author lykan
 */
public class JsonProcessModelTransfer implements ProcessModelTransfer<List<JsonProcess>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonProcessModelTransfer.class);

    @Override
    public Optional<ProcessLink> getProcessLink(ConfigResource config, List<JsonProcess> instance, String startId) {
        AssertUtil.notNull(config, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
        if (CollectionUtils.isEmpty(instance) || StringUtils.isBlank(startId)) {
            return Optional.empty();
        }

        List<JsonProcess> processList = instance.stream().filter(process -> !process.isSubProcess() && Objects.equals(process.getStartId(), startId)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(processList)) {
            return Optional.empty();
        }
        AssertUtil.oneSize(processList, ExceptionEnum.ELEMENT_DUPLICATION_ERROR,
                "There are duplicate start event ids defined! startId: {}, fileName: {}", startId, config.getConfigName());

        JsonProcess jsonProcess = processList.get(0);
        if (CollectionUtils.isEmpty(jsonProcess.getJsonNodes())) {
            return Optional.empty();
        }

        checkJsonNodes(config, jsonProcess.getJsonNodes());
        complementSequenceFlow(jsonProcess.getJsonNodes());
        StartProcessLink processLink = StartProcessLink.build(jsonProcess.getStartId(), jsonProcess.getStartName(), jsonProcess.getProcessId(), jsonProcess.getProcessName());
        doGetStartProcessLink(config, jsonProcess, processLink);
        return Optional.of(processLink);
    }

    @Override
    public Optional<SubProcessLink> getSubProcessLink(ConfigResource config, List<JsonProcess> instance, String subProcessId) {
        AssertUtil.notNull(config, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
        if (StringUtils.isBlank(subProcessId) || CollectionUtils.isEmpty(instance)) {
            return Optional.empty();
        }

        List<JsonProcess> processList = instance.stream().filter(process -> process.isSubProcess() && Objects.equals(process.getProcessId(), subProcessId)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(processList)) {
            return Optional.empty();
        }
        AssertUtil.oneSize(processList, ExceptionEnum.ELEMENT_DUPLICATION_ERROR, "There are duplicate subProcess defined! subProcessId: {}, fileName: {}", subProcessId, config.getConfigName());

        JsonProcess sp = processList.get(0);
        checkJsonNodes(config, sp.getJsonNodes());
        complementSequenceFlow(sp.getJsonNodes());
        SubProcessLink subProcessLink = SubProcessLink.build(sp.getProcessId(), sp.getProcessName(), sp.getStartId(), sp.getStartName(), subBpmnLink -> doGetStartProcessLink(config, sp, subBpmnLink));
        ElementPropertyUtil.getJsonNodeProperty(sp, BpmnElementProperties.TASK_STRICT_MODE).map(BooleanUtils::toBooleanObject).filter(b -> !b).ifPresent(b -> subProcessLink.setStrictMode(false));
        ElementPropertyUtil.getJsonNodeProperty(sp, BpmnElementProperties.TASK_TIMEOUT).map(s -> NumberUtils.toInt(s, -1)).filter(i -> i >= 0).ifPresent(subProcessLink::setTimeout);
        ElementPropertyUtil.getJsonNodeProperty(sp, BpmnElementProperties.EXPRESSION_NOT_SKIP_EXPRESSION).ifPresent(subProcessLink::setNotSkipExp);
        fillIterableProperty(config, sp, subProcessLink::setElementIterable);
        subProcessLink.buildSubDiagramBpmnLink(config);
        return Optional.of(subProcessLink);
    }

    private void doGetStartProcessLink(ConfigResource config, JsonProcess jsonProcess, StartProcessLink processLink) {
        Map<String, JsonNode> jsonNodeMap = jsonProcess.getJsonNodes().stream().collect(Collectors.toMap(JsonNode::getId, Function.identity(), (x, y) -> x));
        JsonNode jsonStartEvent = jsonNodeMap.get(jsonProcess.getStartId());
        AssertUtil.notNull(jsonStartEvent, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "No StartEvent node defined in the process. startId: {}, fileName: {}", jsonProcess.getStartId(), config.getConfigName());

        Set<String> cycleCrossFlows = getCycleCrossFlows(jsonProcess);
        if (CollectionUtils.isNotEmpty(cycleCrossFlows)) {
            LOGGER.info("Automatic identification of cross points in loopback links. fileName: {}, processId: {}, points: {}", config.getConfigName(), jsonProcess.getProcessId(), cycleCrossFlows);
            AssertUtil.isTrue(cycleCrossFlows.size() == 1, ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                    "Only one link loopback and one cross point is allowed in a process! fileName: {}, processId: {}, points: {}", config.getConfigName(), jsonProcess.getProcessId(), cycleCrossFlows);
        }
        Map<String, CycleCrossPointLink> cycleCrossPointMap = Maps.newHashMap();
        Set<String> circularDependencyCheck = Sets.newHashSet();
        Map<String, Integer> comingCountMap = Maps.newHashMap();
        Map<ProcessLink, ProcessLink> inclusiveProcessLinkMap = Maps.newHashMap();
        Map<String, InclusiveJoinPoint> joinAssistMap = Maps.newHashMap();
        Map<String, ProcessLink> nextBpmnLinkMap = Maps.newHashMap();
        nextBpmnLinkMap.put(jsonStartEvent.getId(), processLink);
        InStack<JsonNode> basicInStack = new BasicInStack<>();
        basicInStack.push(jsonStartEvent);
        while (!basicInStack.isEmpty()) {
            JsonNode jsonNode = basicInStack.pop().orElseThrow(() -> ExceptionUtil.buildException(null, ExceptionEnum.SYSTEM_ERROR, null));
            if (BpmnTypeEnum.END_EVENT.is(jsonNode.getType())) {
                continue;
            }

            ProcessLink beforeProcessLink = nextBpmnLinkMap.get(jsonNode.getId());
            if (BpmnTypeEnum.INCLUSIVE_GATEWAY.is(jsonNode.getType())) {
                beforeProcessLink = inclusiveProcessLinkMap.getOrDefault(beforeProcessLink, beforeProcessLink);
            }

            for (String nextNodeId : jsonNode.getNextNodes()) {
                JsonNode nextNode = jsonNodeMap.get(nextNodeId);
                AssertUtil.isTrue(nextNode != null && BpmnTypeEnum.SEQUENCE_FLOW.is(nextNode.getType()) && CollectionUtils.size(nextNode.getNextNodes()) == 1,
                        ExceptionEnum.CONFIGURATION_FLOW_ERROR, "Wrong branch in the path of an element! nearby nodeId: {}, fileName: {}", jsonNode.getId(), config.getConfigName());
                assert nextNode != null;
                if (isCycleCrossPoint(cycleCrossFlows, nextNode)) {
                    AssertUtil.isTrue(cycleCrossFlows.contains(nextNode.getId()));
                    SequenceFlowImpl sequenceFlow = sequenceFlowMapping(config, nextNode);
                    beforeProcessLink.getElement().outing(sequenceFlow);
                    CycleCrossPointLink cycleCrossPointLink = cycleCrossPointMap.get(nextNode.getId());
                    if (cycleCrossPointLink == null || cycleCrossPointLink.processLink == null) {
                        cycleCrossPointLink = new CycleCrossPointLink();
                        cycleCrossPointLink.sequenceFlow = sequenceFlow;
                        cycleCrossPointMap.put(nextNode.getId(), cycleCrossPointLink);
                    } else {
                        cycleCrossPointLink.sequenceFlow = sequenceFlow;
                        cycleCrossPointLink.processLink.byLinkCycle(sequenceFlow);
                    }
                    continue;
                }

                JsonNode targetNode = jsonNodeMap.get(nextNode.getNextNodes().get(0));
                AssertUtil.notNull(targetNode, ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                        "Wrong branch in the path of an element! nodeId: {}, fileName: {}", jsonNode.getId(), config.getConfigName());
                if (BpmnTypeEnum.END_EVENT.is(targetNode.getType())) {
                    beforeProcessLink.end(sequenceFlowMapping(config, nextNode));
                } else if (BpmnTypeEnum.PARALLEL_GATEWAY.is(targetNode.getType())) {
                    ProcessLink parallelProcessLink = nextBpmnLinkMap.computeIfAbsent(targetNode.getId(), node -> {
                        ParallelJoinPointBuilder parallelJoinPointBuilder = processLink.parallel(targetNode.getId());
                        ElementPropertyUtil.getJsonNodeProperty(targetNode,
                                BpmnElementProperties.ASYNC_ELEMENT_OPEN_ASYNC).map(BooleanUtils::toBoolean).filter(b -> b).ifPresent(b -> parallelJoinPointBuilder.openAsync());
                        ElementPropertyUtil.getJsonNodeProperty(targetNode,
                                BpmnElementProperties.TASK_STRICT_MODE).map(BooleanUtils::toBooleanObject).filter(b -> !b).ifPresent(b -> parallelJoinPointBuilder.notStrictMode());
                        return parallelJoinPointBuilder.build();
                    });
                    linkCycleCrossPoint(jsonProcess, cycleCrossFlows, cycleCrossPointMap, targetNode, parallelProcessLink);
                    ProcessLink nextProcessLink = beforeProcessLink.nextParallel(sequenceFlowMapping(config, nextNode), (ParallelJoinPoint) parallelProcessLink);
                    nextBpmnLinkMap.put(targetNode.getId(), nextProcessLink);
                } else if (BpmnTypeEnum.INCLUSIVE_GATEWAY.is(targetNode.getType())) {
                    ProcessLink inclusiveProcessLink = nextBpmnLinkMap.computeIfAbsent(targetNode.getId(), node -> {
                        ServiceTaskImpl serviceTask = getServiceTask(jsonNodeMap.get(node), config, true);
                        InclusiveJoinPoint beforeMockLink = processLink.inclusive(GlobalUtil.uuid(BpmnTypeEnum.INCLUSIVE_GATEWAY, targetNode.getId())).build();
                        ProcessLink nextMockLink = instructWrapper(config, jsonProcess, true, targetNode, serviceTask, null, beforeMockLink, null, cycleCrossFlows);
                        InclusiveJoinPointBuilder inclusiveJoinPointBuilder = processLink.inclusive(targetNode.getId());
                        ElementPropertyUtil.getJsonNodeProperty(targetNode,
                                BpmnElementProperties.ASYNC_ELEMENT_OPEN_ASYNC).map(BooleanUtils::toBoolean).filter(b -> b).ifPresent(b -> inclusiveJoinPointBuilder.openAsync());
                        ElementPropertyUtil.getJsonNodeProperty(targetNode,
                                BpmnElementProperties.INCLUSIVE_GATEWAY_COMPLETED_COUNT).map(NumberUtils::toInt).ifPresent(inclusiveJoinPointBuilder::completedCount);
                        InclusiveJoinPoint actualInclusive = inclusiveJoinPointBuilder.build();
                        if (beforeMockLink == nextMockLink) {
                            linkCycleCrossPoint(jsonProcess, cycleCrossFlows, cycleCrossPointMap, targetNode, actualInclusive);
                            ElementPropertyUtil.getJsonNodeProperty(targetNode, BpmnElementProperties.INCLUSIVE_GATEWAY_MIDWAY_START_ID).ifPresent(inclusiveJoinPointBuilder::midwayStartId);
                            return actualInclusive;
                        }
                        linkCycleCrossPoint(jsonProcess, cycleCrossFlows, cycleCrossPointMap, targetNode, beforeMockLink);
                        ElementPropertyUtil.getJsonNodeProperty(targetNode, BpmnElementProperties.INCLUSIVE_GATEWAY_MIDWAY_START_ID).ifPresent(m -> ((InclusiveGatewayImpl) beforeMockLink.getElement()).setMidwayStartId(m));
                        nextMockLink.nextInclusive(buildSequenceFlow(targetNode.getId()), actualInclusive);
                        inclusiveProcessLinkMap.put(beforeMockLink, actualInclusive);
                        return beforeMockLink;
                    });
                    beforeProcessLink.nextInclusive(sequenceFlowMapping(config, nextNode), (InclusiveJoinPoint) inclusiveProcessLink);
                    nextBpmnLinkMap.put(targetNode.getId(), inclusiveProcessLink);
                } else if (BpmnTypeEnum.EXCLUSIVE_GATEWAY.is(targetNode.getType())) {
                    MergeIncomingRes mergeRes = tryMergeIncoming(cycleCrossFlows, jsonProcess, processLink, config, nextNode, targetNode, joinAssistMap, beforeProcessLink);
                    if (mergeRes.isSkip) {
                        continue;
                    }
                    SequenceFlow sf = mergeRes.sf;
                    ProcessLink nextProcessLink = instructWrapper(config, jsonProcess, true, targetNode, getServiceTask(targetNode, config, true), sf, mergeRes.processLink, cycleCrossPointMap, cycleCrossFlows);
                    if (nextProcessLink != mergeRes.processLink) {
                        sf = buildSequenceFlow(targetNode.getId());
                    }
                    nextProcessLink = nextProcessLink.nextExclusive(targetNode.getId(), sf).build();
                    nextBpmnLinkMap.put(targetNode.getId(), nextProcessLink);
                    basicInStack.push(targetNode);
                } else if (BpmnTypeEnum.SERVICE_TASK.is(targetNode.getType())) {
                    MergeIncomingRes mergeRes = tryMergeIncoming(cycleCrossFlows, jsonProcess, processLink, config, nextNode, targetNode, joinAssistMap, beforeProcessLink);
                    if (mergeRes.isSkip) {
                        continue;
                    }
                    ServiceTaskImpl serviceTask = getServiceTask(targetNode, config, false);
                    ProcessLink nextProcessLink = instructWrapper(config, jsonProcess, false, targetNode, serviceTask, mergeRes.sf, mergeRes.processLink, cycleCrossPointMap, cycleCrossFlows);
                    nextBpmnLinkMap.put(targetNode.getId(), nextProcessLink);
                    basicInStack.push(targetNode);
                } else if (BpmnTypeEnum.SUB_PROCESS.is(targetNode.getType())) {
                    MergeIncomingRes mergeRes = tryMergeIncoming(cycleCrossFlows, jsonProcess, processLink, config, nextNode, targetNode, joinAssistMap, beforeProcessLink);
                    if (mergeRes.isSkip) {
                        continue;
                    }
                    SubProcessBuilder subProcessBuilder = mergeRes.processLink.nextSubProcess(mergeRes.sf, targetNode.getCallSubProcessId());
                    fillIterableProperty(config, targetNode, subProcessBuilder::iterable);
                    ElementPropertyUtil.getJsonNodeProperty(targetNode,
                            BpmnElementProperties.TASK_STRICT_MODE).map(BooleanUtils::toBooleanObject).filter(b -> !b).ifPresent(b -> subProcessBuilder.notStrictMode());
                    ElementPropertyUtil.getJsonNodeProperty(targetNode, BpmnElementProperties.TASK_TIMEOUT).map(s -> NumberUtils.toInt(s, -1)).filter(i -> i >= 0).ifPresent
                            (subProcessBuilder::timeout);
                    ElementPropertyUtil.getJsonNodeProperty(targetNode, BpmnElementProperties.EXPRESSION_NOT_SKIP_EXPRESSION).ifPresent(subProcessBuilder::notSkipExp);

                    ProcessLink subProcessLink = subProcessBuilder.build();
                    linkCycleCrossPoint(jsonProcess, cycleCrossFlows, cycleCrossPointMap, targetNode, subProcessLink);
                    nextBpmnLinkMap.put(targetNode.getId(), subProcessLink);
                    basicInStack.push(targetNode);
                } else {
                    throw ExceptionUtil.buildException(null, ExceptionEnum.CONFIGURATION_UNSUPPORTED_ELEMENT,
                            GlobalUtil.format("There is an error in the json file! fileName: {}, processId: {}", config.getConfigName(), jsonProcess.getProcessId()));
                }

                if (isBpmnSupportAggregation(targetNode)) {
                    comingCountMap.merge(targetNode.getId(), 1, Integer::sum);
                    long incomingCount = getIncoming(jsonProcess, targetNode, cycleCrossFlows).size();
                    if (Objects.equals(comingCountMap.get(targetNode.getId()), Long.valueOf(incomingCount).intValue())) {
                        basicInStack.push(targetNode);
                    }
                } else {
                    AssertUtil.notTrue(circularDependencyCheck.contains(targetNode.getId()), ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                            "Duplicate calls between elements are not allowed! fileName: {}, processId: {}, elementId: {}, elementName: {}",
                            config.getConfigName(), jsonProcess.getProcessId(), targetNode.getId(), targetNode.getName());
                    circularDependencyCheck.add(targetNode.getId());
                }
            }
        }
    }

    private void linkCycleCrossPoint(JsonProcess jsonProcess, Set<String> cycleCrossFlows,
                                     Map<String, CycleCrossPointLink> cycleCrossPointMap, JsonNode targetNode, ProcessLink processLink) {
        if (targetNode == null || processLink == null) {
            return;
        }
        List<JsonNode> incoming = getIncoming(jsonProcess, targetNode, null);
        if (CollectionUtils.isEmpty(incoming)) {
            return;
        }
        incoming.stream().filter(f -> isCycleCrossPoint(cycleCrossFlows, f)).forEach(seq -> {
            CycleCrossPointLink cycleCrossPointLink = cycleCrossPointMap.get(seq.getId());
            if (cycleCrossPointLink == null || cycleCrossPointLink.sequenceFlow == null) {
                cycleCrossPointLink = new CycleCrossPointLink();
                cycleCrossPointLink.processLink = processLink;
                cycleCrossPointMap.put(seq.getId(), cycleCrossPointLink);
            } else {
                processLink.byLinkCycle(cycleCrossPointLink.sequenceFlow);
                cycleCrossPointLink.processLink = processLink;
            }
        });
    }

    private boolean isCycleCrossPoint(Set<String> cycleCrossFlows, JsonNode nextNode) {
        return cycleCrossFlows.contains(nextNode.getId());
    }

    private Set<String> getCycleCrossFlows(JsonProcess jsonProcess) {
        if (jsonProcess == null) {
            return Sets.newHashSet();
        }
        Map<String, JsonNode> jsonNodeMap = jsonProcess.getJsonNodes().stream().collect(Collectors.toMap(JsonNode::getId, Function.identity(), (x, y) -> x));
        JsonNode jsonStartEvent = jsonNodeMap.get(jsonProcess.getStartId());
        if (jsonStartEvent == null) {
            return Sets.newHashSet();
        }
        Set<String> oldFlowIds = Sets.newHashSet();
        List<JsonNode> flowNodeList = Lists.newArrayList();
        flowNodeList.add(jsonStartEvent);
        while (true) {
            List<JsonNode> needRemoveList = flowNodeList.stream()
                    .filter(n -> {
                        List<String> incomingIds = getIncoming(jsonProcess, n, null).stream().map(JsonNode::getId).collect(Collectors.toList());
                        return CollectionUtils.isEmpty(incomingIds) || oldFlowIds.containsAll(incomingIds);
                    })
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(needRemoveList)) {
                needRemoveList.forEach(n -> {
                    flowNodeList.remove(n);
                    List<String> outgoing = n.getNextNodes();
                    if (CollectionUtils.isNotEmpty(outgoing)) {
                        oldFlowIds.addAll(outgoing);
                    }
                    outgoing.stream().filter(jsonNodeMap::containsKey).map(jsonNodeMap::get).map(JsonNode::getNextNodes)
                            .filter(CollectionUtils::isNotEmpty).map(list -> list.get(0)).filter(jsonNodeMap::containsKey).map(jsonNodeMap::get).forEach(flowNodeList::add);
                });
                continue;
            }
            return flowNodeList.stream().flatMap(r -> getIncoming(jsonProcess, r, null).stream()).map(JsonNode::getId).filter(id -> !oldFlowIds.contains(id)).collect(Collectors.toSet());
        }
    }

    private MergeIncomingRes tryMergeIncoming(Set<String> cycleCrossFlows, JsonProcess jsonProcess, StartProcessLink processLink, ConfigResource config,
                                              JsonNode nextNode, JsonNode targetNode, Map<String, InclusiveJoinPoint> joinAssistMap, ProcessLink beforeProcessLink) {
        MergeIncomingRes res = new MergeIncomingRes();
        long incomingCount = getIncoming(jsonProcess, targetNode, cycleCrossFlows).size();
        if (incomingCount <= 1) {
            res.isSkip = false;
            res.sf = sequenceFlowMapping(config, nextNode);
            res.processLink = beforeProcessLink;
            return res;
        }
        InclusiveJoinPoint mockInclusive = joinAssistMap.computeIfAbsent(targetNode.getId(), n -> processLink.inclusive().build());
        res.processLink = beforeProcessLink.nextInclusive(sequenceFlowMapping(config, nextNode), mockInclusive);
        res.isSkip = mockInclusive.getElement().comingList().size() < incomingCount;
        res.sf = buildSequenceFlow(targetNode.getId());
        return res;
    }

    private boolean isBpmnSupportAggregation(JsonNode targetNode) {
        return BpmnTypeEnum.END_EVENT.is(targetNode.getType()) || BpmnTypeEnum.PARALLEL_GATEWAY.is(targetNode.getType()) || BpmnTypeEnum.INCLUSIVE_GATEWAY.is(targetNode.getType());
    }

    private void fillRateLimiterProperty(JsonPropertySupport flowNode, ServiceTaskImpl serviceTaskImpl) {
        RateLimiterBuilder builder = new RateLimiterBuilder();
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_LIMIT_NAME).ifPresent(builder::name);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_LIMIT_PERMITS).map(NumberUtils::toDouble).ifPresent(builder::permits);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_LIMIT_WARMUP_PERIOD).map(NumberUtils::toInt).ifPresent(builder::warmupPeriod);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_LIMIT_ACQUIRE_TIMEOUT).map(NumberUtils::toInt).ifPresent(builder::acquireTimeout);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_LIMIT_FAIL_STRATEGY).ifPresent(builder::failStrategy);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_LIMIT_EXPRESSION).ifPresent(builder::expression);
        serviceTaskImpl.setRateLimiterConfig(builder.build());
    }

    private void fillIterableProperty(ConfigResource config, JsonPropertySupport flowNode, Consumer<BasicElementIterable> setConsumer) {
        BasicElementIterable elementIterable = new BasicElementIterable();
        Optional<String> iteSourceProperty = ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.ITERATE_SOURCE);
        if (iteSourceProperty.filter(s -> {
            if (!ElementParserUtil.isValidDataExpression(s)) {
                LOGGER.warn("[{}] The set ite-source being iterated over is invalid. fileName: {}, calledElementId: {}", ExceptionEnum.BPMN_ATTRIBUTE_INVALID.getExceptionCode(), config.getConfigName(), flowNode.getId());
                return false;
            }
            return true;
        }).map(StringUtils::isNotBlank).orElse(false)) {
            elementIterable.setIteSource(iteSourceProperty.get());
        }
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.ITERATE_ASYNC).map(BooleanUtils::toBoolean).ifPresent(elementIterable::setOpenAsync);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.ITERATE_ALIGN_INDEX).map(BooleanUtils::toBoolean).ifPresent(elementIterable::setIteAlignIndex);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.ITERATE_STRATEGY).flatMap(IterateStrategyEnum::of).ifPresent(elementIterable::setIteStrategy);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.ITERATE_STRIDE).map(NumberUtils::toInt).ifPresent(elementIterable::setStride);
        setConsumer.accept(elementIterable);
    }

    private SequenceFlow buildSequenceFlow(String id) {
        SequenceFlowImpl sequenceFlow = new SequenceFlowImpl();
        sequenceFlow.setId(GlobalUtil.uuid(BpmnTypeEnum.SEQUENCE_FLOW, id));
        sequenceFlow.setName(sequenceFlow.getId());
        return sequenceFlow;
    }

    private ProcessLink instructWrapper(ConfigResource config, JsonProcess jsonProcess, boolean allowEmpty, JsonNode targetNode, ServiceTaskImpl serviceTask,
                                        SequenceFlow firstSequenceFlow, ProcessLink nextProcessLink, Map<String, CycleCrossPointLink> cycleCrossPointMap, Set<String> cycleCrossFlows) {
        ProcessLink before = nextProcessLink;
        boolean isLink = cycleCrossPointMap != null;
        ServiceTaskImpl sTask = getServiceTask(targetNode, config, true);

        List<InstructContent> beforeInstructContentList = getInstructContentList(targetNode, true);
        if (CollectionUtils.isNotEmpty(beforeInstructContentList)) {
            for (InstructContent instructContent : beforeInstructContentList) {
                SequenceFlow sf;
                if (firstSequenceFlow == null) {
                    sf = buildSequenceFlow(targetNode.getId());
                } else {
                    sf = firstSequenceFlow;
                    firstSequenceFlow = null;
                }
                String instruct = instructContent.getInstruct().substring(1);
                nextProcessLink = TaskServiceUtil.instructTaskBuilder(nextProcessLink.nextInstruct(sf, instruct, instructContent.getContent()), sTask)
                        .name(instruct + "@" + serviceTask.getName()).id(GlobalUtil.uuid(BpmnTypeEnum.SERVICE_TASK, "Instruct-" + targetNode.getId())).build();
                if (isLink) {
                    linkCycleCrossPoint(jsonProcess, cycleCrossFlows, cycleCrossPointMap, targetNode, nextProcessLink);
                    isLink = false;
                }
            }
        }

        if (serviceTask.validTask()) {
            SequenceFlow sf;
            if (firstSequenceFlow == null) {
                sf = buildSequenceFlow(targetNode.getId());
            } else {
                sf = firstSequenceFlow;
                firstSequenceFlow = null;
            }
            nextProcessLink = nextProcessLink.nextTask(sf, serviceTask);
            if (isLink) {
                linkCycleCrossPoint(jsonProcess, cycleCrossFlows, cycleCrossPointMap, targetNode, nextProcessLink);
                isLink = false;
            }
        }

        List<InstructContent> afterInstructContentList = getInstructContentList(targetNode, false);
        if (CollectionUtils.isNotEmpty(afterInstructContentList)) {
            for (InstructContent instructContent : afterInstructContentList) {
                SequenceFlow sf;
                if (firstSequenceFlow == null) {
                    sf = buildSequenceFlow(targetNode.getId());
                } else {
                    sf = firstSequenceFlow;
                    firstSequenceFlow = null;
                }
                boolean isSerialize = targetNode.getId().startsWith("SERVICE_TASK-Instruct-") && CollectionUtils.size(afterInstructContentList) == 1;
                nextProcessLink = TaskServiceUtil.instructTaskBuilder(nextProcessLink.nextInstruct(sf, instructContent.getInstruct(), instructContent.getContent()), sTask)
                        .name(isSerialize ? serviceTask.getName() : (serviceTask.getName() + "@" + instructContent.getInstruct()))
                        .id(isSerialize ? targetNode.getId() : (GlobalUtil.uuid(BpmnTypeEnum.SERVICE_TASK, "Instruct-" + targetNode.getId())))
                        .build();
                if (isLink) {
                    linkCycleCrossPoint(jsonProcess, cycleCrossFlows, cycleCrossPointMap, targetNode, nextProcessLink);
                    isLink = false;
                }
            }
        }
        AssertUtil.isTrue(allowEmpty || before != nextProcessLink,
                ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "Invalid serviceNode definition, please add the necessary attributes! elementId: {}", targetNode.getId());
        return nextProcessLink;
    }


    private List<InstructContent> getInstructContentList(JsonNode flowNode, boolean isBefore) {
        List<Pair<String, String>> instructPairList =
                ElementPropertyUtil.getJsonNodeProperties(flowNode, (isBefore ? "^" : StringUtils.EMPTY) + BpmnElementProperties.SERVICE_TASK_TASK_INSTRUCT);
        if (CollectionUtils.isNotEmpty(instructPairList)) {
            return instructPairList.stream().filter(instructPair -> StringUtils.isNotBlank(instructPair.getLeft()))
                    .map(instructPair -> new InstructContent(Optional.of(instructPair.getLeft()).map(String::trim).orElse(null), instructPair.getRight())).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    private ServiceTaskImpl getServiceTask(JsonNode flowNode, ConfigResource config, boolean decorate) {
        ServiceTaskImpl serviceTaskImpl = new ServiceTaskImpl();
        serviceTaskImpl.setId(decorate ? GlobalUtil.uuid(BpmnTypeEnum.SERVICE_TASK, flowNode.getId()) : flowNode.getId());
        serviceTaskImpl.setName(flowNode.getName());
        AssertUtil.notBlank(serviceTaskImpl.getId(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "The json element id attribute cannot be empty! fileName: {}", config.getConfigName());
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_TASK_COMPONENT).ifPresent(serviceTaskImpl::setTaskComponent);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_TASK_SERVICE).ifPresent(serviceTaskImpl::setTaskService);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_TASK_PROPERTY).ifPresent(serviceTaskImpl::setTaskProperty);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_DEMOTION).ifPresent(serviceTaskImpl::setTaskDemotion);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_TASK_PARAMS).ifPresent(serviceTaskImpl::setTaskParams);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.EXPRESSION_NOT_SKIP_EXPRESSION).ifPresent(exp -> {
            SequenceFlowExpression sequenceFlowExpression = new SequenceFlowExpression(exp);
            sequenceFlowExpression.setId(GlobalUtil.uuid(BpmnTypeEnum.EXPRESSION));
            sequenceFlowExpression.setName(exp);
            serviceTaskImpl.setExpression(sequenceFlowExpression);
        });
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_RETRY_TIMES).map(NumberUtils::toInt).filter(i -> i > 0).ifPresent(serviceTaskImpl::setRetryTimes);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.SERVICE_TASK_CUSTOM_ROLE).flatMap(CustomRoleInfo::buildCustomRole).ifPresent(serviceTaskImpl::setCustomRoleInfo);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.TASK_ALLOW_ABSENT).map(BooleanUtils::toBooleanObject).ifPresent(serviceTaskImpl::setAllowAbsent);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.TASK_STRICT_MODE).map(BooleanUtils::toBooleanObject).ifPresent(serviceTaskImpl::setStrictMode);
        ElementPropertyUtil.getJsonNodeProperty(flowNode, BpmnElementProperties.TASK_TIMEOUT).map(s -> NumberUtils.toInt(s, -1)).filter(i -> i >= 0).ifPresent(serviceTaskImpl::setTimeout);
        fillRateLimiterProperty(flowNode, serviceTaskImpl);
        fillIterableProperty(config, flowNode, serviceTaskImpl::mergeElementIterable);
        return serviceTaskImpl;
    }

    private List<JsonNode> getIncoming(JsonProcess jsonProcess, JsonNode targetNode, Set<String> cycleCrossFlows) {
        if (jsonProcess == null || CollectionUtils.isEmpty(jsonProcess.getJsonNodes()) || targetNode == null) {
            return Lists.newArrayList();
        }
        List<JsonNode> incoming = jsonProcess.getJsonNodes().stream()
                .filter(in -> CollectionUtils.isNotEmpty(in.getNextNodes()) && in.getNextNodes().contains(targetNode.getId()))
                .collect(Collectors.toList());
        if (cycleCrossFlows == null) {
            return incoming;
        }
        if (CollectionUtils.isEmpty(incoming)) {
            return Lists.newArrayList();
        }
        return incoming.stream().filter(f -> !isCycleCrossPoint(cycleCrossFlows, f)).collect(Collectors.toList());
    }

    private SequenceFlowImpl sequenceFlowMapping(ConfigResource config, JsonNode sf) {
        SequenceFlowImpl sequenceFlow = new SequenceFlowImpl();
        sequenceFlow.setId(sf.getId());
        sequenceFlow.setName(sf.getName());
        AssertUtil.notBlank(sequenceFlow.getId(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "The element id attribute cannot be empty! fileName: {}", config.getConfigName());
        if (StringUtils.isNotBlank(sf.getFlowExpression())) {
            SequenceFlowExpression sequenceFlowExpression = new SequenceFlowExpression(sf.getFlowExpression());
            sequenceFlowExpression.setId(GlobalUtil.uuid(BpmnTypeEnum.EXPRESSION));
            sequenceFlowExpression.setName(sf.getFlowExpression());
            sequenceFlow.setExpression(sequenceFlowExpression);
        }
        return sequenceFlow;
    }

    private void complementSequenceFlow(List<JsonNode> jsonNodes) {
        if (CollectionUtils.isEmpty(jsonNodes)) {
            return;
        }
        Map<String, JsonNode> nodeMap = jsonNodes.stream().collect(Collectors.toMap(JsonNode::getId, Function.identity(), (x, y) -> y));
        Lists.newArrayList(jsonNodes).stream().filter(n -> !BpmnTypeEnum.SEQUENCE_FLOW.is(n.getType())).forEach(n -> {
            List<String> nextNodeIds = n.getNextNodes();
            if (CollectionUtils.isEmpty(nextNodeIds)) {
                return;
            }
            List<String> newNextNodeIds = Lists.newArrayList();
            nextNodeIds.forEach(nextNodeId -> {
                JsonNode nextNode = nodeMap.get(nextNodeId);
                if (nextNode == null || BpmnTypeEnum.SEQUENCE_FLOW.is(nextNode.getType())) {
                    newNextNodeIds.add(nextNodeId);
                    return;
                }
                String sequenceFlowId = GlobalUtil.uuid(BpmnTypeEnum.SEQUENCE_FLOW);
                JsonNode jn = new JsonNode();
                jn.setId(sequenceFlowId);
                jn.setName("ComplementSequenceFlow-" + sequenceFlowId);
                jn.setType(BpmnTypeEnum.SEQUENCE_FLOW.getType());
                jn.setNextNodes(Lists.newArrayList(nextNodeId));
                jsonNodes.add(jn);
                newNextNodeIds.add(sequenceFlowId);
            });
            n.setNextNodes(newNextNodeIds);
        });
    }

    private static void checkJsonNodes(ConfigResource config, List<JsonNode> jsonNodes) {
        if (CollectionUtils.isEmpty(jsonNodes)) {
            return;
        }

        Map<String, JsonNode> nodeMap = jsonNodes.stream().collect(Collectors.toMap(JsonNode::getId, Function.identity(), (x, y) -> y));
        jsonNodes.forEach(jsonNode -> {
            AssertUtil.notBlank(jsonNode.getId(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "The process node id is not allowed to be empty. fileName: {}", config.getConfigName());

            AssertUtil.present(BpmnTypeEnum.of(jsonNode.getType()), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED,
                    "The process node type is not allowed to be empty or error. nodeId: {}, fileName: {}", jsonNode.getId(), config.getConfigName());

            if (!BpmnTypeEnum.END_EVENT.is(jsonNode.getType())) {
                AssertUtil.notEmpty(jsonNode.getNextNodes(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED,
                        "The process node nextNodes is not allowed to be empty. nodeId: {}, fileName: {}", jsonNode.getId(), config.getConfigName());
            }
            if (CollectionUtils.isNotEmpty(jsonNode.getNextNodes())) {
                for (String nextNodeId : jsonNode.getNextNodes()) {
                    AssertUtil.notNull(nodeMap.get(nextNodeId), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED,
                            "Next process node does not exist. nodeId: {}, nextId: {}, fileName: {}", jsonNode.getId(), nextNodeId, config.getConfigName());
                }
            }
            if (BpmnTypeEnum.SUB_PROCESS.is(jsonNode.getType())) {
                AssertUtil.notBlank(jsonNode.getCallSubProcessId(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED,
                        "The process node call-sub-process-id is not allowed to be empty. nodeId: {}, fileName: {}", jsonNode.getId(), config.getConfigName());
            }
            if (StringUtils.isNotBlank(jsonNode.getCallSubProcessId())) {
                AssertUtil.notBlank(jsonNode.getCallSubProcessId(), ExceptionEnum.BPMN_ATTRIBUTE_INVALID,
                        "The process node type must be sub_process when call-sub-process-id appears. nodeId: {}, fileName: {}", jsonNode.getId(), config.getConfigName());
            }
        });

        List<String> duplicateIdList = jsonNodes.stream().map(JsonNode::getId).collect(Collectors.toMap(e -> e, e -> 1, Integer::sum))
                .entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
        AssertUtil.isEmpty(duplicateIdList, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "Node ids in the same process are not allowed to be repeated. ids: {}, fileName: {}", duplicateIdList, config.getConfigName());
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
