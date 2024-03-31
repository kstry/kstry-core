package cn.kstry.framework.core.bpmn.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
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
public enum IterateStrategyEnum {

    /**
     * 迭代过程中，每一项都必须成功，才能结束迭代
     */
    ALL_SUCCESS("all"),

    /**
     * 迭代过程中，只要有一项能成功，就结束迭代
     */
    ANY_SUCCESS("any"),

    /**
     * 迭代集合中的全部项，执行失败会被忽略，尽量多的拿到成功项。即使全部失败也不会抛出异常
     */
    BEST_SUCCESS("best");

    private final String key;

    IterateStrategyEnum(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static Optional<IterateStrategyEnum> of(String key) {
        if (StringUtils.isBlank(key)) {
            return Optional.empty();
        }
        return Stream.of(values()).filter(e -> Objects.equals(key.trim().toUpperCase(), e.getKey().toUpperCase())).findFirst();
    }
}
