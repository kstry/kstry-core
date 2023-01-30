/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
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
import cn.kstry.framework.core.bpmn.impl.*;
import cn.kstry.framework.core.component.bpmn.builder.ExclusiveGatewayBuilder;
import cn.kstry.framework.core.component.bpmn.builder.ServiceTaskBuilder;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessBuilder;
import cn.kstry.framework.core.component.bpmn.joinpoint.DiagramJoinPoint;
import cn.kstry.framework.core.component.bpmn.joinpoint.EndJoinPoint;
import cn.kstry.framework.core.component.bpmn.joinpoint.InclusiveJoinPoint;
import cn.kstry.framework.core.component.bpmn.joinpoint.ParallelJoinPoint;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * BPMN 元素代码方式连接能力核心能力支持
 *
 * @author lykan
 */
public abstract class BpmnDiagramLink {

    public ServiceTaskBuilder nextTask() {
        return nextTask(StringUtils.EMPTY);
    }

    public ProcessLink nextTask(ServiceTask serviceTask) {
        return nextTask(StringUtils.EMPTY, serviceTask);
    }

    public ProcessLink nextTask(String flowExpression, ServiceTask serviceTask) {
        return nextTask(expressionSequenceFlow(flowExpression), serviceTask);
    }

    public ServiceTaskBuilder nextTask(String flowExpression) {
        return nextTask(getProcessLink(), expressionSequenceFlow(flowExpression));
    }

    public ServiceTaskBuilder nextTask(String component, String service) {
        return nextTask(StringUtils.EMPTY, component, service);
    }

    public ServiceTaskBuilder nextTask(String flowExpression, String component, String service) {
        ServiceTaskBuilder builder = nextTask(getProcessLink(), expressionSequenceFlow(flowExpression));
        builder.component(component);
        builder.service(service);
        return builder;
    }

