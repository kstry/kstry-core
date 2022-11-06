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

    public BpmnLink nextTask(ServiceTask serviceTask) {
        return nextTask(null, serviceTask);
    }

    public BpmnLink nextTask(String flowExpression, ServiceTask serviceTask) {
        AssertUtil.notNull(serviceTask);
        AssertUtil.isTrue(getBpmnLink() instanceof StartDiagramBpmnLink, ExceptionEnum.BPMN_DIAGRAM_LINK_ERROR);
        if (StringUtils.isBlank(serviceTask.getId())) {
            serviceTask.setId(GlobalUtil.uuid());
        }

        AssertUtil.isTrue(serviceTask.validTask(), ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED);
        SequenceFlowImpl sequenceFlow = expressionSequenceFlow(flowExpression);
        sequenceFlow.outing(serviceTask);
        beforeElement().outing(sequenceFlow);
        return new BpmnElementDiagramLink<>(serviceTask, getBpmnLink());
    }

    public ServiceTaskBuilder nextTask(String flowExpression) {
        return nextTask(getBpmnLink(), flowExpression);
    }

    public ServiceTaskBuilder nextTask(String component, String service) {
        return nextTask(null, component, service);
    }

    public ServiceTaskBuilder nextTask(String flowExpression, String component, String service) {
        ServiceTaskBuilder builder = nextTask(getBpmnLink(), flowExpression);
        builder.component(component);
        builder.service(service);
        return builder;
    }

    public SubProcessBuilder nextSubProcess(String processId) {
        return nextSubProcess(null, processId);
    }

    public SubProcessBuilder nextSubProcess(String flowExpression, String processId) {
        SubProcessImpl subProcess = new SubProcessImpl();
        subProcess.setId(processId);
        SequenceFlowImpl sequenceFlow = expressionSequenceFlow(flowExpression);
        sequenceFlow.outing(subProcess);
        getElement().outing(sequenceFlow);
        return new SubProcessBuilder(subProcess, getBpmnLink());
    }

    public ExclusiveGatewayBuilder nextExclusive() {
        return nextExclusive(null);
    }

    public ExclusiveGatewayBuilder nextExclusive(String flowExpression) {
        ExclusiveGatewayImpl exclusiveGateway = new ExclusiveGatewayImpl();
        exclusiveGateway.setId(GlobalUtil.uuid());

        SequenceFlowImpl sequenceFlow = expressionSequenceFlow(flowExpression);
        sequenceFlow.outing(exclusiveGateway);

        beforeElement().outing(sequenceFlow);
        return new ExclusiveGatewayBuilder(exclusiveGateway, getBpmnLink());
    }

    public InclusiveJoinPoint nextInclusive(InclusiveJoinPoint inclusiveGateway) {
        return nextInclusive(null, inclusiveGateway);
    }

    public InclusiveJoinPoint nextInclusive(String flowExpression, InclusiveJoinPoint inclusiveGateway) {
        SequenceFlowImpl sequenceFlow = expressionSequenceFlow(flowExpression);
        sequenceFlow.outing(inclusiveGateway.getElement());
        beforeElement().outing(sequenceFlow);
        return inclusiveGateway;
    }

    public ParallelJoinPoint nextParallel(ParallelJoinPoint parallelGateway) {
        return nextParallel(null, parallelGateway);
    }

    public ParallelJoinPoint nextParallel(String flowExpression, ParallelJoinPoint parallelGateway) {
        SequenceFlowImpl sequenceFlow = expressionSequenceFlow(flowExpression);
        sequenceFlow.outing(parallelGateway.getElement());
        beforeElement().outing(sequenceFlow);
        return parallelGateway;
    }

    public <T extends AggregationFlowElement> void joinTask(DiagramJoinPoint<T> diagramJoinPoint) {
        joinTask(null, diagramJoinPoint);
    }

    public <T extends AggregationFlowElement> void joinTask(String flowExpression, DiagramJoinPoint<T> diagramJoinPoint) {
        T element = diagramJoinPoint.getElement();
        SequenceFlowImpl sequenceFlow = expressionSequenceFlow(flowExpression);
        getElement().outing(sequenceFlow);
        sequenceFlow.outing(element);
    }

    public void end() {
        joinTask(new EndJoinPoint(getBpmnLink()));
    }

    abstract BpmnLink getBpmnLink();

    abstract <T extends FlowElement> T getElement();

    <T extends FlowElement> T beforeElement() {
        return getElement();
    }

    private ServiceTaskBuilder nextTask(BpmnLink bpmnLink, String expression) {
        AssertUtil.isTrue(bpmnLink instanceof StartDiagramBpmnLink, ExceptionEnum.BPMN_DIAGRAM_LINK_ERROR);

        ServiceTaskImpl serviceTask = new ServiceTaskImpl();
        serviceTask.setId(GlobalUtil.uuid());

        SequenceFlowImpl sequenceFlow = expressionSequenceFlow(expression);
        sequenceFlow.outing(serviceTask);

        beforeElement().outing(sequenceFlow);
        return new ServiceTaskBuilder(serviceTask, bpmnLink);
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
        SequenceFlowExpression sequenceFlowExpression = new SequenceFlowExpression(expression.trim());
        sequenceFlowExpression.setId(GlobalUtil.uuid());
        sequenceFlowExpression.setName(StringUtils.EMPTY);
        sequenceFlow.setExpression(sequenceFlowExpression);
        return sequenceFlow;
    }
}
