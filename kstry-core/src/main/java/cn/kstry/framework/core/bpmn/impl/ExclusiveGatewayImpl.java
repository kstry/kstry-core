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
package cn.kstry.framework.core.bpmn.impl;

import cn.kstry.framework.core.bpmn.ExclusiveGateway;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.component.expression.ConditionExpression;
import cn.kstry.framework.core.component.expression.Expression;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;

import java.util.Optional;

/**
 * ExclusiveGatewayImpl
 */
public class ExclusiveGatewayImpl extends GatewayImpl implements ExclusiveGateway {

    /**
     * 支持定义 ServiceTask
     */
    private ServiceTask serviceTask;

    /**
     * Boolean 表达式
     */
    private Expression expression;

    public ExclusiveGatewayImpl() {

    }

    @Override
    public BpmnTypeEnum getElementType() {
        return BpmnTypeEnum.EXCLUSIVE_GATEWAY;
    }

    public void setServiceTask(ServiceTask serviceTask) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        if (serviceTask != null && serviceTask.validTask()) {
            this.serviceTask = serviceTask;
        }
    }

    @Override
    public Optional<ServiceTask> getServiceTask() {
        return Optional.ofNullable(serviceTask);
    }

    @Override
    public Optional<ConditionExpression> getConditionExpression() {
        return Optional.ofNullable(this.expression).flatMap(Expression::getConditionExpression);
    }

    public void setExpression(Expression expression) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        this.expression = expression;
    }
}
