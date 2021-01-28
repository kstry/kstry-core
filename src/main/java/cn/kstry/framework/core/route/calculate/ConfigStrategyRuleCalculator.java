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
package cn.kstry.framework.core.route.calculate;

import cn.kstry.framework.core.config.GlobalConstant;
import cn.kstry.framework.core.util.AssertUtil;

/**
 * 计算触发条件
 *
 * @author lykan
 */
public abstract class ConfigStrategyRuleCalculator implements StrategyRuleCalculator {

    /**
     * 计算
     */
    abstract boolean calculateExpected(Object source, String expected);

    /**
     * 获取规则名称
     */
    abstract String getName();

    @Override
    public boolean calculate(Object source, Object expected) {
        if (expected == null) {
            return false;
        }
        return calculateExpected(source, expected.toString());
    }

    @Override
    public String getCalculatorName() {
        AssertUtil.notBlank(getName());
        return GlobalConstant.KSTRY_STRATEGY_CALCULATOR_NAME_PREFIX + getName();
    }
}
