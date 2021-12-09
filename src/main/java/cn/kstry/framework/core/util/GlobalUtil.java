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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.slf4j.helpers.MessageFormatter;

import com.google.common.collect.Lists;

import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;

/**
 * GlobalUtil
 *
 * @author lykan
 */
public class GlobalUtil {

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

    public static String notBlank(String str) {
        AssertUtil.notBlank(str);
        return str;
    }

    public static String format(String str, Object... params) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        return MessageFormatter.arrayFormat(str, params).getMessage();
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

    public static boolean isCollection(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return Iterable.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz) || clazz.isArray();
    }

    /**
     * 排序后子类在前，父类在后
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> sortObjExtends(Collection<T> objList) {
        if (CollectionUtils.isEmpty(objList)) {
            return Lists.newArrayList();
        }
        Object[] objs = objList.stream().filter(Objects::nonNull).toArray();
        if (objs.length < 2) {
            return Lists.newArrayList(objList);
        }

        boolean isContinue;
        do {
            isContinue = false;
            for (int i = 0; i < objs.length; i++) {
                for (int j = i; j < objs.length; j++) {
                    if (objs[i].getClass() == objs[j].getClass()) {
                        continue;
                    }
                    if (objs[i].getClass().isAssignableFrom(objs[j].getClass())) {
                        Object o = objs[i];
                        objs[i] = objs[j];
                        objs[j] = o;
                        isContinue = true;
                    }
                }
            }
        } while (isContinue);
        return Arrays.stream(objs).map(t -> (T) t).collect(Collectors.toList());
    }

    public static String getOrSetRequestId(StoryRequest<?> storyRequest) {
        if (StringUtils.isNotBlank(storyRequest.getRequestId())) {
            return storyRequest.getRequestId();
        }

        String requestId = MDC.get(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME);
        if (StringUtils.isBlank(requestId)) {
            requestId = UUID.randomUUID().toString();
        }
        storyRequest.setRequestId(requestId);
        return requestId;
    }
}
