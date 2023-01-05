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
import cn.kstry.framework.core.util.GlobalUtil;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lykan
 */
public class ConditionExpressionImpl implements ConditionExpression {

    /**
     * 表达式
     */
    private String expression;

    /**
     * 计算表达式行为，由具体业务指定
     */
    private final BiPredicate<StoryBus, String> testCondition;

    private final static Pattern itemPattern = Pattern.compile("\\.[\\w-]+");

    public ConditionExpressionImpl(BiPredicate<StoryBus, String> testCondition) {
        AssertUtil.notNull(testCondition);
        this.testCondition = testCondition;
    }

    @Override
    public boolean condition(StoryBus storyBus) {
        if (storyBus == null) {
            return false;
        }
        AssertUtil.notBlank(this.expression);
        return this.testCondition.test(storyBus, this.expression);
    }

    @Override
    public boolean match(String expression) {
        return false;
    }

    /**
     * 创建实际参与工作的表达式对象
     *
     * @param expression 表达式
     * @return 表达式对象
     */
    public ConditionExpression newWorkConditionExpression(String expression) {
        final String finalExpression = expression;
        Matcher matcher = itemPattern.matcher(finalExpression);
        while (matcher.find()) {
            String group = matcher.group();
            int endIndex = matcher.end();
            for (; endIndex < finalExpression.length(); endIndex++) {
                if (finalExpression.charAt(endIndex) == ' ') {
                    continue;
                }
                if (!Objects.equals(finalExpression.charAt(endIndex), '(')) {
                    expression = expression.replace(group, GlobalUtil.format("['{}']", group.substring(1)));
                }
                break;
            }
        }
        ConditionExpressionImpl conditionExpression = new ConditionExpressionImpl(this.testCondition);
        conditionExpression.expression = expression;
        return conditionExpression;
    }
}