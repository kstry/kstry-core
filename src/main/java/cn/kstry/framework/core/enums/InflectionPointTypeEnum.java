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

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * @author lykan
 */
public enum InflectionPointTypeEnum {

    /**
     * 匹配成功，直接执行
     */
    INVOKE,

    /**
     * 匹配成功，判断是否需要执行。若匹配失败，直接跳过该节点，继续执行下一节点
     */
    CONDITION;

    public static Optional<InflectionPointTypeEnum> getInflectionPointTypeEnum(String type) {
        if (StringUtils.isBlank(type)) {
            return Optional.empty();
        }

        for (InflectionPointTypeEnum e : InflectionPointTypeEnum.values()) {
            if (Objects.equals(e.name().toUpperCase(), type.toUpperCase())) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }
}
