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
import java.util.stream.Stream;

/**
 *
 * @author lykan
 */
public enum TrackingTypeEnum {

    /**
     * 不开启链路追踪
     */
    NONE,

    /**
     * 全量监控
     */
    ALL,

    /**
     * 监控所有节点，不统计参数获取、参数通知等操作
     */
    NODE,

    /**
     * 统计所有 TaskService 节点，记录开始时间，结束时间，工作线程等
     */
    SERVICE,

    /**
     * 在 SERVICE 的基础上，统计参数获取、参数通知等操作细节
     */
    SERVICE_DETAIL;

    public static TrackingTypeEnum of(String type) {
        if (StringUtils.isBlank(type)) {
            return NONE;
        }
        return Stream.of(values()).filter(e -> Objects.equals(e.name().toLowerCase(), type.toLowerCase())).findFirst().orElse(NONE);
    }

    public boolean needServiceTracking() {
        return this == ALL || this == SERVICE || this == SERVICE_DETAIL;
    }

    public boolean isServiceTracking() {
        return this == SERVICE || this == SERVICE_DETAIL;
    }

    public boolean needServiceDetailTracking() {
        return this == ALL || this == SERVICE_DETAIL;
    }

    public boolean isNone() {
        return this == NONE;
    }
}
