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
package cn.kstry.framework.core.bpmn.impl;

import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.component.expression.*;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

/**
 * SequenceFlowExpression
 */
public class SequenceFlowExpression extends BaseElementImpl implements Expression {

    private static final List<ConditionExpressionImpl> actualWorkExpressionList =
            Lists.newArrayList(new BooleanConditionExpression(), new RoleConditionExpression(), new SpelConditionExpression());

    /**
     * 表达式
     */
    private final ConditionExpression conditionExpression;

    public SequenceFlowExpression(String expression) {
        AssertUtil.notBlank(expression);
        for (ConditionExpressionImpl cExp : actualWorkExpressionList) {
            if (cExp.match(expression)) {
                conditionExpression = cExp.newWorkConditionExpression(expression);
                return;
            }
        }
        throw KstryException.buildException(ExceptionEnum.SYSTEM_ERROR);
    }

    @Override
    public Optional<ConditionExpression> getConditionExpression() {
        return Optional.of(conditionExpression);
    }

    @Override
    public BpmnTypeEnum getElementType() {
        return BpmnTypeEnum.EXPRESSION;
    }
}
