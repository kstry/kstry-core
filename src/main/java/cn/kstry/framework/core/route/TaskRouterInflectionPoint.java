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
package cn.kstry.framework.core.route;

import cn.kstry.framework.core.util.InflectionPointCalculate;

/**
 * router 定义的可使用的拐点
 *
 * @author lykan
 */
public class TaskRouterInflectionPoint {

    /**
     * DynamicRouteTable 字段名称
     */
    private String fieldName;

    /**
     * 预期值
     */
    private String expectedValue;

    /**
     * 匹配策略
     */
    private InflectionPointCalculate matchingStrategy;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public InflectionPointCalculate getMatchingStrategy() {
        return matchingStrategy;
    }

    public void setMatchingStrategy(InflectionPointCalculate matchingStrategy) {
        this.matchingStrategy = matchingStrategy;
    }
}