    public ProcessLink nextTask(SequenceFlow sequenceFlow, ServiceTask serviceTask) {
        AssertUtil.notNull(serviceTask);
        AssertUtil.isTrue(getProcessLink() instanceof StartDiagramProcessLink, ExceptionEnum.BPMN_DIAGRAM_LINK_ERROR);
        if (StringUtils.isBlank(serviceTask.getId())) {
            serviceTask.setId(GlobalUtil.uuid());
        }
        AssertUtil.isTrue(serviceTask.validTask(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED);
        sequenceFlow.outing(serviceTask);
        beforeElement().outing(sequenceFlow);
        return new BpmnElementDiagramLink<>(serviceTask, getProcessLink());
    }

    public ServiceTaskBuilder nextTask(SequenceFlow sequenceFlow, String component, String service) {
        ServiceTaskBuilder builder = nextTask(getProcessLink(), sequenceFlow);
        builder.component(component);
        builder.service(service);
        return builder;
    }

    public ServiceTaskBuilder nextService(String service) {
        return nextTask(null, service);
    }

    public ServiceTaskBuilder nextService(String flowExpression, String service) {
        return nextTask(flowExpression, null, service);
    }

    public ServiceTaskBuilder nextService(SequenceFlow sequenceFlow, String service) {
        return nextTask(sequenceFlow, null, service);
    }

    public ServiceTaskBuilder nextInstruct(String instruct, String content) {
        return nextInstruct(StringUtils.EMPTY, instruct, content);
    }

    public ServiceTaskBuilder nextInstruct(String flowExpression, String instruct, String content) {
        return nextTask(flowExpression).instruct(instruct).instructContent(content);
    }

    public ServiceTaskBuilder nextInstruct(SequenceFlow sequenceFlow, String instruct, String content) {
        return nextTask(sequenceFlow, null, null).instruct(instruct).instructContent(content);
    }

    public SubProcessBuilder nextSubProcess(String processId) {
        return nextSubProcess(StringUtils.EMPTY, processId);
    }

    public SubProcessBuilder nextSubProcess(String flowExpression, String processId) {
        return nextSubProcess(expressionSequenceFlow(flowExpression), processId);
    }

    public SubProcessBuilder nextSubProcess(SequenceFlow sequenceFlow, String processId) {
        SubProcessImpl subProcess = new SubProcessImpl();
        subProcess.setId(processId);
        sequenceFlow.outing(subProcess);
        getElement().outing(sequenceFlow);
        return new SubProcessBuilder(subProcess, getProcessLink());
    }

    public ExclusiveGatewayBuilder nextExclusive() {
        return nextExclusive(StringUtils.EMPTY);
    }

    public ExclusiveGatewayBuilder nextExclusive(String flowExpression) {
        return nextExclusive(null, expressionSequenceFlow(flowExpression));
    }

    public ExclusiveGatewayBuilder nextExclusive(String id, SequenceFlow sequenceFlow) {
        if (StringUtils.isBlank(id)) {
            id = GlobalUtil.uuid();
        }
        ExclusiveGatewayImpl exclusiveGateway = new ExclusiveGatewayImpl();
        exclusiveGateway.setId(id);
        sequenceFlow.outing(exclusiveGateway);
        beforeElement().outing(sequenceFlow);
        return new ExclusiveGatewayBuilder(exclusiveGateway, getProcessLink());
    }

    public InclusiveJoinPoint nextInclusive(InclusiveJoinPoint inclusiveGateway) {
        return nextInclusive(StringUtils.EMPTY, inclusiveGateway);
    }

    public InclusiveJoinPoint nextInclusive(String flowExpression, InclusiveJoinPoint inclusiveGateway) {
        return nextInclusive(expressionSequenceFlow(flowExpression), inclusiveGateway);
    }

    public InclusiveJoinPoint nextInclusive(SequenceFlow sequenceFlow, InclusiveJoinPoint inclusiveGateway) {
        sequenceFlow.outing(inclusiveGateway.getElement());
        beforeElement().outing(sequenceFlow);
        return inclusiveGateway;
    }

    public ParallelJoinPoint nextParallel(ParallelJoinPoint parallelGateway) {
        return nextParallel(StringUtils.EMPTY, parallelGateway);
    }

    public ParallelJoinPoint nextParallel(String flowExpression, ParallelJoinPoint parallelGateway) {
        return nextParallel(expressionSequenceFlow(flowExpression), parallelGateway);
    }

    public ParallelJoinPoint nextParallel(SequenceFlow sequenceFlow, ParallelJoinPoint parallelGateway) {
        sequenceFlow.outing(parallelGateway.getElement());
        beforeElement().outing(sequenceFlow);
        return parallelGateway;
    }

    public <T extends AggregationFlowElement> void joinTask(DiagramJoinPoint<T> diagramJoinPoint) {
        joinTask(StringUtils.EMPTY, diagramJoinPoint);
    }

    public <T extends AggregationFlowElement> void joinTask(String flowExpression, DiagramJoinPoint<T> diagramJoinPoint) {
        joinTask(expressionSequenceFlow(flowExpression), diagramJoinPoint);
    }

    public <T extends AggregationFlowElement> void joinTask(SequenceFlow sequenceFlow, DiagramJoinPoint<T> diagramJoinPoint) {
        T element = diagramJoinPoint.getElement();
        getElement().outing(sequenceFlow);
        sequenceFlow.outing(element);
    }

    public void end() {
        end(StringUtils.EMPTY);
    }

    public void end(String flowExpression) {
        joinTask(flowExpression, new EndJoinPoint(getProcessLink()));
    }

    public void end(SequenceFlow sequenceFlow) {
        joinTask(sequenceFlow, new EndJoinPoint(getProcessLink()));
    }

    abstract ProcessLink getProcessLink();

    abstract <T extends FlowElement> T getElement();

    <T extends FlowElement> T beforeElement() {
        return getElement();
    }

    private ServiceTaskBuilder nextTask(ProcessLink processLink, SequenceFlow sequenceFlow) {
        AssertUtil.isTrue(processLink instanceof StartDiagramProcessLink, ExceptionEnum.BPMN_DIAGRAM_LINK_ERROR);
        ServiceTaskImpl serviceTask = new ServiceTaskImpl();
        serviceTask.setId(GlobalUtil.uuid());
        sequenceFlow.outing(serviceTask);
        beforeElement().outing(sequenceFlow);
        return new ServiceTaskBuilder(serviceTask, processLink);
    }

    private SequenceFlowImpl simpleSequenceFlow() {
        SequenceFlowImpl sequenceFlow = new SequenceFlowImpl();
        sequenceFlow.setId(GlobalUtil.uuid());
        sequenceFlow.setName(StringUtils.EMPTY);
        return sequenceFlow;
    }

    private SequenceFlowImpl expressionSequenceFlow(String expression) {
        SequenceFlowImpl sequenceFlow = simpleSequenceFlow();
        if (StringUtils.isBlank(expression)) {
            return sequenceFlow;
        }
        SequenceFlowExpression sequenceFlowExpression = new SequenceFlowExpression(expression);
        sequenceFlowExpression.setId(GlobalUtil.uuid());
        sequenceFlowExpression.setName(StringUtils.EMPTY);
        sequenceFlow.setExpression(sequenceFlowExpression);
        return sequenceFlow;
    }
}
