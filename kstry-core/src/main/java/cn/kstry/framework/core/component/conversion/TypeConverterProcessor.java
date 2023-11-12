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

import cn.kstry.framework.core.constant.GlobalConstant;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 类型转换处理器
 */
public class TypeConverterProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeConverterProcessor.class);

    private final List<TypeConverter<?, ?>> sortedConverterList;

    private final Map<String, TypeConverter<?, ?>> typeConverterMap = Maps.newHashMap();

    public TypeConverterProcessor(List<TypeConverter<?, ?>> converterList) {
        if (CollectionUtils.isEmpty(converterList)) {
            this.sortedConverterList = Lists.newArrayList();
            return;
        }

        converterList.forEach(converter -> {
            AssertUtil.notNull(converter.getSourceType(), ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY, "SourceType is not allowed to be null. name: {}", converter.getConvertName());
            AssertUtil.notNull(converter.getTargetType(), ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY, "TargetType is not allowed to be null. name: {}", converter.getConvertName());
            AssertUtil.notBlank(converter.getConvertName(), ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY, "ConvertName is not allowed to be blank. name: {}", converter.getConvertName());
            AssertUtil.notTrue(typeConverterMap.containsKey(converter.getConvertName().toUpperCase()),
                    ExceptionEnum.COMPONENT_DUPLICATION_ERROR, "TypeConverter names are not allowed to be duplicated. duplicate name: {}", converter.getConvertName());
            typeConverterMap.put(converter.getConvertName().toUpperCase(), converter);
        });
        OrderComparator.sort(converterList);
        this.sortedConverterList = converterList;
    }

    public <S, T> Pair<String, T> convert(S source, Class<T> targetType) {
        return convert(null, source, targetType);
    }

    public <S, T> Pair<String, T> convert(String convertName, S source) {
        return convert(convertName, source, null);
    }

    public <S, T> Pair<String, T> convert(String convertName, S source, Class<T> targetType) {
        return convert(convertName, source, targetType, null);
    }

    @SuppressWarnings("unchecked")
    public <S, T> Pair<String, T> convert(String convertName, S source, Class<T> targetType, Class<?> collGeneric) {
        if (source == null) {
            return ImmutablePair.nullPair();
        }

        if (StringUtils.isBlank(convertName)) {
            if (targetType == null || Objects.equals(GlobalConstant.VOID, targetType.getName())) {
                return ImmutablePair.of(null, (T) source);
            }
            if (ElementParserUtil.isAssignable(targetType, source.getClass()) && notCollectionTransfer(source, targetType, collGeneric)) {
                return ImmutablePair.of(null, (T) source);
            }
            convertName = getMustMatchedConverter(source, targetType, collGeneric);
            if (StringUtils.isBlank(convertName)) {
                return ImmutablePair.of(null, (T) source);
            }
        }

        Object typeConverterObj = typeConverterMap.get(convertName.toUpperCase());
        if (typeConverterObj == null) {
            return ImmutablePair.of(null, (T) source);
        }

        TypeConverter<Object, Object> typeConverter = (TypeConverter<Object, Object>) typeConverterObj;
        if (!ElementParserUtil.isAssignable(typeConverter.getSourceType(), source.getClass())) {
            LOGGER.warn("[{}] The actual specified type converter cannot take effect! converter name: {}, actual source type: {}, converter source type: {}",
                    ExceptionEnum.TYPE_TRANSFER_ERROR.getExceptionCode(), typeConverter.getConvertName(), source.getClass(), typeConverter.getSourceType());
            return ImmutablePair.of(null, (T) source);
        }

        if (targetType != null && !ElementParserUtil.isAssignable(typeConverter.getTargetType(), targetType)) {
            LOGGER.warn("[{}] The actual specified type converter cannot take effect! converter name: {}, actual target type: {}, converter target type: {}",
                    ExceptionEnum.TYPE_TRANSFER_ERROR.getExceptionCode(), typeConverter.getConvertName(), targetType, typeConverter.getTargetType());
            return ImmutablePair.of(null, (T) source);
        }
        try {
            return ImmutablePair.of(convertName, (T) Optional.of(source).map(s -> typeConverter.convert(s, targetType, collGeneric)).orElse(null));
        } catch (Exception e) {
            throw ExceptionUtil.buildException(e, ExceptionEnum.TYPE_TRANSFER_ERROR,
                    GlobalUtil.format("TypeConverterProcessor converter exception. converter name: {}, source: {}, targetType: {}", typeConverter.getConvertName(), source, targetType));
        }
    }

    private <S, T> boolean notCollectionTransfer(S source, Class<T> targetType, Class<?> collGeneric) {
        if (collGeneric == null) {
            return true;
        }
        return !(source instanceof Set || source instanceof List) || !(List.class.isAssignableFrom(targetType) || Set.class.isAssignableFrom(targetType));
    }

    private <S, T> String getMustMatchedConverter(S source, Class<T> targetType, Class<?> collGeneric) {
        List<String> converterNames = sortedConverterList.stream().filter(c -> c.match(source, targetType, collGeneric)).map(TypeConverter::getConvertName).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(converterNames)) {
            return StringUtils.EMPTY;
        }
        if (converterNames.size() > 1) {
            LOGGER.debug("MustMatchedConverter sourceType: {}, targetType: {}. The name of the converter that can be used is: {}, practical use of the first!",
                    source.getClass().getName(), targetType.getName(), JSON.toJSONString(converterNames));
        }
        return converterNames.get(0);
    }
}
