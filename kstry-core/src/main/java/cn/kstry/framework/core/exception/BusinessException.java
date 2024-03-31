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
package cn.kstry.framework.core.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * 业务系统使用，业务逻辑发生异常时用来抛出业务异常
 *
 * @author lykan
 */
public class BusinessException extends KstryException {

    /**
     * 任务标识
     */
    private String taskIdentity;

    /**
     * 方法名称
     */
    private String methodName;

    public BusinessException(String code, String desc) {
        this(code, desc, null);
    }

    public BusinessException(String code, String desc, Throwable cause) {
        super(StringUtils.isBlank(code) ? ExceptionEnum.BUSINESS_INVOKE_ERROR.getExceptionCode() : code, desc, cause);
    }

    public String getTaskIdentity() {
        return taskIdentity;
    }

    public void setTaskIdentity(String taskIdentity) {
        this.taskIdentity = taskIdentity;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
