/*
 *
 *  *  Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  *  <p>
 *  *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *  <p>
 *  * https://www.gnu.org/licenses/lgpl.html
 *  *  <p>
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

import java.util.function.BiPredicate;

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
        ConditionExpressionImpl conditionExpression = new ConditionExpressionImpl(this.testCondition);
        conditionExpression.expression = expression;
        return conditionExpression;
    }
}