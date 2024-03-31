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
package cn.kstry.framework.core.component.conversion.converter;

import cn.kstry.framework.core.component.conversion.TypeConverter;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.constant.TypeConverterNames;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class String2LocalDateTimeTypeConverter implements TypeConverter<String, LocalDateTime>, InitializingBean {

    private DateTimeFormatter dateTimeFormatter;

    @Override
    public void afterPropertiesSet() {
        dateTimeFormatter = DateTimeFormatter.ofPattern(GlobalProperties.TYPE_CONVERTER_DATE_FORMAT);
    }

    @Override
    public LocalDateTime doConvert(String source, @Nullable Class<?> needType) {
        return LocalDateTime.parse(source, dateTimeFormatter);
    }

    @Override
    public Class<String> getSourceType() {
        return String.class;
    }

    @Override
    public Class<LocalDateTime> getTargetType() {
        return LocalDateTime.class;
    }

    @Override
    public String getConvertName() {
        return TypeConverterNames.STRING_TO_LOCAL_DATETIME;
    }
}
