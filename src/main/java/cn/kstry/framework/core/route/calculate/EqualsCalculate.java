/*
 *
 *  *  Copyright (c) 2020-2020, Lykan (jiashuomeng@gmail.com).
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

import cn.kstry.framework.core.util.InflectionPointCalculate;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @author lykan
 */
public class EqualsCalculate implements InflectionPointCalculate {

    @Override
    public boolean calculate(Object source, Object expected) {
        if (source == null) {
            return expected == null;
        }

        if (expected == null) {
            return false;
        }

        if (NumberUtils.isCreatable(source.toString())) {
            if (!NumberUtils.isCreatable(expected.toString())) {
                return false;
            }
            Number s = NumberUtils.createNumber(source.toString());
            Number t = NumberUtils.createNumber(expected.toString());
            return s.equals(t);
        }

        if (source instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return source.equals(sdf.parse(expected.toString()));
            } catch (ParseException e) {
                return false;
            }
        }
        return Objects.equals(source, expected);
    }
}
