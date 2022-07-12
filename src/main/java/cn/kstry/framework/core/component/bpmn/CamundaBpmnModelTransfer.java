/*
 *
 *  * Copyright (c) 2020-2022, Lykan (jiashuomeng@gmail.com).
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
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.bpmn.impl.*;
import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.constant.BpmnConstant;
import cn.kstry.framework.core.constant.GlobalConstant;
import cn.kstry.framework.core.util.CustomRoleInfo;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementPropertyUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.FlowNode;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Camunda 解析器实现
 *
 * @author lykan
 */
public class CamundaBpmnModelTransfer implements BpmnModelTransfer<BpmnModelInstance> {

    /**
     * Camunda 中定义的 ServiceTask、Task 类型常量
     */
    private final List<String> CAMUNDA_TASK_TYPE_LIST = Lists.newArrayList(BpmnModelConstants.BPMN_ELEMENT_SERVICE_TASK, BpmnModelConstants.BPMN_ELEMENT_TASK);

    @Override
    public Optional<StartEvent> getKstryModel(@Nonnull Map<String, SubProcess> allSubProcess,
                                              ConfigResource config, BpmnModelInstance instance, String startId) {

        AssertUtil.notNull(config, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
        if (instance == null || StringUtils.isBlank(startId)) {
            return Optional.empty();
        }

        org.camunda.bpm.model.bpmn.instance.StartEvent camundaStartEvent = instance.getModelElementById(startId);
        return doGetKstryModel(allSubProcess, config, camundaStartEvent);
    }

    @Override
    public Map<String, SubProcess> getAllSubProcess(ConfigResource config, BpmnModelInstance instance) {
        AssertUtil.notNull(config, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
        Map<String, SubProcess> subProcessMap = Maps.newHashMap();
        if (instance == null) {
            return subProcessMap;
        }
        Collection<org.camunda.bpm.model.bpmn.instance.SubProcess> subProcesses =
                instance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.SubProcess.class);
        if (CollectionUtils.isEmpty(subProcesses)) {
            return subProcessMap;
        }
        subProcesses.forEach(sp -> {
            SubProcess subProcess = getKstrySubProcess(config, sp);
            AssertUtil.notTrue(subProcessMap.containsKey(subProcess.getId()), ExceptionEnum.ELEMENT_DUPLICATION_ERROR,
                    "There are duplicate SubProcess ids defined! id: {}, fileName: {}", subProcess.getId(), config.getConfigName());
            subProcessMap.put(subProcess.getId(), subProcess);
        });
        return subProcessMap;
    }

    private Optional<StartEvent> doGetKstryModel(Map<String, SubProcess> allSubProcess,
                                                 ConfigResource config, org.camunda.bpm.model.bpmn.instance.StartEvent camundaStartEvent) {
        if (camundaStartEvent == null) {
            return Optional.empty();
        }

        StartEvent startEvent = (StartEvent) elementMapping(allSubProcess, config, camundaStartEvent);
        BpmnMappingItem bpmnMappingItem = new BpmnMappingItem(camundaStartEvent, startEvent);

        Map<String, BpmnMappingItem> bpmnMappingItemCache = Maps.newHashMap();
        bpmnMappingItemCache.put(bpmnMappingItem.getId(), bpmnMappingItem);

        Map<FlowElement, Integer> comingCountMap = Maps.newHashMap();
        InStack<BpmnMappingItem> basicInStack = new BasicInStack<>();
        basicInStack.push(bpmnMappingItem);
        while (!basicInStack.isEmpty()) {
            BpmnMappingItem mappingItem = basicInStack.pop().orElseThrow(() -> KstryException.buildException(null, ExceptionEnum.SYSTEM_ERROR, null));
            if (ElementPropertyUtil.isSupportAggregation(mappingItem.getKstryElement())) {
                comingCountMap.merge(mappingItem.getKstryElement(), 1, Integer::sum);
                int targetSize = ((FlowNode) mappingItem.getCamundaElement()).getIncoming().size();
                Integer size = comingCountMap.get(mappingItem.getKstryElement());
                if (!Objects.equals(size, targetSize)) {
                    continue;
                }
            }

            if (mappingItem.getCamundaElement() instanceof org.camunda.bpm.model.bpmn.instance.SequenceFlow) {
                FlowNode targetNode = ((org.camunda.bpm.model.bpmn.instance.SequenceFlow) mappingItem.getCamundaElement()).getTarget();

                FlowElement kstryNode = elementMapping(allSubProcess, config, targetNode);
                BpmnMappingItem item = getCacheMappingItem(bpmnMappingItemCache, targetNode, kstryNode);
                mappingItem.getKstryElement().outing(item.getKstryElement());
                basicInStack.push(item);

            } else if (mappingItem.getCamundaElement() instanceof FlowNode) {
                List<BpmnMappingItem> itemList = ((FlowNode) mappingItem.getCamundaElement()).getOutgoing().stream()
                        .map(flow -> {
                            SequenceFlow sf = sequenceFlowMapping(config, flow);
                            BpmnMappingItem item = getCacheMappingItem(bpmnMappingItemCache, flow, sf);
                            mappingItem.getKstryElement().outing(item.getKstryElement());
                            return item;
                        })
                        .collect(Collectors.toList());
                basicInStack.pushList(itemList);
            } else {
                throw KstryException.buildException(null, ExceptionEnum.CONFIGURATION_UNSUPPORTED_ELEMENT,
                        GlobalUtil.format("There is an error in the bpmn file! fileName: {}", config.getConfigName()));
            }
        }

        return Optional.of(startEvent);
    }

    private FlowElement elementMapping(Map<String, SubProcess> allSubProcess, ConfigResource config, FlowNode flowNode) {
        if (flowNode == null) {
            return null;
        }

        FlowElement flowElement;
        if (org.camunda.bpm.model.bpmn.instance.Task.class.isAssignableFrom(flowNode.getClass())
                && CAMUNDA_TASK_TYPE_LIST.contains(flowNode.getElementType().getTypeName())) {
            ServiceTaskImpl serviceTaskImpl = getServiceTask(flowNode);
            AssertUtil.notBlank(serviceTaskImpl.getTaskService(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED,
                    "TaskService cannot be empty! id: {}, fileName: {}", flowNode.getId(), config.getConfigName());
            AssertUtil.notBlank(serviceTaskImpl.getTaskComponent(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED,
                    "TaskComponent cannot be empty! id: {}, fileName: {}", flowNode.getId(), config.getConfigName());
            flowElement = serviceTaskImpl;
        } else if (flowNode instanceof org.camunda.bpm.model.bpmn.instance.ParallelGateway) {
            BasicAsyncFlowElement asyncFlowElement = new BasicAsyncFlowElement();
            fillAsyncProperty(flowNode, asyncFlowElement);
            ParallelGatewayImpl parallelGateway = new ParallelGatewayImpl(asyncFlowElement);
            ElementPropertyUtil.getNodeProperty(flowNode, BpmnConstant.TASK_STRICT_MODE).ifPresent(parallelGateway::setStrictMode);
            flowElement = parallelGateway;
        } else if (flowNode instanceof org.camunda.bpm.model.bpmn.instance.ExclusiveGateway) {
            ServiceTaskImpl serviceTaskImpl = getServiceTask(flowNode);
            flowElement = new ExclusiveGatewayImpl(serviceTaskImpl);
        } else if (flowNode instanceof org.camunda.bpm.model.bpmn.instance.InclusiveGateway) {
            BasicAsyncFlowElement asyncFlowElement = new BasicAsyncFlowElement();
            fillAsyncProperty(flowNode, asyncFlowElement);
            ServiceTaskImpl serviceTaskImpl = getServiceTask(flowNode);
            flowElement = new InclusiveGatewayImpl(asyncFlowElement, serviceTaskImpl);
        } else if (flowNode instanceof org.camunda.bpm.model.bpmn.instance.StartEvent) {
            StartEventImpl startEvent = new StartEventImpl();
            startEvent.setConfig(config);
            flowElement = startEvent;
        } else if (flowNode instanceof org.camunda.bpm.model.bpmn.instance.EndEvent) {
            flowElement = new EndEventImpl();
        } else if (flowNode instanceof org.camunda.bpm.model.bpmn.instance.SubProcess) {
            flowElement = getSubProcess(allSubProcess, config, flowNode.getId());
        } else if (flowNode instanceof org.camunda.bpm.model.bpmn.instance.CallActivity) {
            String calledElementId = ((CallActivity) flowNode).getCalledElement();
            flowElement = getSubProcess(allSubProcess, config, calledElementId);
        } else {
            throw KstryException.buildException(null, ExceptionEnum.CONFIGURATION_UNSUPPORTED_ELEMENT,
                    GlobalUtil.format("{} element: {}, fileName: {}", ExceptionEnum.CONFIGURATION_UNSUPPORTED_ELEMENT.getDesc(),
                            flowNode.getElementType().getTypeName(), config.getConfigName()));
        }
        flowElement.setId(flowNode.getId());
        flowElement.setName(flowNode.getName());
        AssertUtil.notBlank(flowElement.getId(),
                ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "The bpmn element id attribute cannot be empty! fileName: {}", config.getConfigName());
        return flowElement;
    }

    private ServiceTaskImpl getServiceTask(FlowNode flowNode) {
        ServiceTaskImpl serviceTaskImpl = new ServiceTaskImpl();
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnConstant.SERVICE_TASK_TASK_COMPONENT).ifPresent(serviceTaskImpl::setTaskComponent);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnConstant.SERVICE_TASK_TASK_SERVICE).ifPresent(serviceTaskImpl::setTaskService);
        ElementPropertyUtil.getNodeProperty(flowNode,
                BpmnConstant.SERVICE_TASK_CUSTOM_ROLE).flatMap(CustomRoleInfo::buildCustomRole).ifPresent(serviceTaskImpl::setCustomRoleInfo);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnConstant.TASK_ALLOW_ABSENT).ifPresent(serviceTaskImpl::setAllowAbsent);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnConstant.TASK_STRICT_MODE).ifPresent(serviceTaskImpl::setStrictMode);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnConstant.TASK_TIMEOUT)
                .map(s -> NumberUtils.toInt(s, -1)).filter(i -> i >= 0).ifPresent(serviceTaskImpl::setTimeout);
        return serviceTaskImpl;
    }

    private SequenceFlow sequenceFlowMapping(ConfigResource config, org.camunda.bpm.model.bpmn.instance.SequenceFlow sf) {
        SequenceFlowImpl sequenceFlow = new SequenceFlowImpl();
        sequenceFlow.setId(sf.getId());
        sequenceFlow.setName(sf.getName());
        AssertUtil.notBlank(sequenceFlow.getId(),
                ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "The bpmn element id attribute cannot be empty! fileName: {}", config.getConfigName());
        if (sf.getConditionExpression() != null && StringUtils.isNotBlank(sf.getConditionExpression().getTextContent())) {
            SequenceFlowExpression sequenceFlowExpression = new SequenceFlowExpression(sf.getConditionExpression().getTextContent());
            sequenceFlowExpression.setId(sf.getConditionExpression().getId());
            sequenceFlowExpression.setName(sf.getConditionExpression().getTextContent());
            sequenceFlow.setExpression(sequenceFlowExpression);
        }
        return sequenceFlow;
    }

    private BpmnMappingItem getCacheMappingItem(Map<String, BpmnMappingItem> bpmnMappingItemCache,
                                                org.camunda.bpm.model.bpmn.instance.FlowElement camundaElement, FlowElement element) {
        BpmnMappingItem item = bpmnMappingItemCache.get(element.getId());
        if (item == null) {
            item = new BpmnMappingItem(camundaElement, element);
            bpmnMappingItemCache.put(element.getId(), item);
        } else {
            AssertUtil.isTrue(GlobalConstant.ELEMENT_MAX_OCCUR_NUMBER >= item.getOccurNumber(), ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                    "Duplicate calls between elements are not allowed! elementId: {}", item.getKstryElement().getId());
            item.occurNumberIncrement();
        }
        return item;
    }

    private void fillAsyncProperty(FlowNode flowNode, BasicAsyncFlowElement asyncFlowElement) {
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnConstant.ASYNC_ELEMENT_OPEN_ASYNC)
                .map(BooleanUtils::toBoolean).ifPresent(asyncFlowElement::setOpenAsync);
    }

    private FlowElement getSubProcess(Map<String, SubProcess> allSubProcess, ConfigResource config, String calledElementId) {
        AssertUtil.notBlank(calledElementId, ExceptionEnum.CONFIGURATION_SUBPROCESS_ERROR,
                "CallActivity element id cannot be empty!, fileName: {}", config.getConfigName());
        SubProcess subProcess = allSubProcess.get(calledElementId);
        AssertUtil.notNull(subProcess, ExceptionEnum.CONFIGURATION_SUBPROCESS_ERROR,
                "CallActivity element id cannot match to SubProcess instance!, fileName: {}, calledElementId: {}", config.getConfigName(), calledElementId);
        return GlobalUtil.transferNotEmpty(subProcess, SubProcessImpl.class).cloneSubProcess(allSubProcess);
    }

    private SubProcess getKstrySubProcess(ConfigResource config, org.camunda.bpm.model.bpmn.instance.SubProcess sp) {
        SubProcessImpl subProcess = new SubProcessImpl(new StartEventBuilder(config, sp));
        AssertUtil.notBlank(sp.getId());
        subProcess.setId(sp.getId());
        subProcess.setName(sp.getName());
        ElementPropertyUtil.getNodeProperty(sp, BpmnConstant.TASK_STRICT_MODE).ifPresent(subProcess::setStrictMode);
        ElementPropertyUtil.getNodeProperty(sp, BpmnConstant.TASK_TIMEOUT)
                .map(s -> NumberUtils.toInt(s, -1)).filter(i -> i >= 0).ifPresent(subProcess::setTimeout);
        return subProcess;
    }

    public class StartEventBuilder implements Function<Map<String, SubProcess>, StartEvent> {

        private final ConfigResource config;

        private final org.camunda.bpm.model.bpmn.instance.SubProcess sp;

        public StartEventBuilder(ConfigResource config, org.camunda.bpm.model.bpmn.instance.SubProcess sp) {
            this.config = config;
            this.sp = sp;
        }

        @Override
        public StartEvent apply(Map<String, SubProcess> allSubProcess) {
            Collection<org.camunda.bpm.model.bpmn.instance.StartEvent> childElementList =
                    sp.getChildElementsByType(org.camunda.bpm.model.bpmn.instance.StartEvent.class);
            AssertUtil.oneSize(childElementList, ExceptionEnum.CONFIGURATION_SUBPROCESS_ERROR,
                    "SubProcesses are only allowed to also have a start event! fileName: {}", config.getConfigName());
            return doGetKstryModel(allSubProcess, config, childElementList.iterator().next())
                    .orElseThrow(() -> KstryException.buildException(null, ExceptionEnum.CONFIGURATION_SUBPROCESS_ERROR, null));
        }
    }

    private static class BpmnMappingItem {

        private final org.camunda.bpm.model.bpmn.instance.FlowElement camundaElement;

        private final FlowElement kstryElement;

        private int occurNumber = 0;

        public BpmnMappingItem(org.camunda.bpm.model.bpmn.instance.FlowElement camundaElement, FlowElement kstryElement) {
            this.camundaElement = camundaElement;
            this.kstryElement = kstryElement;
            occurNumberIncrement();
        }

        public org.camunda.bpm.model.bpmn.instance.FlowElement getCamundaElement() {
            return camundaElement;
        }

        public FlowElement getKstryElement() {
            return kstryElement;
        }

        public String getId() {
            AssertUtil.notNull(kstryElement, ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY);
            return kstryElement.getId();
        }

        public int getOccurNumber() {
            return occurNumber;
        }

        public void occurNumberIncrement() {
            this.occurNumber++;
        }
    }
}