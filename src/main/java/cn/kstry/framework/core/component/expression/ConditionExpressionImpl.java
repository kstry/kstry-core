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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.Ordered;

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
     * 表达式顺序
     */
    private int order;

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

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * 创建实际参与工作的表达式对象
     *
     * @param expression 表达式
     * @return 表达式对象
     */
    public ConditionExpression newWorkConditionExpression(String expression) {
        Pair<Integer, String> expOrder = parseExpressionOrder(expression);
        expression = expOrder.getRight();
        Matcher matcher = itemPattern.matcher(expOrder.getRight());
        while (matcher.find()) {
            String group = matcher.group();
            int endIndex = matcher.end();
            for (; endIndex < expOrder.getRight().length(); endIndex++) {
                if (expOrder.getRight().charAt(endIndex) == ' ') {
                    continue;
                }
                if (!Objects.equals(expOrder.getRight().charAt(endIndex), '(')) {
                    expression = expression.replace(group, GlobalUtil.format("['{}']", group.substring(1)));
                }
                break;
            }
        }
        ConditionExpressionImpl conditionExpression = new ConditionExpressionImpl(this.testCondition);
        conditionExpression.expression = expression;
        conditionExpression.order = expOrder.getLeft();
        return conditionExpression;
    }

    public Pair<Integer, String> parseExpressionOrder(String expression) {
        String[] split = expression.split(":", 2);
        if (split.length == 1) {
            return ImmutablePair.of(Ordered.LOWEST_PRECEDENCE, expression);
        }
        String left = StringUtils.trim(split[0]);
        String right = StringUtils.trim(split[1]);
        char fChar = left.charAt(0);
        if (fChar != 'o' && fChar != 'O') {
            return ImmutablePair.of(Ordered.LOWEST_PRECEDENCE, expression);
        }
        left = StringUtils.trim(left.substring(1));
        if (!NumberUtils.isCreatable(left)) {
            return ImmutablePair.of(Ordered.LOWEST_PRECEDENCE, expression);
        }
        return ImmutablePair.of(NumberUtils.toInt(left), right);
    }
}