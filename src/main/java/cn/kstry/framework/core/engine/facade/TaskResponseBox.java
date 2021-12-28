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
package cn.kstry.framework.core.engine.facade;

import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.util.AssertUtil;

/**
 * Task 执行后返回结果
 *
 * @author lykan
 */
public class TaskResponseBox<T> implements TaskResponse<T> {

    private static final long serialVersionUID = -1806941962936550135L;

    /**
     * Task 执行结果
     */
    private boolean success = false;

    /**
     * code
     */
    private String resultCode;

    /**
     * code desc
     */
    private String resultDesc;

    /**
     * exception
     */
    private Throwable resultException;

    /**
     * result
     */
    private T result;

    @Override
    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void resultSuccess(String code) {
        AssertUtil.notBlank(code);
        this.success = true;
        this.resultCode = code;
    }

    public void resultSuccess() {
        resultSuccess(GlobalProperties.STORY_SUCCESS_CODE);
    }

    @Override
    public String getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(String code) {
        this.resultCode = code;
    }

    @Override
    public String getResultDesc() {
        return this.resultDesc;
    }

    public void setResultDesc(String resultDesc) {
        this.resultDesc = resultDesc;
    }

    @Override
    public Throwable getResultException() {
        return this.resultException;
    }

    public void setResultException(Throwable exception) {
        this.resultException = exception;
    }

    @Override
    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public static <E> TaskResponse<E> buildSuccess(E buildTarget) {
        TaskResponseBox<E> result = new TaskResponseBox<>();
        result.resultSuccess();
        result.setResult(buildTarget);
        return result;
    }

    public static <E> TaskResponse<E> buildError(String code, String desc) {
        AssertUtil.notBlank(code);
        TaskResponseBox<E> result = new TaskResponseBox<>();
        result.setResultCode(code);
        result.setResultDesc(desc);
        result.setSuccess(false);
        result.setResult(null);
        return result;
    }
}
