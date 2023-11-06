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
package cn.kstry.framework.core.component.conversion.converter;

import cn.kstry.framework.core.component.conversion.TypeConverter;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.constant.TypeConverterNames;
import org.springframework.beans.factory.InitializingBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTime2StringTypeConverter implements TypeConverter<LocalDateTime, String>, InitializingBean {

    private DateTimeFormatter dateTimeFormatter;

    @Override
    public void afterPropertiesSet() {
        dateTimeFormatter = DateTimeFormatter.ofPattern(GlobalProperties.TYPE_CONVERTER_DATE_FORMAT);
    }

    @Override
    public Class<LocalDateTime> getSourceType() {
        return LocalDateTime.class;
    }

    @Override
    public Class<String> getTargetType() {
        return String.class;
    }

    @Override
    public String getConvertName() {
        return TypeConverterNames.LOCAL_DATETIME_TO_STRING;
    }

    @Override
    public String doConvert(LocalDateTime source, Class<?> needType) {
        return dateTimeFormatter.format(source);
    }
}
