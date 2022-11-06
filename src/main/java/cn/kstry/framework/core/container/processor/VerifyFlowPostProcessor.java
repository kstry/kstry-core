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
package cn.kstry.framework.core.container.processor;

import cn.kstry.framework.core.bpmn.*;
import cn.kstry.framework.core.component.bpmn.DiagramTraverseSupport;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.container.component.TaskContainer;
import cn.kstry.framework.core.container.component.TaskServiceDef;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.role.ServiceTaskRole;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Sets;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 校验流程是否非法
 * - 包含网关、并行网关、结束事件支持多入度，其他元素均不支持多入度
 * - 一个流程中有且仅能有一个结束事件
 *
 * @author lykan
 */
public class VerifyFlowPostProcessor extends DiagramTraverseSupport<Set<EndEvent>> implements StartEventPostProcessor {

    private final TaskContainer taskContainer;

    public VerifyFlowPostProcessor(ApplicationContext applicationContext) {
        super(Sets::newHashSet, true);
        Map<String, TaskContainer> taskContainerMap = applicationContext.getBeansOfType(TaskContainer.class);
        AssertUtil.oneSize(taskContainerMap.values());
        taskContainer = taskContainerMap.values().iterator().next();
    }

    @Override
    public Optional<StartEvent> postStartEvent(StartEvent startEvent) {
        traverse(startEvent);
        return Optional.of(startEvent);
    }

    @Override
    public void doAggregationBack(InStack<FlowElement> inStack, Map<FlowElement, Integer> comingCountMap, StartEvent startEvent, FlowElement node) {
        AssertUtil.isTrue(comingCountMap.get(node) < node.comingList().size() && inStack.peek().isPresent(),
                ExceptionEnum.CONFIGURATION_FLOW_ERROR, "Wrong branch in the path of an element! identity: {}, fileName: {}",
                node.identity(), startEvent.getConfig().map(ConfigResource::getConfigName).orElse(null));
    }

    @Override
    public void doPlainElement(Set<EndEvent> endEventSet, FlowElement node, SubProcess subProcess) {
        if (node instanceof EndEvent) {
            endEventSet.add((EndEvent) node);
        }
        if (node instanceof ServiceTask) {
            ServiceTask serviceTask = GlobalUtil.transferNotEmpty(node, ServiceTask.class);
            if (!serviceTask.allowAbsent()) {
                Optional<TaskServiceDef> taskDefOptional = taskContainer.getTaskServiceDef(serviceTask.getTaskComponent(), serviceTask.getTaskService(), new ServiceTaskRole());
                AssertUtil.isTrue(taskDefOptional.isPresent(), ExceptionEnum.TASK_SERVICE_MATCH_ERROR,
                        ExceptionEnum.TASK_SERVICE_MATCH_ERROR.getDesc() + GlobalUtil.format(" service task identity: {}", serviceTask.identity()));
            }
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
}
