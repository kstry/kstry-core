/*
 *
 *  * Copyright (c) 2020-2022, Lykan (jiashuomeng@gmail.com).
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

import cn.kstry.framework.core.util.ExceptionUtil;

import java.util.function.Consumer;

/**
 * 异常
 *
 * @author lykan
 */
public class KstryException extends RuntimeException {

    private static final long serialVersionUID = 331496378583084864L;

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 是否已经进行了日志记录
     */
    private volatile boolean alreadyLog;

    public KstryException(ExceptionEnum exceptionEnum) {
        this(exceptionEnum.getExceptionCode(), exceptionEnum.getDesc(), null);
    }

    public KstryException(String code, String desc, Throwable cause) {
        super(ExceptionUtil.getExcMessage(code, desc), cause);
        this.errorCode = code;
        this.alreadyLog = false;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void log(Consumer<KstryException> doLog) {
        if (alreadyLog) {
            return;
        }
        alreadyLog = true;
        doLog.accept(this);
    }
}
