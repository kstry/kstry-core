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
package cn.kstry.framework.core.component.bpmn.link;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.SequenceFlow;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.extend.AggregationFlowElement;
import cn.kstry.framework.core.bpmn.impl.SequenceFlowImpl;
import cn.kstry.framework.core.component.bpmn.builder.ExclusiveGatewayBuilder;
import cn.kstry.framework.core.component.bpmn.builder.ServiceTaskBuilder;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessBuilder;
import cn.kstry.framework.core.component.bpmn.joinpoint.DiagramJoinPoint;
import cn.kstry.framework.core.component.bpmn.joinpoint.InclusiveJoinPoint;
import cn.kstry.framework.core.component.bpmn.joinpoint.ParallelJoinPoint;
import cn.kstry.framework.core.component.bpmn.lambda.LambdaServiceSupport;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * BPMN 元素代码方式连接能力
 *
 * @author lykan
 */
public interface ProcessLink extends LambdaServiceSupport {

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
     * @return ProcessLink
     */
    ProcessLink nextTask(ServiceTask serviceTask);

    /**
     * 定义下一个  ServiceTask
     *
     * @param flowExpression 指向 ServiceTask 箭头的条件表达式
     * @param serviceTask    serviceTask
     * @return ProcessLink
     */
    ProcessLink nextTask(String flowExpression, ServiceTask serviceTask);

    /**
     * 定义下一个  ServiceTask
     *
     * @param sequenceFlow 指向 ServiceTask 箭头对象
     * @param serviceTask  serviceTask
     * @return ProcessLink
     */
    ProcessLink nextTask(SequenceFlow sequenceFlow, ServiceTask serviceTask);

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
     * @param sequenceFlow 指向 ServiceTask 箭头对象
     * @param component    对应 @TaskComponent
     * @param service      对应 @TaskService
     * @return ServiceTask Builder
     */
    ServiceTaskBuilder nextTask(SequenceFlow sequenceFlow, String component, String service);

    /**
     * 定义下一个  ServiceTask
     *
     * @param service 对应 @TaskService
     * @return ServiceTask Builder
     */
    ServiceTaskBuilder nextService(String service);

    /**
     * 定义下一个  ServiceTask
     *
     * @param flowExpression 指向 ServiceTask 箭头的条件表达式
     * @param service        对应 @TaskService
     * @return ServiceTask Builder
     */
    ServiceTaskBuilder nextService(String flowExpression, String service);

    /**
     * 定义下一个  ServiceTask
     *
     * @param sequenceFlow 指向 SubProcess 箭头对象
     * @param service      对应 @TaskService
     * @return ServiceTask Builder
     */
    ServiceTaskBuilder nextService(SequenceFlow sequenceFlow, String service);

    /**
     * 定义下一个指令
     *
     * @param instruct 指令
     * @param content  指令内容
     * @return ServiceTask Builder
     */
    ServiceTaskBuilder nextInstruct(String instruct, String content);

    /**
     * 定义下一个指令
     *
     * @param flowExpression 指向指令箭头的条件表达式
     * @param instruct       指令
     * @param content        指令内容
     * @return ServiceTask Builder
     */
    ServiceTaskBuilder nextInstruct(String flowExpression, String instruct, String content);

    /**
     * 定义下一个指令
     *
     * @param sequenceFlow 指向 SubProcess 箭头对象
     * @param instruct     指令
     * @param content      指令内容
     * @return ServiceTask Builder
     */
    ServiceTaskBuilder nextInstruct(SequenceFlow sequenceFlow, String instruct, String content);

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
     * @param sequenceFlow 指向 SubProcess 箭头对象
     * @param processId    子流程Id
     * @return SubProcess Builder
     */
    SubProcessBuilder nextSubProcess(SequenceFlow sequenceFlow, String processId);

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
     * 定义下一个 ExclusiveGateway
     *
     * @param id           指定排他网关Id
     * @param sequenceFlow 指向 ExclusiveGateway 箭头对象
     * @return ExclusiveGatewayBuilder
     */
    ExclusiveGatewayBuilder nextExclusive(String id, SequenceFlow sequenceFlow);

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
     * 定义下一个 InclusiveGateway
     *
     * @param sequenceFlow     指向 inclusiveGateway 箭头对象
     * @param inclusiveGateway inclusiveGateway
     * @return InclusiveJoinPoint
     */
    InclusiveJoinPoint nextInclusive(SequenceFlow sequenceFlow, InclusiveJoinPoint inclusiveGateway);

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
     * 定义下一个 ParallelGateway
     *
     * @param parallelGateway parallelGateway
     * @param sequenceFlow    指向 parallelGateway 箭头对象
     * @return ParallelJoinPoint
     */
    ParallelJoinPoint nextParallel(SequenceFlow sequenceFlow, ParallelJoinPoint parallelGateway);

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
     * 将多个任务分支进行合并
     *
     * @param sequenceFlow     指向 DiagramJoinPoint 箭头对象
     * @param diagramJoinPoint 合并点元素
     */
    <T extends AggregationFlowElement> void joinTask(SequenceFlow sequenceFlow, DiagramJoinPoint<T> diagramJoinPoint);

    /**
     * 结束流程 —— 将当前节点指向结束节点
     */
    void end();

    /**
     * 结束流程 —— 将当前节点指向结束节点
     *
     * @param flowExpression 指向 end 箭头的条件表达式
     */
    void end(String flowExpression);

    /**
     * 结束流程 —— 将当前节点指向结束节点
     *
     * @param sequenceFlow 指向 end 箭头对象
     */
    void end(SequenceFlow sequenceFlow);

    /**
     * 获取关联事件
     *
     * @return 关联事件
     */
    <T extends FlowElement> T getElement();

    /**
     * 获取当前流程
     */
    ProcessLink getProcessLink();

    /**
     * 当前节点被sequenceFlow接成环
     */
    default void byLinkCycle(SequenceFlowImpl sequenceFlow) {
        AssertUtil.notNull(sequenceFlow);
        AssertUtil.isNull(sequenceFlow.getCycleBeginElement());
        List<FlowElement> comingList = sequenceFlow.comingList();
        AssertUtil.isTrue(CollectionUtils.size(comingList) == 1);
        comingList.get(0).outing(sequenceFlow);
        sequenceFlow.outing(GlobalUtil.transferNotEmpty(getProcessLink(), StartDiagramProcessLink.class).getEndEvent());
        sequenceFlow.setCycleBeginElement(getElement());
    }
}
