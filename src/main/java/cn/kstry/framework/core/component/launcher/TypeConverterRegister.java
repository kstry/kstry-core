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
package cn.kstry.framework.core.component.launcher;

import cn.kstry.framework.core.component.conversion.TypeConverter;
import cn.kstry.framework.core.component.conversion.converter.*;
import org.springframework.context.annotation.Bean;

public class TypeConverterRegister {

    @Bean
    public TypeConverter<?, ?> getBasicTypeConverter() {
        return new BasicTypeConverter();
    }

    @Bean
    public TypeConverter<?, ?> getDate2StringTypeConverter() {
        return new Date2StringTypeConverter();
    }

    @Bean
    public TypeConverter<?, ?> getString2DateTypeConverter() {
        return new String2DateTypeConverter();
    }

    @Bean
    public TypeConverter<?, ?> getLocalDateTime2StringTypeConverter() {
        return new LocalDateTime2StringTypeConverter();
    }

    @Bean
    public TypeConverter<?, ?> getString2LocalDateTimeTypeConverter() {
        return new String2LocalDateTimeTypeConverter();
    }

    @Bean
    public TypeConverter<?, ?> getOneItem2ListTypeConverter() {
        return new OneItem2ListTypeConverter();
    }

    @Bean
    public TypeConverter<?, ?> getOneItem2SetTypeConverter() {
        return new OneItem2SetTypeConverter();
    }

    @Bean
    public TypeConverter<?, ?> getObject2BooleanTypeConverter() {
        return new Object2BooleanTypeConverter();
    }

    @Bean
    public TypeConverter<?, ?> getFirstItemFromListTypeConverter() {
        return new FirstItemFromListTypeConverter();
    }
}
