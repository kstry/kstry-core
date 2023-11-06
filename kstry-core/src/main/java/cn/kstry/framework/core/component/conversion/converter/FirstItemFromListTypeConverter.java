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
import cn.kstry.framework.core.util.ElementParserUtil;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 取List集合中的首个元素。默认不启用，使用时需要显示指定转换器名
 */
@SuppressWarnings("rawtypes")
public class FirstItemFromListTypeConverter implements TypeConverter<List, Object> {

    @Override
    public boolean match(Object source, Class<?> needType) {
        return false;
    }

    @Override
    public Object doConvert(List source, @Nullable Class<?> needType) {
        if (CollectionUtils.isNotEmpty(source)) {
            return source.get(0);
        }
        if (needType == null) {
            return null;
        }
        return needType.isPrimitive() ? ElementParserUtil.initPrimitive(needType) : null;
    }

    @Override
    public Class<List> getSourceType() {
        return List.class;
    }

    @Override
    public Class<Object> getTargetType() {
        return Object.class;
    }

    @Override
    public String getConvertName() {
        return TypeConverterNames.FIRST_ITEM_FROM_LIST;
    }
}
