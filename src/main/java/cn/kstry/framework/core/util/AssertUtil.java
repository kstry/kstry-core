/*
 *
 *  *  Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  *  <p>
 *  *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *  <p>
 *  * https://www.gnu.org/licenses/lgpl.html
 *  *  <p>
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 断言工具
 *
 * @author lykan
 */
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
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.SYSTEM_ERROR;
        }
        if (BooleanUtils.isNotTrue(flag)) {
            throwCustomException(exceptionEnum, desc);
        }
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
    public static void equals(Object left, Object right, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.SYSTEM_ERROR;
        }
        if (left == null) {
            AssertUtil.isNull(right, exceptionEnum, desc);
            return;
        }
        AssertUtil.isTrue(left.equals(right), exceptionEnum, desc);
    }

    @SuppressWarnings("all")
    public static void present(Optional<?> optional) {
        present(optional, ExceptionEnum.SYSTEM_ERROR);
    }

    @SuppressWarnings("all")
    public static void present(Optional<?> optional, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.SYSTEM_ERROR;
        }
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
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.OBJ_MUST_EMPTY;
        }
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
        if (object == null) {
            throwCustomException(exceptionEnum, desc);
        }
    }

    /**
     * 字符串不允许为空
     *
     * @param str str
     */
    public static void notBlank(String str) {
        notBlank(str, ExceptionEnum.NOT_ALLOW_EMPTY);
    }

    public static void notBlank(String str, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.NOT_ALLOW_EMPTY;
        }
        isTrue(StringUtils.isNotBlank(str), exceptionEnum, desc);
    }

    public static void notEmpty(Collection<?> collection) {
        notEmpty(collection, ExceptionEnum.COLLECTION_NOT_ALLOW_EMPTY);
    }

    public static void notEmpty(Collection<?> collection, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.COLLECTION_NOT_ALLOW_EMPTY;
        }
        isTrue(CollectionUtils.isNotEmpty(collection), exceptionEnum, desc);
    }

    public static void isEmpty(Collection<?> collection, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.COLLECTION_NOT_ALLOW_EMPTY;
        }
        isTrue(CollectionUtils.isEmpty(collection), exceptionEnum, desc);
    }

    public static <T> void notEmpty(T[] array) {
        notEmpty(array, ExceptionEnum.COLLECTION_NOT_ALLOW_EMPTY);
    }

    public static <T> void notEmpty(T[] array, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.COLLECTION_NOT_ALLOW_EMPTY;
        }
        isTrue(array != null && array.length > 0, exceptionEnum, desc);
    }

    public static void notEmpty(Map<?, ?> map) {
        notEmpty(map, ExceptionEnum.COLLECTION_NOT_ALLOW_EMPTY);
    }

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

    public static void anyNotBlank(String... strArray) {
        if (strArray == null || strArray.length == 0) {
            return;
        }
        for (String s : strArray) {
            notBlank(s);
        }
    }

    /**
     * 断言：有效字段
     */
    public static void isValidField(String field, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.PARAMS_ERROR;
        }
        isTrue(GlobalUtil.isValidField(field), exceptionEnum, desc);
    }

    public static void oneSize(Collection<?> collection) {
        oneSize(collection, ExceptionEnum.PARAMS_ERROR);
    }

    /**
     * 集合只允许一个元素
     *
     * @param collection 集合
     */
    public static void oneSize(Collection<?> collection, ExceptionEnum exceptionEnum, Object... desc) {
        if (exceptionEnum == null) {
            exceptionEnum = ExceptionEnum.PARAMS_ERROR;
        }
        if (CollectionUtils.isEmpty(collection) || collection.size() != 1) {
            throwCustomException(exceptionEnum, desc);
        }
    }

    private static void throwCustomException(ExceptionEnum exceptionEnum, Object[] desc) {
        String exDesc = exceptionEnum.getDesc();
        if (desc == null || desc.length == 0 || !(desc[0] instanceof String)) {
            KstryException.throwException(exceptionEnum.getExceptionCode(), exDesc);
        }

        if (desc.length == 1) {
            exDesc = (String) desc[0];
            KstryException.throwException(exceptionEnum.getExceptionCode(), exDesc);
        }

        List<String> params = Lists.newLinkedList();
        for (int i = 1; i < desc.length; i++) {
            if (desc[i] instanceof String) {
                params.add((String) desc[i]);
                continue;
            }
            params.add(JSON.toJSONString(desc[i]));
        }
        KstryException.throwException(exceptionEnum.getExceptionCode(), String.format((String) desc[0], params.toArray()));
    }
}
