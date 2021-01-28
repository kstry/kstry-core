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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 目标值，在预期的数组里面则匹配成功
 *
 * @author lykan
 */
public class InCalculator extends ConfigStrategyRuleCalculator {

    @Override
    public boolean calculateExpected(Object source, String expected) {
        if (source == null) {
            return false;
        }
        if (StringUtils.isBlank(expected)) {
            return false;
        }
        List<String> list = JSON.parseObject(expected, new TypeReference<List<String>>() {
        });
        return list.contains(source.toString().trim());
    }

    @Override
    public boolean checkExpected(String expected) {

        if (StringUtils.isBlank(expected)) {
            return false;
        }

        try {
            JSON.parseObject(expected, new TypeReference<List<String>>() {
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return CalculatorEnum.IN.getName();
    }
}
