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
import cn.kstry.framework.core.bpmn.extend.ServiceTaskSupport;
import cn.kstry.framework.core.bpmn.impl.ServiceTaskImpl;
import cn.kstry.framework.core.component.bpmn.DiagramTraverseSupport;
import cn.kstry.framework.core.component.expression.ConditionExpression;
import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.container.component.TaskContainer;
import cn.kstry.framework.core.container.component.TaskServiceDef;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.role.ServiceTaskRole;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 校验流程是否非法
 * - 包含网关、并行网关、结束事件支持多入度，其他元素均不支持多入度
 * - 一个流程中有且仅能有一个结束事件
 *
 * @author lykan
 */
public class VerifyFlowPostProcessor extends DiagramTraverseSupport<Set<EndEvent>> implements StartEventPostProcessor {

    private final TaskContainer taskContainer;

    private final Map<String, List<ServiceNodeResource>> serviceNodeResourceMap;

    public VerifyFlowPostProcessor(ApplicationContext applicationContext) {
        super(start -> Sets.newHashSet(), true);
        Map<String, TaskContainer> taskContainerMap = applicationContext.getBeansOfType(TaskContainer.class);
        AssertUtil.oneSize(taskContainerMap.values());
        this.taskContainer = taskContainerMap.values().iterator().next();

        Map<String, List<ServiceNodeResource>> serviceNodeResourceMap = Maps.newHashMap();
        this.taskContainer.getServiceNodeResource().forEach(resource -> {
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
    public void doAggregationBack(InStack<FlowElement> inStack, Map<FlowElement, Integer> comingCountMap, StartEvent startEvent, FlowElement node) {
        AssertUtil.isTrue(comingCountMap.get(node) < node.comingList().size() && inStack.peek().isPresent(), ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                "Wrong branch in the path of an element! identity: {}, fileName: {}", node.identity(), startEvent.getConfig().map(ConfigResource::getConfigName).orElse(null));

        if (node instanceof InclusiveGateway) {
            Integer completedCount = GlobalUtil.transferNotEmpty(node, InclusiveGateway.class).getCompletedCount();
            if (completedCount != null) {
                AssertUtil.isTrue(completedCount <= node.comingList().size(), ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                        "completed-count cannot be greater than comingList size! completed-count: {}, identity: {}, fileName: {}",
                        completedCount, node.identity(), startEvent.getConfig().map(ConfigResource::getConfigName).orElse(null));
            }
        }
    }

    @Override
    public void doPlainElement(Set<EndEvent> endEventSet, FlowElement node, SubProcess subProcess) {
        if (node instanceof EndEvent) {
            checkCycleCross((EndEvent) node);
            endEventSet.add((EndEvent) node);
        } else {
            AssertUtil.isTrue(CollectionUtils.isNotEmpty(node.outingList()), ExceptionEnum.CONFIGURATION_FLOW_ERROR, "Intermediate break in node flow! identity: {}", node.identity());
        }

        if (node instanceof ServiceTask) {
            fillVerifyTask(GlobalUtil.transferNotEmpty(node, ServiceTaskImpl.class));
        }
        if (node instanceof ServiceTaskSupport) {
            fillVerifyTask(((ServiceTaskSupport) node).getServiceTask().map(s -> GlobalUtil.transferNotEmpty(s, ServiceTaskImpl.class)).orElse(null));
        }
    }

    @Override
    public void doLast(Set<EndEvent> endEventSet, StartEvent startEvent) {
        AssertUtil.oneSize(endEventSet, ExceptionEnum.CONFIGURATION_FLOW_ERROR, "EndEvent must appear and can only appear once! startEventId: {}, fileName: {}",
                startEvent.getId(), startEvent.getConfig().map(ConfigResource::getConfigName).orElse(null));
    }

    @Override
    public int getOrder() {
        return 10;
    }

    private void fillVerifyTask(ServiceTaskImpl serviceTask) {
        if (serviceTask == null) {
            return;
        }

        if (StringUtils.isNotBlank(serviceTask.getTaskInstruct())) {
            verifyInstructService(serviceTask.getTaskInstruct(), serviceTask);
            Optional<ServiceNodeResource> instructResource = taskContainer.getServiceNodeResourceByInstruct(serviceTask.getTaskInstruct());
            if (!instructResource.isPresent()) {
                if (serviceTask.allowAbsent()) {
                    return;
                }
                throw ExceptionUtil.buildException(null, ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED,
                        GlobalUtil.format("TaskInstruct cannot be empty! identity: {}, instruct: {}", serviceTask.identity(), serviceTask.getTaskInstruct()));
            }
            serviceTask.setTaskService(instructResource.get().getServiceName());
            serviceTask.setTaskComponent(instructResource.get().getComponentName());
            ElementParserUtil.tryFillTaskName(serviceTask, serviceNodeResourceMap.get(serviceTask.getTaskService()));
            return;
        }

        AssertUtil.notBlank(serviceTask.getTaskService(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "TaskService cannot be empty! identity: {}", serviceTask.identity());
        List<ServiceNodeResource> serviceNodeResources = serviceNodeResourceMap.get(serviceTask.getTaskService());
        if (StringUtils.isNotBlank(serviceTask.getTaskComponent())) {
            ElementParserUtil.tryFillTaskName(serviceTask, serviceNodeResources);
            checkAllowAbsent(serviceTask);
            return;
        }

        if (CollectionUtils.isEmpty(serviceNodeResources)) {
            if (serviceTask.allowAbsent()) {
                return;
            }
            throw ExceptionUtil.buildException(null, ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, GlobalUtil.format("TaskComponent cannot be empty! identity: {}", serviceTask.identity()));
        }
        List<String> componentNames = serviceNodeResources.stream().map(ServiceNodeResource::getComponentName).distinct().collect(Collectors.toList());
        AssertUtil.isTrue(componentNames.size() == 1 && StringUtils.isNotBlank(componentNames.get(0)), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED,
                "Do not allow multiple or empty TaskComponent to correspond to TaskService! identity: {}, components: {}", serviceTask.identity(), componentNames);
        serviceTask.setTaskComponent(componentNames.get(0));
        ElementParserUtil.tryFillTaskName(serviceTask, serviceNodeResources);
        checkAllowAbsent(serviceTask);
    }

    private void verifyInstructService(String taskInstruct, ServiceTask serviceTask) {
        Optional<ServiceNodeResource> instructResource = taskContainer.getServiceNodeResourceByInstruct(taskInstruct);
        if (!instructResource.isPresent()) {
            return;
        }
        if (StringUtils.isNotBlank(serviceTask.getTaskService())) {
            AssertUtil.equals(instructResource.get().getServiceName(), serviceTask.getTaskService(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED,
                    "The service name of the node must be the same as the name in the resource if it appears! resourceName: {}, identity: {}", instructResource.get().getServiceName(), serviceTask.identity());
        }
        if (StringUtils.isNotBlank(serviceTask.getTaskComponent())) {
            AssertUtil.equals(instructResource.get().getComponentName(), serviceTask.getTaskComponent(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED,
                    "The component name of the node must be the same as the name in the resource if it appears! resourceName: {}, identity: {}", instructResource.get().getComponentName(), serviceTask.identity());
        }
    }

    private void checkAllowAbsent(ServiceTaskImpl serviceTask) {
        if (serviceTask.allowAbsent()) {
            return;
        }
        Optional<TaskServiceDef> taskDefOptional = taskContainer.getTaskServiceDef(serviceTask.getTaskComponent(), serviceTask.getTaskService(), new ServiceTaskRole());
        AssertUtil.isTrue(taskDefOptional.isPresent(), ExceptionEnum.TASK_SERVICE_MATCH_ERROR,
                ExceptionEnum.TASK_SERVICE_MATCH_ERROR.getDesc() + GlobalUtil.format(" service task identity: {}", serviceTask.identity()));
    }

    private void checkCycleCross(EndEvent endEvent) {
        List<SequenceFlow> comingList = endEvent.comingList().stream().map(e -> (SequenceFlow) e).collect(Collectors.toList());
        List<SequenceFlow> needCycleList = comingList.stream().filter(s -> s.getCycleBeginElement() != null).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(needCycleList)) {
            return;
        }

        List<SequenceFlow> comingListCopy = Lists.newArrayList(comingList);
        comingListCopy.removeAll(needCycleList);
        AssertUtil.isTrue(needCycleList.size() == 1, ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                "Only one link loopback is allowed in a process! elementIds: {}", needCycleList.stream().map(SequenceFlow::getId).collect(Collectors.toList()));
        AssertUtil.isTrue(comingListCopy.size() == 1, ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                "In processes where a loopback exists, the end node allows only one incoming line! elementIds: {}", comingListCopy.stream().map(SequenceFlow::getId).collect(Collectors.toList()));
        SequenceFlow endFlow = comingListCopy.get(0);
        List<FlowElement> gatewayList = endFlow.comingList();
        AssertUtil.isTrue(CollectionUtils.size(gatewayList) == 1 && gatewayList.get(0) instanceof ExclusiveGateway, ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                "In processes where a loopback exists, the previous node of the end node must be ExclusiveGateway! error node identity: {}", gatewayList.get(0).identity());
        AssertUtil.isTrue(CollectionUtils.size(gatewayList.get(0).outingList()) == 2, ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                "In processes where a loopback exists, the outflow of ExclusiveGateway after end node must be two! error node identity: {}", gatewayList.get(0).identity());
        List<SequenceFlow> outingList = gatewayList.get(0).outingList().stream().map(e -> (SequenceFlow) e).collect(Collectors.toList());
        String expression1 = outingList.get(0).getConditionExpression().map(ConditionExpression::getPlainExpression).orElse(null);
        String expression2 = outingList.get(1).getConditionExpression().map(ConditionExpression::getPlainExpression).orElse(null);
        AssertUtil.isTrue(!Objects.equals(expression1, expression2), ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                "Loopback expression are not allowed to be the same as end expression in the process! same expression: {}, elementIds: {}",
                expression1, outingList.stream().map(SequenceFlow::getId).collect(Collectors.toList()));

        int num = 50000;
        List<FlowElement> flowElements = Lists.newArrayList(gatewayList.get(0).outingList());
        flowElements.remove(endFlow);
        Set<FlowElement> outingSet = Sets.newHashSet();
        Set<FlowElement> comingSet = Sets.newHashSet();
        InStack<FlowElement> outingStack = new BasicInStack<>();
        InStack<FlowElement> comingStack = new BasicInStack<>();
        outingStack.push(flowElements.get(0));
        comingStack.push(gatewayList.get(0).comingList().get(0));
        while (num-- > 0) {
            FlowElement outing = outingStack.pop().orElse(null);
            FlowElement coming = comingStack.pop().orElse(null);
            if (outing == null && coming == null) {
                break;
            }
            if (outing != null) {
                AssertUtil.notTrue(comingSet.contains(outing),
                        ExceptionEnum.CONFIGURATION_FLOW_ERROR, "Only one link loopback is allowed in a process! elementIds: {}", outing.getId());
                outingSet.add(outing);
                outingStack.pushList(outing.outingList());
            }
            if (coming != null) {
                AssertUtil.notTrue(outingSet.contains(coming),
                        ExceptionEnum.CONFIGURATION_FLOW_ERROR, "Only one link loopback is allowed in a process! elementIds: {}", coming.getId());
                comingSet.add(coming);
                comingStack.pushList(coming.comingList());
            }
        }
        for (FlowElement coming : comingSet) {
            for (FlowElement flowElement : coming.outingList()) {
                AssertUtil.isTrue(comingSet.contains(flowElement) || Objects.equals(gatewayList.get(0), flowElement), ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                        "Processes before and after the circular judgment ExclusiveGateway are not allowed to intersect! elementIds: {}", flowElement.getId());
            }
        }
        for (FlowElement outing : outingSet) {
            if (outing instanceof EndEvent) {
                continue;
            }
            for (FlowElement flowElement : outing.comingList()) {
                AssertUtil.isTrue(outingSet.contains(flowElement) || Objects.equals(gatewayList.get(0), flowElement), ExceptionEnum.CONFIGURATION_FLOW_ERROR,
                        "Processes before and after the circular judgment ExclusiveGateway are not allowed to intersect! elementIds: {}", flowElement.getId());
            }
        }
    }
}
