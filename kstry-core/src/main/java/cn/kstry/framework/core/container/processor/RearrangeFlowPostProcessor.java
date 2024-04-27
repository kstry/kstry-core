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
package cn.kstry.framework.core.container.processor;

import cn.kstry.framework.core.bpmn.*;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.bpmn.extend.ServiceTaskSupport;
import cn.kstry.framework.core.bpmn.impl.FlowElementImpl;
import cn.kstry.framework.core.bpmn.impl.InclusiveGatewayImpl;
import cn.kstry.framework.core.bpmn.impl.SequenceFlowImpl;
import cn.kstry.framework.core.bpmn.impl.ServiceTaskImpl;
import cn.kstry.framework.core.component.bpmn.DiagramTraverseSupport;
import cn.kstry.framework.core.container.component.TaskContainer;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 重排链路
 *  - 如果排他网关、包含网关中指定了 TaskService，这个流程会把指定的 TaskService 单独拎出来作为执行节点加入到链路中
 *
 * @author lykan
 */
public class RearrangeFlowPostProcessor extends DiagramTraverseSupport<Object> implements StartEventPostProcessor {

    private final Map<String, List<ServiceNodeResource>> serviceNodeResourceMap;

    public RearrangeFlowPostProcessor(ApplicationContext applicationContext) {
        Map<String, TaskContainer> taskContainerMap = applicationContext.getBeansOfType(TaskContainer.class);
        AssertUtil.oneSize(taskContainerMap.values());
        Map<String, List<ServiceNodeResource>> serviceNodeResourceMap = Maps.newHashMap();
        taskContainerMap.values().iterator().next().getServiceNodeResource().forEach(resource -> {
            List<ServiceNodeResource> serviceNodeResources = serviceNodeResourceMap.computeIfAbsent(resource.getServiceName(), key -> Lists.newArrayList());
            serviceNodeResources.add(resource);
        });
        this.serviceNodeResourceMap = serviceNodeResourceMap;
    }

    @Override
    public Optional<StartEvent> postStartEvent(StartEvent startEvent) {
        traverse(startEvent);
        return Optional.of(startEvent);
    }

    @Override
    public void doPlainElement(Object course, FlowElement node, SubProcess subProcess) {
        if (node instanceof ServiceTaskSupport) {
            Optional<ServiceTask> serviceTaskOptional = ((ServiceTaskSupport) node).getServiceTask();
            serviceTaskOptional.ifPresent(serviceTask -> doSeparateFlowElement(node, serviceTask));
        }
        if (node instanceof ServiceTask) {
            ServiceNodeResource customRoleInfo = ((ServiceTask) node).getCustomRoleInfo();
            // 支持自定义角色属性
            if (customRoleInfo != null) {
                List<FlowElement> outingList = Lists.newArrayList(node.outingList());
                AssertUtil.notEmpty(outingList);
                node.clearOutingChain();

                SequenceFlowImpl sequenceFlow = new SequenceFlowImpl();
                sequenceFlow.setId(GlobalUtil.uuid(BpmnTypeEnum.SEQUENCE_FLOW));
                ServiceTask serviceTask = ServiceTask.builder().service(customRoleInfo.getServiceName()).component(customRoleInfo.getComponentName()).ins();
                ElementParserUtil.tryFillTaskName(GlobalUtil.transferNotEmpty(serviceTask, ServiceTaskImpl.class), serviceNodeResourceMap.get(serviceTask.getTaskService()));
                node.outing(sequenceFlow);
                sequenceFlow.outing(serviceTask);
                outingList.forEach(serviceTask::outing);
            }
        }
    }

    private void doSeparateFlowElement(FlowElement node, ServiceTask serviceTask) {
        AssertUtil.isTrue(node instanceof ServiceTaskSupport);

        FlowElementImpl serviceTaskImpl = GlobalUtil.transferNotEmpty(serviceTask, FlowElementImpl.class);
        serviceTaskImpl.setId(GlobalUtil.uuid(BpmnTypeEnum.SERVICE_TASK, node.getId()));
        serviceTaskImpl.setName(node.getName());

        if (node instanceof InclusiveGateway) {
            List<FlowElement> nodeComingList = node.comingList();
            AssertUtil.isTrue(nodeComingList.size() > 0);

            InclusiveGatewayImpl mockInclusiveGateway = new InclusiveGatewayImpl();
            mockInclusiveGateway.setId(GlobalUtil.uuid(BpmnTypeEnum.INCLUSIVE_GATEWAY, node.getId()));
            Lists.newArrayList(nodeComingList).forEach(flowElement -> {
                FlowElementImpl beforeSequenceFlow = GlobalUtil.transferNotEmpty(flowElement, FlowElementImpl.class);
                beforeSequenceFlow.clearOutingChain();
                beforeSequenceFlow.outing(mockInclusiveGateway);
            });

            SequenceFlowImpl sequenceFlow1 = new SequenceFlowImpl();
            sequenceFlow1.setId(GlobalUtil.uuid(BpmnTypeEnum.SEQUENCE_FLOW));
            mockInclusiveGateway.outing(sequenceFlow1);
            sequenceFlow1.outing(serviceTaskImpl);

            SequenceFlowImpl sequenceFlow2 = new SequenceFlowImpl();
            sequenceFlow2.setId(GlobalUtil.uuid(BpmnTypeEnum.SEQUENCE_FLOW));
            serviceTaskImpl.outing(sequenceFlow2);
            sequenceFlow2.outing(node);

        } else if (node instanceof ExclusiveGateway) {
            List<FlowElement> nodeComingList = node.comingList();
            AssertUtil.oneSize(nodeComingList);

            SequenceFlowImpl sequenceFlow = new SequenceFlowImpl();
            sequenceFlow.setId(GlobalUtil.uuid(BpmnTypeEnum.SEQUENCE_FLOW));

            FlowElementImpl beforeSequenceFlow = GlobalUtil.transferNotEmpty(nodeComingList.get(0), FlowElementImpl.class);
            beforeSequenceFlow.clearOutingChain();
            beforeSequenceFlow.outing(serviceTaskImpl);
            serviceTaskImpl.outing(sequenceFlow);
            sequenceFlow.outing(node);
        } else {
            throw ExceptionUtil.buildException(null, ExceptionEnum.CONFIGURATION_UNSUPPORTED_ELEMENT, "There is a flow analysis that exceeds expectations!");
        }
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
