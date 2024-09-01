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
package cn.kstry.framework.core.component.validator;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.ViolationException;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 校验参数
 *
 * @author lykan
 */
public class RequestValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestValidator.class);

    private static final List<String> VIOLATION_EXCEPTION_CLASS_NAMES = Lists.newArrayList("jakarta.validation.ConstraintViolationException", "javax.validation.ConstraintViolationException");

    public static Throwable processViolationException(Throwable throwable) {
        if (!VIOLATION_EXCEPTION_CLASS_NAMES.contains(throwable.getClass().getName())) {
            return throwable;
        }
        try {
            Set<?> validateResultSet = (Set<?>) MethodUtils.invokeMethod(throwable, "getConstraintViolations");
            if (CollectionUtils.isEmpty(validateResultSet)) {
                return throwable;
            }
            Object vNode = validateResultSet.iterator().next();
            Optional<Object> leafBeanOptional = GlobalUtil.resOptional(MethodUtils.invokeMethod(vNode, "getLeafBean"));

            ViolationException exception = new ViolationException(ExceptionEnum.PARAM_VERIFICATION_ERROR.getExceptionCode(), String.valueOf(MethodUtils.invokeMethod(vNode, "getMessage")), throwable);
            exception.setLeafBean(leafBeanOptional.orElse(null));

            Object propertyPath = MethodUtils.invokeMethod(vNode, "getPropertyPath");
            if (!(propertyPath instanceof Iterable)) {
                return throwable;
            }
            List<String> fieldPath = Lists.newArrayList();
            for (Object item : ((Iterable<?>) propertyPath)) {
                fieldPath.add(String.valueOf(MethodUtils.invokeMethod(item, "getName")));
            }
            if (CollectionUtils.isNotEmpty(fieldPath)) {
                exception.setFieldPath(fieldPath);
                exception.setFieldName(fieldPath.get(fieldPath.size() - 1));
            }
            exception.setInvalidValue(MethodUtils.invokeMethod(vNode, "getInvalidValue"));
            Object leafClass = leafBeanOptional.map(o -> o.getClass().getName()).orElse(StringUtils.EMPTY);
            exception.log(e -> LOGGER.warn("{}, class: {}, fieldPath: {}, invalidValue: {}", e.getMessage(), leafClass, String.join(".", fieldPath), exception.getInvalidValue(), e));
            return exception;
        } catch (Throwable e) {
            LOGGER.error("[{}] ProcessViolationException failure.", ExceptionEnum.SYSTEM_ERROR.getExceptionCode(), e);
            return throwable;
        }
    }
}
