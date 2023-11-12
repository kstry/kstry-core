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
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * 默认不启用
 */
@SuppressWarnings("rawtypes")
public class OneItem2SetTypeConverter implements TypeConverter<Object, Set> {

    @Override
    public boolean match(Object source, Class<?> needType, Class<?> collGeneric) {
        return false;
    }

    @Override
    public Set<?> doConvert(Object source, @Nullable Class<?> needType) {
        return Sets.newHashSet(source);
    }

    @Override
    public Class<Object> getSourceType() {
        return Object.class;
    }

    @Override
    public Class<Set> getTargetType() {
        return Set.class;
    }

    @Override
    public String getConvertName() {
        return TypeConverterNames.ONE_ITEM_TO_SET;
    }
}
