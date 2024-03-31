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

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class String2DateTypeConverter implements TypeConverter<String, Date> {

    @Override
    public Class<String> getSourceType() {
        return String.class;
    }

    @Override
    public Class<Date> getTargetType() {
        return Date.class;
    }

    @Override
    public String getConvertName() {
        return TypeConverterNames.STRING_TO_DATE;
    }

    @Override
    public Date doConvert(String source, @Nullable Class<?> needType) {
        SimpleDateFormat format = new SimpleDateFormat(GlobalProperties.TYPE_CONVERTER_DATE_FORMAT);
        try {
            return format.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
