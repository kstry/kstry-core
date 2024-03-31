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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.ExpressionException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
public class ExpressionAliasUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionAliasUtil.class);

    /**
     * 为空
     */
    public static boolean isEmpty(Object obj) {
        return size(obj) == 0L;
    }

    /**
     * 不为空
     */
    public static boolean notEmpty(Object obj) {
        return size(obj) != 0L;
    }

    /**
     * 不相等
     */
    public static boolean notEquals(Object a, Object b) {
        return !Objects.equals(a, b);
    }

    /**
     * 包含
     */
    public static boolean contains(Object obj, Object key) {
        if (obj == null || key == null) {
            return false;
        }
        if (obj instanceof String && key instanceof CharSequence) {
            return obj.toString().contains((CharSequence) key);
        } else if (obj instanceof Map) {
            return ((Map<?, ?>) obj).containsKey(key);
        } else if (obj instanceof Collection) {
            return ((Collection<?>) obj).contains(key);
        } else if (obj instanceof Iterator) {
            Iterator<?> it = (Iterator<?>) obj;
            while (it.hasNext()) {
                Object next = it.next();
                if (Objects.equals(next, key)) {
                    return true;
                }
            }
            return false;
        } else if (obj instanceof Enumeration) {
            Enumeration<?> it = (Enumeration<?>) obj;
            while (it.hasMoreElements()) {
                Object next = it.nextElement();
                if (Objects.equals(next, key)) {
                    return true;
                }
            }
            return false;
        } else if (obj.getClass().isArray()) {
            int arrLength = Array.getLength(obj);
            if (arrLength == 0) {
                return false;
            }
            for (int i = 0; i < arrLength; i++) {
                if (Objects.equals(Array.get(obj, i), key)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 元素数量
     */
    public static long size(Object obj) {
        if (obj == null) {
            return 0L;
        }
        if (obj instanceof String) {
            return obj.toString().length();
        } else if (obj instanceof Map) {
            return ((Map<?, ?>) obj).size();
        } else if (obj instanceof Collection) {
            return ((Collection<?>) obj).size();
        } else if (obj instanceof Object[]) {
            return ((Object[]) obj).length;
        } else if (obj instanceof Iterator) {
            int total = 0;
            Iterator<?> it = (Iterator<?>) obj;
            while (it.hasNext()) {
                total++;
                it.next();
            }
            return total;
        } else if (obj instanceof Enumeration) {
            int total = 0;
            Enumeration<?> it = (Enumeration<?>) obj;
            while (it.hasMoreElements()) {
                total++;
                it.nextElement();
            }
            return total;
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj);
        }
        LOGGER.error("The ExpressionAliasUtil does not support illegal param. param: {}", JSON.toJSONString(obj));
        throw new ExpressionException(ExceptionEnum.EXPRESSION_INVOKE_ERROR, "The ExpressionAliasUtil does not support illegal param.");
    }

    /**
     * 有效Id
     */
    public static boolean validId(Object id) {
        if (id == null) {
            return false;
        } else if (id instanceof String) {
            return StringUtils.isNotBlank(id.toString());
        } else if (NumberUtils.isCreatable(id.toString())) {
            return NumberUtils.toLong(id.toString()) > 0L;
        }
        return false;
    }

    public static boolean anyNull(final Object... values) {
        return !ObjectUtils.allNotNull(values);
    }

    public static String toString(Object param) {
        if (param == null) {
            return StringUtils.EMPTY;
        }
        if (param instanceof String) {
            return (String) param;
        }
        return JSON.toJSONString(param, SerializerFeature.DisableCircularReferenceDetect);
    }
}
