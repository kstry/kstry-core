/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
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
import java.util.stream.Stream;

/**
 *
 * @author lykan
 */
public enum ScopeTypeEnum {

    /**
     * request
     */
    REQUEST("req"),

    /**
     * 可变变量
     */
    VARIABLE("var"),

    /**
     * 不可变变量，一经赋值，不再发生变更
     */
    STABLE("sta"),

    /**
     * result 域
     */
    RESULT("result"),

    /**
     * empty
     */
    EMPTY("empty");

    /**
     *
     */
    private final String abbr;

    ScopeTypeEnum(String abbr) {
        this.abbr = abbr;
    }

    public static Optional<ScopeTypeEnum> of(String abbr) {
        if (StringUtils.isBlank(abbr)) {
            return Optional.empty();
        }
        return Stream.of(values()).filter(e -> Objects.equals(abbr.toUpperCase(), e.getAbbr().toUpperCase())).findFirst();
    }

    public String getAbbr() {
        return abbr;
    }
}
