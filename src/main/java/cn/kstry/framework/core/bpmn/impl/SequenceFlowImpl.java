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

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.SequenceFlow;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.component.expression.ConditionExpression;
import cn.kstry.framework.core.component.expression.Expression;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * SequenceFlowImpl
 */
public class SequenceFlowImpl extends FlowElementImpl implements SequenceFlow {

    /**
     * Boolean 表达式
     */
    private Expression expression;

    /**
     * 经过该 SequenceFlow 的聚合节点（InclusiveGateway、ParallelGateway、EndEvent）
     */
    private List<FlowElement> endElementList = Lists.newArrayList();

    @Override
    public BpmnTypeEnum getElementType() {
        return BpmnTypeEnum.SEQUENCE_FLOW;
    }

    @Override
    public Optional<ConditionExpression> getConditionExpression() {
        return Optional.ofNullable(this.expression).flatMap(Expression::getConditionExpression);
    }

    /**
     * 设置表达式
     *
     * @param expression 表达式
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void addEndElementList(List<FlowElement> endElementList) {
        if (CollectionUtils.isEmpty(endElementList)) {
            return;
        }
        endElementList.forEach(endElement -> {
            if (this.endElementList.contains(endElement)) {
                return;
            }
            this.endElementList.add(endElement);
        });
    }

    @Override
    public List<FlowElement> getEndElementList() {
        return this.endElementList;
    }

    @Override
    public void immutableEndElement() {
        this.endElementList = ImmutableList.copyOf(this.endElementList);
    }
}
