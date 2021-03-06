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

/**
 * 计算触发条件
 *
 * @author lykan
 */
public interface StrategyRuleCalculator {

    /**
     * 计算
     */
    boolean calculate(Object source, Object expected);

    /**
     * 校验预期值规则
     */
    boolean checkExpected(String expected);

    /**
     * 获取计算规则的名称
     * @return 名称
     */
    String getCalculatorName();
}
