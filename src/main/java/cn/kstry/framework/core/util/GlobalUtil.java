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

import cn.kstry.framework.core.config.GlobalConstant;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * @author lykan
 */
public class GlobalUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalUtil.class);

    /**
     * 获取解析属性值失败，返回该标识
     */
    public static final Object GET_PROPERTY_ERROR_SIGN = new Object();

    private static final List<Character> VALID_FIELD_CHARACTER_LIST = Lists.newArrayList(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_');

    @SuppressWarnings("all")
    public static <T> T notEmpty(Optional<T> optional) {
        if (!optional.isPresent()) {
            KstryException.throwException(ExceptionEnum.NOT_ALLOW_EMPTY);
        }
        return optional.get();
    }

    public static <T> T notNull(T obj) {
        AssertUtil.notNull(obj);
        return obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> transfer(Object source, Class<T> targetClass) {
        AssertUtil.notNull(targetClass);
        if (source == null) {
            return Optional.empty();
        }
        AssertUtil.isTrue(targetClass.isAssignableFrom(source.getClass()), ExceptionEnum.TYPE_TRANSFER_ERROR);
        return Optional.of((T) source);
    }

    public static <T> T transferNotEmpty(Object source, Class<T> targetClass) {
        return notEmpty(transfer(source, targetClass));
    }

    public static boolean isValidField(String fieldStr) {
        if (StringUtils.isBlank(fieldStr)) {
            return false;
        }

        if (GlobalConstant.RESERVED_WORDS_LIST.contains(fieldStr.trim())) {
            return false;
        }
        for (Character c : fieldStr.toCharArray()) {
            if (!VALID_FIELD_CHARACTER_LIST.contains(c)) {
                return false;
            }
        }
        return true;
    }

    public static Optional<Object> getProperty(Object bean, String propertyName) {
        if (bean == null || StringUtils.isBlank(propertyName)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(BeanUtilsBean.getInstance().getPropertyUtils().getProperty(bean, propertyName));
        } catch (Exception e) {
            LOGGER.warn("[{}] BeanUtils Failed to get bean property! propertyName:{}", ExceptionEnum.FAILED_GET_PROPERTY.getExceptionCode(), propertyName, e);
            return Optional.of(GET_PROPERTY_ERROR_SIGN);
        }
    }

    public static void setProperty(Object target, String propertyName, Object value) {
        if (target == null || StringUtils.isBlank(propertyName)) {
            return;
        }

        try {
            BeanUtils.setProperty(target, propertyName, value);
        } catch (Exception e) {
            LOGGER.warn("[{}] BeanUtils Failed to set bean property! target:{}, propertyName:{}, value:{}",
                    ExceptionEnum.FAILED_SET_PROPERTY.getExceptionCode(), JSON.toJSONString(target), propertyName, value, e);
        }
    }
}
