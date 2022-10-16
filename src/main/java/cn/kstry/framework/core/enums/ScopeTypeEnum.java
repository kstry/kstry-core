/*
 *
 *  * Copyright (c) 2020-2022, Lykan (jiashuomeng@gmail.com).
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
    REQUEST("req", true),

    /**
     * 可变变量
     */
    VARIABLE("var", false),

    /**
     * 不可变变量，一经赋值，不再发生变更
     */
    STABLE("sta", false),

    /**
     * result 域
     */
    RESULT("res", false),

    /**
     * empty
     */
    EMPTY("empty", true);

    /**
     * 键
     */
    private final String key;

    /**
     * 不可被编辑
     */
    private final boolean notEdit;

    ScopeTypeEnum(String key, boolean notEdit) {
        this.key = key;
        this.notEdit = notEdit;
    }

    public static Optional<ScopeTypeEnum> of(String key) {
        if (StringUtils.isBlank(key)) {
            return Optional.empty();
        }
        return Stream.of(values()).filter(e -> Objects.equals(key.trim().toUpperCase(), e.getKey().toUpperCase())).findFirst();
    }

    public String getKey() {
        return key;
    }

    public boolean isNotEdit() {
        return notEdit;
    }
}
