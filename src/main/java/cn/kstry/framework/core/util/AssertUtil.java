/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

/**
 * 断言工具
 *
 * @author lykan
 */
@SuppressWarnings("unused")
public class AssertUtil {

    /**
     * 判断条件为 true
     */
    public static void isTrue(Boolean flag) {
        isTrue(flag, ExceptionEnum.SYSTEM_ERROR);
    }

    /**
     * 判断条件为 true
     */
    public static void isTrue(Boolean flag, ExceptionEnum exceptionEnum, Object... desc) {
        if (desc == null || desc.length == 0) {
            isTrue(flag, exceptionEnum, null, null);
        } else if (desc.length == 1) {
            isTrue(flag, exceptionEnum, (String) desc[0], null);
        } else {
            isTrue(flag, exceptionEnum, (String) desc[0], () -> {
                Object[] targetArray = new Object[desc.length - 1];
                System.arraycopy(desc, 1, targetArray, 0, desc.length - 1);
                return Lists.newArrayList(targetArray);
            });
        }
    }

    /**
     * 判断条件为 true
     */
    public static void isTrue(Boolean flag, ExceptionEnum exceptionEnum, String desc, Supplier<List<?>> paramsSupplier) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.SYSTEM_ERROR;
        }
        if (BooleanUtils.isNotTrue(flag)) {
            throwCustomException(exceptionEnum, desc, paramsSupplier);
        }
    }

    /**
     * 判断条件为 非 true
     */
    public static void notTrue(Boolean flag) {
        notTrue(flag, ExceptionEnum.SYSTEM_ERROR);
    }

    /**
     * 判断条件为 非 true
     */
    public static void notTrue(Boolean flag, ExceptionEnum exceptionEnum, Object... desc) {
        isTrue(BooleanUtils.isNotTrue(flag), exceptionEnum, desc);
    }

    /**
     * 判断条件为 equals
     */
    public static void equals(Object left, Object right) {
        equals(left, right, null);
    }

    /**
     * 判断条件为 equals
     */
    public static void equals(Object left, Object right, ExceptionEnum exceptionEnum, Object... desc) {
        AssertUtil.isTrue(Objects.equals(left, right), exceptionEnum, desc);
    }

    @SuppressWarnings("all")
    public static void present(Optional<?> optional) {
        present(optional, ExceptionEnum.SYSTEM_ERROR);
    }

    @SuppressWarnings("all")
    public static void present(Optional<?> optional, ExceptionEnum exceptionEnum, Object... desc) {
        isTrue(optional != null && optional.isPresent(), exceptionEnum, desc);
    }

    /**
     * 必须为空
     */
    public static void isNull(Object object) {
        isNull(object, ExceptionEnum.OBJ_MUST_EMPTY);
    }

    /**
     * 必须为空
     */
    public static void isNull(Object object, ExceptionEnum exceptionEnum, Object... desc) {
        isTrue(object == null, exceptionEnum, desc);
    }

    /**
     * 不允许为空
     *
     * @param object obj
     */
    public static void notNull(Object object) {
        notNull(object, ExceptionEnum.NOT_ALLOW_EMPTY);
    }

    /**
     * 不允许为空
     *
     * @param object obj
     */
    public static void notNull(Object object, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.NOT_ALLOW_EMPTY;
        }
        isTrue(object != null, exceptionEnum, desc);
    }

    /**
     * 字符串不允许为空
     *
     * @param str str
     */
    public static void notBlank(String str) {
        notBlank(str, ExceptionEnum.NOT_ALLOW_EMPTY);
    }

    /**
     * 字符串不允许为空
     *
     * @param str str
     */
    public static void notBlank(String str, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.NOT_ALLOW_EMPTY;
        }
        isTrue(StringUtils.isNotBlank(str), exceptionEnum, desc);
    }

    /**
     * 集合不允许为空
     *
     * @param collection collection
     */
    public static void notEmpty(Collection<?> collection) {
        notEmpty(collection, ExceptionEnum.COLLECTION_NOT_ALLOW_EMPTY);
    }

    /**
     * 集合不允许为空
     *
     * @param collection collection
     */
    public static void notEmpty(Collection<?> collection, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.COLLECTION_NOT_ALLOW_EMPTY;
        }
        isTrue(CollectionUtils.isNotEmpty(collection), exceptionEnum, desc);
    }

    /**
     * 集合必须为空
     *
     * @param collection collection
     */
    public static void isEmpty(Collection<?> collection) {
        isEmpty(collection, ExceptionEnum.SYSTEM_ERROR);
    }

    /**
     * 集合必须为空
     *
     * @param collection collection
     */
    public static void isEmpty(Collection<?> collection, ExceptionEnum exceptionEnum, Object... desc) {
        isTrue(CollectionUtils.isEmpty(collection), exceptionEnum, desc);
    }

    /**
     * 数组不允许为空
     *
     * @param array array
     */
    public static <T> void notEmpty(T[] array) {
        notEmpty(array, ExceptionEnum.COLLECTION_NOT_ALLOW_EMPTY);
    }

    /**
     * 数组不允许为空
     *
     * @param array array
     */
    public static <T> void notEmpty(T[] array, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.COLLECTION_NOT_ALLOW_EMPTY;
        }
        isTrue(array != null && array.length > 0, exceptionEnum, desc);
    }

    /**
     * Map不允许为空
     *
     * @param map map
     */
    public static void notEmpty(Map<?, ?> map) {
        notEmpty(map, ExceptionEnum.COLLECTION_NOT_ALLOW_EMPTY);
    }

    /**
     * Map不允许为空
     *
     * @param map map
     */
    public static void notEmpty(Map<?, ?> map, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.COLLECTION_NOT_ALLOW_EMPTY;
        }
        isTrue(MapUtils.isNotEmpty(map), exceptionEnum, desc);
    }

    /**
     * 不允许为空
     *
     * @param objectArray obj list
     */
    public static void anyNotNull(Object... objectArray) {
        if (objectArray == null || objectArray.length == 0) {
            return;
        }
        for (Object o : objectArray) {
            notNull(o);
        }
    }

    /**
     * 不允许为空
     *
     * @param strArray string list
     */
    public static void anyNotBlank(String... strArray) {
        if (strArray == null || strArray.length == 0) {
            return;
        }
        for (String s : strArray) {
            notBlank(s);
        }
    }

    /**
     * 集合只允许一个元素
     *
     * @param collection 集合
     */
    public static void oneSize(Collection<?> collection, ExceptionEnum exceptionEnum, Object... desc) {
        isTrue(CollectionUtils.isNotEmpty(collection) && collection.size() == 1, exceptionEnum, desc);
    }

    /**
     * 集合只允许一个元素
     *
     * @param collection 集合
     */
    public static void oneSize(Collection<?> collection, ExceptionEnum exceptionEnum, String desc, Supplier<List<?>> paramsSupplier) {
        isTrue(CollectionUtils.isNotEmpty(collection) && collection.size() == 1, exceptionEnum, desc, paramsSupplier);
    }

    /**
     * 集合只允许一个元素
     *
     * @param collection 集合
     */
    public static void oneSize(Collection<?> collection) {
        oneSize(collection, ExceptionEnum.SYSTEM_ERROR);
    }

    private static void throwCustomException(@Nonnull ExceptionEnum exceptionEnum, String desc, Supplier<List<?>> paramsSupplier) {
        if (StringUtils.isBlank(desc)) {
            KstryException.throwException(exceptionEnum, exceptionEnum.getDesc());
            return;
        }

        if (paramsSupplier == null) {
            KstryException.throwException(exceptionEnum, desc);
            return;
        }

        List<?> objList = paramsSupplier.get();
        if (CollectionUtils.isEmpty(objList)) {
            KstryException.throwException(exceptionEnum, desc);
        }

        Object[] params = objList.stream().map(obj -> (obj instanceof String) ? obj : JSON.toJSONString(obj)).toArray();
        KstryException.throwException(exceptionEnum, GlobalUtil.format(desc, params));
    }
}
