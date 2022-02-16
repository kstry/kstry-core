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
package cn.kstry.framework.core.component.bpmn;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.SequenceFlow;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.impl.*;
import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.constant.BpmnConstant;
import cn.kstry.framework.core.constant.GlobalConstant;
import cn.kstry.framework.core.engine.facade.CustomRoleInfo;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.resource.config.Config;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementPropertyUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author lykan
 */
public class CamundaBpmnModelTransfer implements BpmnModelTransfer {

    /**
     * Camunda 中定义的 ServiceTask、Task 类型常量
     */
    private final List<String> CAMUNDA_TASK_TYPE_LIST = Lists.newArrayList(BpmnModelConstants.BPMN_ELEMENT_SERVICE_TASK, BpmnModelConstants.BPMN_ELEMENT_TASK);

    @Override
    public Optional<StartEvent> getKstryModel(Config config, Object instance, String startId) {

        AssertUtil.notNull(config, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
        if (instance == null || StringUtils.isBlank(startId)) {
            return Optional.empty();
        }

        BpmnModelInstance bpmnModelInstance = GlobalUtil.transferNotEmpty(instance, BpmnModelInstance.class);
        org.camunda.bpm.model.bpmn.instance.StartEvent camundaStartEvent = bpmnModelInstance.getModelElementById(startId);
        return doGetKstryModel(config, bpmnModelInstance, camundaStartEvent);
    }

    private Optional<StartEvent> doGetKstryModel(Config config, BpmnModelInstance bpmnModelInstance,
                                                 org.camunda.bpm.model.bpmn.instance.StartEvent camundaStartEvent) {
        if (camundaStartEvent == null) {
            return Optional.empty();
        }

        StartEvent startEvent = (StartEvent) elementMapping(config, bpmnModelInstance, camundaStartEvent);
        BpmnMappingItem bpmnMappingItem = new BpmnMappingItem(camundaStartEvent, startEvent);

        Map<String, BpmnMappingItem> bpmnMappingItemCache = Maps.newHashMap();
        bpmnMappingItemCache.put(bpmnMappingItem.getId(), bpmnMappingItem);

        Map<FlowElement, Integer> comingCountMap = Maps.newHashMap();
        InStack<BpmnMappingItem> basicInStack = new BasicInStack<>();
        basicInStack.push(bpmnMappingItem);
        while (!basicInStack.isEmpty()) {
            BpmnMappingItem mappingItem = basicInStack.pop().orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
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

                FlowElement kstryNode = elementMapping(config, bpmnModelInstance, targetNode);
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
                KstryException.throwException(ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                        GlobalUtil.format("There is an error in the bpmn file! fileName: {}", config.getConfigName()));
            }
        }

        return Optional.of(startEvent);
    }

    private FlowElement elementMapping(Config config, BpmnModelInstance bpmnModelInstance, FlowNode flowNode) {
        if (flowNode == null) {
            return null;
        }

        FlowElement flowElement = null;
        if (org.camunda.bpm.model.bpmn.instance.Task.class.isAssignableFrom(flowNode.getClass())
                && CAMUNDA_TASK_TYPE_LIST.contains(flowNode.getElementType().getTypeName())) {
            ServiceTaskImpl serviceTaskImpl = getServiceTask(flowNode);
            AssertUtil.notBlank(serviceTaskImpl.getTaskService(),
                    ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "TaskService cannot be empty! fileName: {}", config.getConfigName());
            AssertUtil.notBlank(serviceTaskImpl.getTaskComponent(),
                    ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "TaskComponent cannot be empty! fileName: {}", config.getConfigName());
            flowElement = serviceTaskImpl;
        } else if (flowNode instanceof org.camunda.bpm.model.bpmn.instance.ParallelGateway) {
            BasicAsyncFlowElement asyncFlowElement = new BasicAsyncFlowElement();
            fillAsyncProperty(flowNode, asyncFlowElement);
            ParallelGatewayImpl parallelGateway = new ParallelGatewayImpl(asyncFlowElement);
            ElementPropertyUtil.getNodeProperty(flowNode, BpmnConstant.ELEMENT_STRICT_MODE)
                    .ifPresent(strict -> parallelGateway.setStrictMode(BooleanUtils.isNotFalse(BooleanUtils.toBooleanObject(strict))));
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
            flowElement = buildSubProcess(config, bpmnModelInstance, flowNode);
        } else if (flowNode instanceof org.camunda.bpm.model.bpmn.instance.CallActivity) {
            String calledElementId = ((CallActivity) flowNode).getCalledElement();
            AssertUtil.notBlank(calledElementId, ExceptionEnum.CONFIGURATION_SUBPROCESS_ERROR,
                    "CallActivity element id cannot be empty!, fileName: {}", config.getConfigName());
            org.camunda.bpm.model.bpmn.instance.SubProcess subProcess = bpmnModelInstance.getModelElementById(calledElementId);
            flowElement = buildSubProcess(config, bpmnModelInstance, subProcess);
        } else {
            KstryException.throwException(ExceptionEnum.CONFIGURATION_UNSUPPORTED_ELEMENT,
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
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnConstant.SERVICE_TASK_ALLOW_ABSENT).ifPresent(serviceTaskImpl::setAllowAbsent);
        ElementPropertyUtil.getNodeProperty(flowNode, BpmnConstant.ELEMENT_STRICT_MODE).ifPresent(serviceTaskImpl::setStrictMode);
        ElementPropertyUtil.getNodeProperty(flowNode,
                BpmnConstant.SERVICE_TASK_CUSTOM_ROLE).flatMap(CustomRoleInfo::buildCustomRole).ifPresent(serviceTaskImpl::setCustomRoleInfo);
        return serviceTaskImpl;
    }

    private SequenceFlow sequenceFlowMapping(Config config, org.camunda.bpm.model.bpmn.instance.SequenceFlow sf) {
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

    private FlowElement buildSubProcess(Config config, BpmnModelInstance bpmnModelInstance, ModelElementInstance modelElementInstance) {
        AssertUtil.notNull(modelElementInstance, ExceptionEnum.CONFIGURATION_SUBPROCESS_ERROR,
                "No matching subprocesses found in the configuration file! fileName: {}", config.getConfigName());
        Collection<org.camunda.bpm.model.bpmn.instance.StartEvent> childElement =
                modelElementInstance.getChildElementsByType(org.camunda.bpm.model.bpmn.instance.StartEvent.class);
        AssertUtil.oneSize(childElement, ExceptionEnum.CONFIGURATION_SUBPROCESS_ERROR,
                "Subprocesses are only allowed to also have a start event! fileName: {}", config.getConfigName());
        StartEvent innerStartEvent = doGetKstryModel(config, bpmnModelInstance,
                childElement.iterator().next()).orElseThrow(() -> KstryException.buildException(ExceptionEnum.CONFIGURATION_SUBPROCESS_ERROR));
        return new SubProcessImpl(innerStartEvent);
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
            if (kstryElement == null) {
                KstryException.throwException(ExceptionEnum.SYSTEM_ERROR);
                return null;
            }
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