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
package cn.kstry.framework.core.component.expression;

import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.util.AssertUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.function.BiPredicate;

/**
 * @author lykan
 */
public class ConditionExpressionImpl implements ConditionExpression {

    /**
     * 原表达式
     */
    private String expression;

    /**
     * 真实参与计算的表达式
     */
    private String conditionExpression;

    /**
     * 需要解析表达式
     */
    private boolean needParserExpression;

    /**
     * 表达式顺序
     */
    private int order;

    /**
     * 计算表达式行为，由具体业务指定
     */
    private final BiPredicate<StoryBus, String> testCondition;

    public ConditionExpressionImpl(BiPredicate<StoryBus, String> testCondition) {
        AssertUtil.notNull(testCondition);
        this.testCondition = testCondition;
    }

    @Override
    public boolean condition(StoryBus storyBus) {
        if (storyBus == null) {
            return false;
        }
        AssertUtil.notBlank(this.conditionExpression);
        return this.testCondition.test(storyBus, this.conditionExpression);
    }

    @Override
    public boolean match(String expression) {
        return false;
    }

    @Override
    public void parserConditionExpression(ExpressionAliasParser aliasParser) {
        if (StringUtils.isNotBlank(this.conditionExpression)) {
            return;
        }
        if (isNeedParserExpression()) {
            this.conditionExpression = aliasParser.parserExpression(expression);
        } else {
            this.conditionExpression = this.expression;
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    public boolean isNeedParserExpression() {
        return needParserExpression;
    }

    /**
     * 创建实际参与工作的表达式对象
     *
     * @param expression 表达式
     * @return 表达式对象
     */
    public ConditionExpression newWorkConditionExpression(String expression, int order, boolean needParserExpression) {
        ConditionExpressionImpl conditionExpression = new ConditionExpressionImpl(this.testCondition);
        conditionExpression.order = order;
        conditionExpression.expression = expression;
        conditionExpression.needParserExpression = needParserExpression;
        return conditionExpression;
    }
}