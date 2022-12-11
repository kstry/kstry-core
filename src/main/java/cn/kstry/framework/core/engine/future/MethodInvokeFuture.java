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
package cn.kstry.framework.core.engine.future;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 流程片段 Future
 *
 * @author lykan
 */
public class MethodInvokeFuture extends FragmentTaskFuture<Object> implements InvokeFuture {

    public MethodInvokeFuture(Future<Object> future, String taskName) {
        super(future, taskName);
    }

    @Override
    public Object invokeMethod(int timeout) {
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            future.cancel(true);
            throw ExceptionUtil.buildException(e, ExceptionEnum.ASYNC_TASK_INTERRUPTED, GlobalUtil.format("Task interrupted. method invoke was interrupted! taskName: {}", getTaskName()));
        } catch (TimeoutException e) {
            future.cancel(true);
            throw ExceptionUtil.buildException(e, ExceptionEnum.ASYNC_TASK_TIMEOUT, GlobalUtil.format("Async invoke method timeout! taskName: {}, maximum time limit: {}ms", getTaskName(), timeout));
        } catch (Throwable e) {
            future.cancel(true);
            throw ExceptionUtil.buildException(e, ExceptionEnum.SERVICE_INVOKE_ERROR, null);
        }
    }
}
