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
package cn.kstry.framework.core.component.validator;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.ViolationException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;

/**
 *  校验参数
 *
 * @author lykan
 */
public class RequestValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestValidator.class);

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public static void validate(Object request) {
        if (request == null) {
            return;
        }
        Set<ConstraintViolation<Object>> validateResultSet = new HashSet<>(VALIDATOR.validate(request));
        if (CollectionUtils.isEmpty(validateResultSet)) {
            return;
        }

        ConstraintViolation<Object> vNode = validateResultSet.iterator().next();
        Path propertyPath = vNode.getPropertyPath();
        ViolationException exception = new ViolationException(ExceptionEnum.PARAM_VERIFICATION_ERROR.getExceptionCode(), vNode.getMessage(), null);
        exception.setFieldName(propertyPath.iterator().next().getName());
        exception.setInvalidValue(vNode.getInvalidValue());
        LOGGER.warn("{}, class: {}, fieldName: {}, invalidValue: {}",
                exception.getMessage(), request.getClass().getName(), exception.getFieldName(), exception.getInvalidValue());
        throw exception;
    }
}
