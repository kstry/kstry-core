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
package cn.kstry.framework.core.component.bpmn.link;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.extend.AggregationFlowElement;
import cn.kstry.framework.core.component.bpmn.builder.ExclusiveGatewayBuilder;
import cn.kstry.framework.core.component.bpmn.builder.ServiceTaskBuilder;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessBuilder;
import cn.kstry.framework.core.component.bpmn.joinpoint.DiagramJoinPoint;
import cn.kstry.framework.core.component.bpmn.joinpoint.InclusiveJoinPoint;
import cn.kstry.framework.core.component.bpmn.joinpoint.ParallelJoinPoint;

/**
 * BPMN 元素代码方式连接能力
 *
 * @author lykan
 */
public interface BpmnLink {

    /**
     * 定义下一个  ServiceTask
     *
     * @return ServiceTask Builder
     */
    ServiceTaskBuilder nextTask();

    /**
     * 定义下一个  ServiceTask
     *
     * @param serviceTask serviceTask
     * @return BpmnLink
     */
    BpmnLink nextTask(ServiceTask serviceTask);

    /**
     * 定义下一个  ServiceTask
     *
     * @param flowExpression 指向 ServiceTask 箭头的条件表达式
     * @param serviceTask    serviceTask
     * @return BpmnLink
     */
    BpmnLink nextTask(String flowExpression, ServiceTask serviceTask);

    /**
     * 定义下一个  ServiceTask
     *
     * @param flowExpression 指向 ServiceTask 箭头的条件表达式
     * @return ServiceTask Builder
     */
    ServiceTaskBuilder nextTask(String flowExpression);

    /**
     * 定义下一个  ServiceTask
     *
     * @param component 对应 @TaskComponent
     * @param service   对应 @TaskService
     * @return ServiceTask Builder
     */
    ServiceTaskBuilder nextTask(String component, String service);

    /**
     * 定义下一个  ServiceTask
     *
     * @param flowExpression 指向 ServiceTask 箭头的条件表达式
     * @param component      对应 @TaskComponent
     * @param service        对应 @TaskService
     * @return ServiceTask Builder
     */
    ServiceTaskBuilder nextTask(String flowExpression, String component, String service);

    /**
     * 定义下一个子流程
     *
     * @param processId 子流程Id
     * @return SubProcess Builder
     */
    SubProcessBuilder nextSubProcess(String processId);

    /**
     * 定义下一个子流程
     *
     * @param flowExpression 指向 SubProcess 箭头的条件表达式
     * @param processId      子流程Id
     * @return SubProcess Builder
     */
    SubProcessBuilder nextSubProcess(String flowExpression, String processId);

    /**
     * 定义下一个 ExclusiveGateway
     *
     * @return ExclusiveGatewayBuilder
     */
    ExclusiveGatewayBuilder nextExclusive();

    /**
     * 定义下一个 ExclusiveGateway
     *
     * @param flowExpression 指向 ExclusiveGateway 箭头的条件表达式
     * @return ExclusiveGatewayBuilder
     */
    ExclusiveGatewayBuilder nextExclusive(String flowExpression);

    /**
     * 定义下一个 InclusiveGateway
     *
     * @param inclusiveGateway inclusiveGateway
     * @return InclusiveJoinPoint
     */
    InclusiveJoinPoint nextInclusive(InclusiveJoinPoint inclusiveGateway);

    /**
     * 定义下一个 InclusiveGateway
     *
     * @param flowExpression   指向 inclusiveGateway 箭头的条件表达式
     * @param inclusiveGateway inclusiveGateway
     * @return InclusiveJoinPoint
     */
    InclusiveJoinPoint nextInclusive(String flowExpression, InclusiveJoinPoint inclusiveGateway);

    /**
     * 定义下一个 ParallelGateway
     *
     * @param parallelGateway parallelGateway
     * @return ParallelJoinPoint
     */
    ParallelJoinPoint nextParallel(ParallelJoinPoint parallelGateway);

    /**
     * 定义下一个 ParallelGateway
     *
     * @param parallelGateway parallelGateway
     * @param flowExpression  指向 parallelGateway 箭头的条件表达式
     * @return ParallelJoinPoint
     */
    ParallelJoinPoint nextParallel(String flowExpression, ParallelJoinPoint parallelGateway);

    /**
     * 将多个任务分支进行合并
     *
     * @param diagramJoinPoint 合并点元素
     */
    <T extends AggregationFlowElement> void joinTask(DiagramJoinPoint<T> diagramJoinPoint);

    /**
     * 将多个任务分支进行合并
     *
     * @param flowExpression   指向 DiagramJoinPoint 箭头的条件表达式
     * @param diagramJoinPoint 合并点元素
     */
    <T extends AggregationFlowElement> void joinTask(String flowExpression, DiagramJoinPoint<T> diagramJoinPoint);

    /**
     * 结束流程 —— 将当前节点指向结束节点
     */
    void end();

    /**
     * 获取关联事件
     *
     * @return 关联事件
     */
    <T extends FlowElement> T getElement();
}
