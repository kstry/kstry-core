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
import cn.kstry.framework.core.constant.TypeConverterNames;
import cn.kstry.framework.core.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.core.Ordered;

public class BasicTypeConverter implements TypeConverter<Object, Object> {

    @Override
    public Class<Object> getSourceType() {
        return Object.class;
    }

    @Override
    public Class<Object> getTargetType() {
        return Object.class;
    }

    @Override
    public Object doConvert(Object source, Class<?> needType) {
        AssertUtil.notNull(needType);
        if (source instanceof String && String.class.isAssignableFrom(needType)) {
            return source;
        }
        if (source instanceof String) {
            return JSON.parseObject((String) source, needType);
        }
        if (String.class.isAssignableFrom(needType)) {
            return JSON.toJSONString(source, SerializerFeature.DisableCircularReferenceDetect);
        }
        return JSON.parseObject(JSON.toJSONString(source, SerializerFeature.DisableCircularReferenceDetect), needType);
    }

    @Override
    public String getConvertName() {
        return TypeConverterNames.BASIC_CONVERTER;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
