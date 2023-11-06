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

import java.text.SimpleDateFormat;
import java.util.Date;

public class Date2StringTypeConverter implements TypeConverter<Date, String> {

    @Override
    public Class<Date> getSourceType() {
        return Date.class;
    }

    @Override
    public Class<String> getTargetType() {
        return String.class;
    }

    @Override
    public String getConvertName() {
        return TypeConverterNames.DATE_TO_STRING;
    }

    @Override
    public String doConvert(Date source, Class<?> needType) {
        SimpleDateFormat format = new SimpleDateFormat(GlobalProperties.TYPE_CONVERTER_DATE_FORMAT);
        return format.format(source);
    }
}
