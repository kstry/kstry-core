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
import cn.kstry.framework.core.util.ElementParserUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.core.Ordered;

import java.util.Collection;

public class BasicTypeConverter implements TypeConverter<Object, Object> {

    private final CollectionGenericTypeConverter collectionGenericTypeConverter;

    public BasicTypeConverter(CollectionGenericTypeConverter collectionGenericTypeConverter) {
        this.collectionGenericTypeConverter = collectionGenericTypeConverter;
    }

    @Override
    public Class<Object> getSourceType() {
        return Object.class;
    }

    @Override
    public Class<Object> getTargetType() {
        return Object.class;
    }

    @Override
    public Object convert(Object source, Class<?> needType, Class<?> collGeneric) {
        AssertUtil.notNull(source);
        if (needType == null || ElementParserUtil.isAssignable(needType, source.getClass())) {
            return source;
        }
        if (source instanceof String && String.class.isAssignableFrom(needType)) {
            return source;
        }
        Object target;
        if (source instanceof String) {
            target = JSON.parseObject((String) source, needType);
        } else if (String.class.isAssignableFrom(needType)) {
            target = JSON.toJSONString(source, SerializerFeature.DisableCircularReferenceDetect);
        } else {
            target = JSON.parseObject(JSON.toJSONString(source, SerializerFeature.DisableCircularReferenceDetect), needType);
        }
        if (!collectionGenericTypeConverter.match(target, needType, collGeneric)) {
            return target;
        }
        return collectionGenericTypeConverter.convert((Collection<?>) target, needType, collGeneric);
    }

    @Override
    public Object doConvert(Object source, Class<?> needType) {
        return null;
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
