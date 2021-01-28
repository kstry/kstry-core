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
package cn.kstry.framework.core.enums;

import cn.kstry.framework.core.route.calculate.EqualsCalculator;
import cn.kstry.framework.core.route.calculate.InCalculator;
import cn.kstry.framework.core.route.calculate.NotNullCalculator;
import cn.kstry.framework.core.route.calculate.NumberCompareCalculator;
import cn.kstry.framework.core.route.calculate.StrategyRuleCalculator;

/**
 * @author lykan
 */
public enum CalculatorEnum {

    /**
     * 相等
     */
    EQUALS("equals", new EqualsCalculator()),

    /**
     * 非空
     */
    NOT_NULL("notNull", new NotNullCalculator()),

    /**
     * 数字比较
     */
    COMPARE("compare", new NumberCompareCalculator()),

    /**
     * 目标值，在预期的数组里面则匹配成功
     */
    IN("in", new InCalculator()),

    // END
    ;

    CalculatorEnum(String name, StrategyRuleCalculator expression) {
        this.name = name;
        this.expression = expression;
    }

    private final String name;

    private final StrategyRuleCalculator expression;

    public String getName() {
        return name;
    }

    public StrategyRuleCalculator getExpression() {
        return expression;
    }
}
