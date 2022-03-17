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
package cn.kstry.framework.core.exception;

import cn.kstry.framework.core.enums.ExceptionTypeEnum;
import cn.kstry.framework.core.util.GlobalUtil;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

/**
 * 异常
 *
 * @author lykan
 */
public class KstryException extends RuntimeException {

    private static final long serialVersionUID = 331496378583084864L;

    private final String errorCode;

    public KstryException(ExceptionEnum exceptionEnum) {
        this(exceptionEnum.getExceptionCode(), exceptionEnum.getDesc(), null);
    }

    public KstryException(String code, String desc, Throwable cause) {
        super(GlobalUtil.format("[{}] {}", StringUtils.isBlank(code)
                ? ExceptionEnum.SYSTEM_ERROR.getExceptionCode() : code, StringUtils.isBlank(desc) ? "System Error!" : desc), cause);
        this.errorCode = code;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public static void throwException(@Nonnull ExceptionEnum exceptionEnum) {
        throwException(exceptionEnum, exceptionEnum.getDesc());
    }

    public static void throwException(@Nonnull ExceptionEnum exceptionEnum, String desc) {
        throwException(null, exceptionEnum, desc);
    }

    public static void throwException(Throwable exception, @Nonnull ExceptionEnum exceptionEnum) {
        throwException(exception, exceptionEnum, exceptionEnum.getDesc());
    }

    public static void throwException(Throwable exception, @Nonnull ExceptionEnum exceptionEnum, String desc) {
        throw buildException(exception, exceptionEnum, desc);
    }

    public static KstryException buildException(@Nonnull ExceptionEnum exceptionEnum) {
        return buildException(null, exceptionEnum, null);
    }

    public static KstryException buildException(@Nonnull ExceptionEnum exceptionEnum, String desc) {
        return buildException(null, exceptionEnum, desc);
    }

    public static KstryException buildException(Throwable exception, @Nonnull ExceptionEnum exceptionEnum, String desc) {
        if (exception instanceof KstryException) {
            return (KstryException) exception;
        }
        if (StringUtils.isBlank(desc)) {
            desc = exceptionEnum.getDesc();
        }
        ExceptionTypeEnum typeEnum = exceptionEnum.getTypeEnum();
        switch (typeEnum) {
            case CONFIG:
                return new ResourceException(exceptionEnum.getExceptionCode(), desc, exception);
            case ASYNC_TASK:
                return new TaskAsyncException(exceptionEnum.getExceptionCode(), desc, exception);
            default:
                return new KstryException(exceptionEnum.getExceptionCode(), desc, exception);
        }
    }
}
