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

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.SequenceFlow;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.component.bpmn.DiagramTraverseSupport;
import cn.kstry.framework.core.component.expression.ConditionExpression;
import cn.kstry.framework.core.component.expression.ExpressionAliasParser;
import cn.kstry.framework.core.util.GlobalUtil;

import java.util.Optional;

/**
 * 表达式解析器
 *
 * @author lykan
 */
public class ExpressionParserProcessor extends DiagramTraverseSupport<Object> implements StartEventPostProcessor {

    private final ExpressionAliasParser expressionAliasParser;

    public ExpressionParserProcessor(ExpressionAliasParser expressionAliasParser) {
        this.expressionAliasParser = expressionAliasParser;
    }

    @Override
    public Optional<StartEvent> postStartEvent(StartEvent startEvent) {
        traverse(startEvent);
        return Optional.of(startEvent);
    }

    @Override
    public void doPlainElement(Object course, FlowElement node, SubProcess subProcess) {
        if (!(node instanceof SequenceFlow)) {
            return;
        }
        ConditionExpression conditionExpression = GlobalUtil.transferNotEmpty(node, SequenceFlow.class).getConditionExpression().orElse(null);
        if (conditionExpression == null) {
            return;
        }
        conditionExpression.parseConditionExpression(expressionAliasParser);
    }

    @Override
    public int getOrder() {
        return 49;
    }
}
