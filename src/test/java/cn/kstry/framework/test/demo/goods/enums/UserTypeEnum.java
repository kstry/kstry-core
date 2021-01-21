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
package cn.kstry.framework.test.demo.goods.enums;

import java.util.Optional;

/**
 * 用户类型
 *
 * @author lykan
 */
public enum UserTypeEnum {

    /**
     * 客户
     */
    CUSTOMER("CUSTOMER", 1),

    /**
     * 用户
     */
    USER("USER", 2);

    private final String type;

    private final int code;

    UserTypeEnum(String type, int code) {
        this.type = type;
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public int getCode() {
        return code;
    }

    public static Optional<UserTypeEnum> valueOf(int code) {
        for (UserTypeEnum userTypeEnum : values()) {
            if (userTypeEnum.getCode() == code) {
                return Optional.of(userTypeEnum);
            }
        }
        return Optional.empty();
    }
}
