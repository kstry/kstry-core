/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.component.conversion;

import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;

/**
 * 类型转换接口
 *
 * @author lykan
 */
public interface TypeConverter<S, T> extends Ordered {

    T doConvert(S source, @Nullable Class<?> needType);

    /**
     * 获取源数据类型
     */
    Class<S> getSourceType();

    /**
     * 获取目标数据类型
     */
    Class<T> getTargetType();

    /**
     * 获取转换器名称
     */
    String getConvertName();

    /**
     * 默认的类型转换器匹配规则
     * 使用类型转换器时，如果指定了转换器名字，直接使用指定的转换器不会进行转换器匹配操作，只有未指定转换器名字时才会使用match匹配合适的类型转换器
     *
     * @param collGeneric needType为List、Set时，collGeneric是集合上的泛型
     */
    default boolean match(Object source, Class<?> needType, @Nullable Class<?> collGeneric) {
        if (source == null || needType == null) {
            return false;
        }
        return ElementParserUtil.isAssignable(getSourceType(), source.getClass()) && ElementParserUtil.isAssignable(getTargetType(), needType);
    }

    /**
     * 对外暴露使用的转换方法。
     * 默认不支持同类型转换（同类型转换会返回原值），需要同类型转换时可以覆写该方法实现
     *
     * @param collGeneric needType为List、Set时，collGeneric是集合上的泛型
     */
    @SuppressWarnings("unchecked")
    default T convert(S source, Class<?> needType, Class<?> collGeneric) {
        AssertUtil.notNull(source);
        if (ElementParserUtil.isAssignable(getTargetType(), source.getClass()) && (needType == null || ElementParserUtil.isAssignable(needType, source.getClass()))) {
            return (T) source;
        }
        return doConvert(source, needType);
    }

    /**
     * 同时匹配多个转换器时，使用优先级最高的
     */
    default int getOrder() {
        return 0;
    }
}
