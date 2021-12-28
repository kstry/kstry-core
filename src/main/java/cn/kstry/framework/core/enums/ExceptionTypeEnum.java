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

/**
 * 组件 类型 枚举
 *
 * @author lykan
 */
public enum ExceptionTypeEnum {

    /**
     * 全局
     */
    GLOBAL(1),

    /**
     * 注解
     */
    COMPONENT(2),

    /**
     * 配置
     */
    CONFIG(3),

    /**
     * Story 执行期间
     */
    STORY(4),

    /**
     * 属性
     */
    PROPERTY(5),

    /**
     * 异步任务
     */
    ASYNC_TASK(6),
    ;

    ExceptionTypeEnum(int type) {
        this.type = type;
    }

    final int type;

    public int getType() {
        return type;
    }
}
