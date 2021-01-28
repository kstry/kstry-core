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
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 计算规则：source [ '=' | '>' | '<' | '>=' | '<=' ] expected
 *
 * @author lyka
 */
public class NumberCompareCalculator extends ConfigStrategyRuleCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NumberCompareCalculator.class);

    /**
     * 等于
     */
    private static final String EQUALS = "=";

    /**
     * 大于
     */
    private static final String GREATER = ">";

    /**
     * 小于
     */
    private static final String LESS = "<";

    /**
     * 大于 OR 等于
     */
    private static final String GREATER_EQUALS = ">=";

    /**
     * 小于 OR 等于
     */
    private static final String LESS_EQUALS = ">=";

    /**
     * 计算符号
     */
    private static final List<String> CALCULATE_SIGN_LIST = Lists.newArrayList(EQUALS, GREATER, LESS, GREATER_EQUALS, LESS_EQUALS);

    /**
     * 允许的格式 ps：
     * =1
     * =3.3f
     * <=2
     * >=2L
     */
    private static final Pattern CALCULATE_SIGN_PATTERN = Pattern.compile(String.format("^(%s){1}(-|\\+)?\\d+(\\.\\d)?\\d*[fFlLdD]?$",
            StringUtils.join(CALCULATE_SIGN_LIST.stream().map(s -> String.format("(%s)", s)).collect(Collectors.toList()), "|")));

    @Override
    public boolean calculateExpected(Object source, String expected) {
        if (!(source instanceof Number) || StringUtils.isBlank(expected)) {
            return false;
        }

        String expectedNumber = expected;
        for (String sign : CALCULATE_SIGN_LIST) {
            expectedNumber = expectedNumber.replace(sign, StringUtils.EMPTY);
        }
        try {
            Number number = NumberUtils.createNumber(expectedNumber);
            if (source instanceof Integer && number instanceof Integer) {
                return expected.contains(compareInt((int) source, (int) number));
            }
            if (source instanceof Integer && number instanceof Long) {
                return expected.contains(compareLong(((Integer) source).longValue(), (long) number));
            }
            if (source instanceof Long && number instanceof Long) {
                return expected.contains(compareLong((long) source, (long) number));
            }
            if (source instanceof Long && number instanceof Integer) {
                return expected.contains(compareLong((long) source, number.longValue()));
            }
            if (source instanceof Double && number instanceof Double) {
                return expected.contains(compareDouble((double) source, (double) number));
            }
            if (source instanceof Double && number instanceof Float) {
                return expected.contains(compareDouble((double) source, number.doubleValue()));
            }
            if (source instanceof Float && number instanceof Float) {
                return expected.contains(compareFloat((float) source, (float) number));
            }
            if (source instanceof Float && number instanceof Double) {
                return expected.contains(compareDouble(((Float) source).doubleValue(), number.doubleValue()));
            }
        } catch (NumberFormatException e) {
            LOGGER.error("An error occurred while parsing the expected value of the rule! source:{}, expected:{}", source, expected, e);
            return false;
        }
        return false;
    }

    @Override
    public boolean checkExpected(String expected) {
        if (StringUtils.isBlank(expected)) {
            return false;
        }
        return CALCULATE_SIGN_PATTERN.matcher(expected).matches();
    }

    @Override
    public String getName() {
        return CalculatorEnum.COMPARE.getName();
    }

    private String compareInt(int left, int right) {
        if (left == right) {
            return EQUALS;
        }
        return left > right ? GREATER : LESS;
    }

    private String compareLong(long left, long right) {
        if (left == right) {
            return EQUALS;
        }
        return left > right ? GREATER : LESS;
    }

    private String compareFloat(float left, float right) {
        if (left == right) {
            return EQUALS;
        }
        return left > right ? GREATER : LESS;
    }

    private String compareDouble(double left, double right) {
        if (left == right) {
            return EQUALS;
        }
        return left > right ? GREATER : LESS;
    }
}
