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

import cn.kstry.framework.core.enums.CalculatorEnum;
import cn.kstry.framework.core.route.StrategyRuleCalculator;

/**
 * 计算规则： source != null
 *
 * @author lykan
 */
public class NotNullCalculator implements StrategyRuleCalculator {

    @Override
    public boolean calculate(Object source, String expected) {
        return source != null;
    }

    @Override
    public boolean checkExpected(String expected) {
        return true;
    }

    @Override
    public String getCalculatorName() {
        return CalculatorEnum.NOT_NULL.getName();
    }
}
