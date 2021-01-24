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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * 计算规则：source == expected
 *
 * @author lykan
 */
public class EqualsCalculator implements StrategyRuleCalculator {

    private final StrategyRuleCalculator numberCompareCalculator = new NumberCompareCalculator();

    @Override
    public boolean calculate(Object source, String expected) {
        if (source == null) {
            return expected == null;
        }

        if (expected == null) {
            return false;
        }

        if (NumberUtils.isCreatable(source.toString())) {
            if (!NumberUtils.isCreatable(expected)) {
                return false;
            }
            return numberCompareCalculator.calculate(source, "=" + expected);
        }

        if (source instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return source.equals(sdf.parse(expected));
            } catch (ParseException e) {
                return false;
            }
        }
        return Objects.equals(source, expected);
    }

    @Override
    public boolean checkExpected(String expected) {
        return StringUtils.isNotBlank(expected);
    }

    @Override
    public String getCalculatorName() {
        return CalculatorEnum.EQUALS.getName();
    }
}
