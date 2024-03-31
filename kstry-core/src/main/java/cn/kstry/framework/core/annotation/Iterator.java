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
package cn.kstry.framework.core.annotation;

import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务节点迭代器
 * <p>
 * 参数与 TaskService 中遍历相关参数相对应，两者同时出现时以 TaskService 中参数配置的属性为准，注解参数将被覆盖
 *
 * @author lykan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface Iterator {

    /**
     * 指定迭代哪个数据域的数据
     */
    ScopeTypeEnum sourceScope() default ScopeTypeEnum.EMPTY;

    /**
     * 指定迭代数据域中的哪个集合变量
     */
    String source() default StringUtils.EMPTY;

    /**
     * 是否使用并发遍历的方式
     */
    boolean async() default false;

    /**
     * 指定迭代策略
     */
    IterateStrategyEnum strategy() default IterateStrategyEnum.ALL_SUCCESS;

    /**
     * 指定迭代每一项的步长，大于1时会进行批量遍历，遍历中每次拿到的将是一个list数组
     */
    int stride() default 1;

    /**
     * 迭代时，是否将返回值、入参两集合中的索引进行一一对应。默认false不进行索引对齐
     * 注意：如果进行一一对齐，返回值集合中的null值将不会被过滤，需要考虑集合中元素出现null的情况
     */
    boolean alignIndex() default false;
}
