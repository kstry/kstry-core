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
package cn.kstry.framework.core.enums;

/**
 * @author lykan
 */
public enum ResourceTypeEnum {

    /**
     * BPMN 流程定义
     */
    BPMN_PROCESS,

    /**
     * JSON流程定义
     */
    JSON_PROCESS,

    /**
     * 变量
     */
    KV_PROPERTIES,

    /**
     * 动态流程
     */
    DYNAMIC_PROCESS,

    /**
     * 代码定义流程
     */
    CODE_PROCESS,

    /**
     * 自定义资源
     */
    CUSTOM,
}
