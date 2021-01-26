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

import java.util.Optional;

/**
 * 策略类型
 *
 * @author lykan
 */
public enum StrategyTypeEnum {

    /**
     * 匹配成功，执行当前事件，匹配失败跳过当前事件继续执行下一个事件
     */
    FILTER("FILTER"),

    /**
     * 匹配成功，将事件流切至当前事件执行
     */
    MATCH("MATCH"),

    /**
     * 脱离主流程，使用 timeSlot 的形式执行任务
     */
    TIMESLOT("TIMESLOT");

    /**
     * 策略类型
     */
    private final String type;

    StrategyTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static Optional<StrategyTypeEnum> getStrategyTypeEnum(String type) {

        if (StringUtils.isBlank(type)) {
            return Optional.empty();
        }
        for (StrategyTypeEnum typeEnum : StrategyTypeEnum.values()) {
            if (typeEnum.getType().toUpperCase().equals(type.trim().toUpperCase())) {
                return Optional.of(typeEnum);
            }
        }
        return Optional.empty();
    }

    public static boolean isType(String type, StrategyTypeEnum typeEnum) {

        if (typeEnum == null) {
            return false;
        }

        Optional<StrategyTypeEnum> strategyTypeOptional = getStrategyTypeEnum(type);
        return strategyTypeOptional.isPresent() && strategyTypeOptional.get() == typeEnum;
    }
}
