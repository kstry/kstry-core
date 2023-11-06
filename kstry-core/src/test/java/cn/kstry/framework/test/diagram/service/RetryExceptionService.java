/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.test.diagram.service;

import cn.kstry.framework.core.annotation.Invoke;
import cn.kstry.framework.core.annotation.ReqTaskParam;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.exception.BusinessException;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.exception.TaskTimeoutException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author lykan
 */
@TaskComponent
public class RetryExceptionService {

    // , retry = 3
    @TaskService(invoke = @Invoke(timeout = 100, retryExcludeExp = AExp.class))
    public void simpleExceptionAsync(@ReqTaskParam(reqSelf = true) AtomicInteger req) {
        req.incrementAndGet();
        int i = 1 / 0;
    }

    @TaskService(invoke = @Invoke(retry = 3, retryIncludeExp = BusinessException.class))
    public void simpleException(@ReqTaskParam(reqSelf = true) AtomicInteger req) {
        req.incrementAndGet();
        int i = 1 / 0;
    }

    @TaskService(invoke = @Invoke(timeout = 100, retry = 3, retryExcludeExp = RuntimeException.class))
    public void simpleExceptionAsync1(@ReqTaskParam(reqSelf = true) AtomicInteger req) {
        req.incrementAndGet();
        int i = 1 / 0;
    }

    @TaskService(invoke = @Invoke(retry = 3, retryIncludeExp = AExp.class))
    public void simpleException1(@ReqTaskParam(reqSelf = true) AtomicInteger req) {
        req.incrementAndGet();
        int i = 1 / 0;
    }

    @TaskService(invoke = @Invoke(retry = 3, retryIncludeExp = BusinessException.class, retryExcludeExp = RuntimeException.class))
    public void simpleException2(@ReqTaskParam(reqSelf = true) AtomicInteger req) {
        req.incrementAndGet();
        int i = 1 / 0;
    }

    @TaskService(invoke = @Invoke(timeout = 100, retry = 3, retryIncludeExp = TaskTimeoutException.class))
    public void simpleExceptionAsync2(@ReqTaskParam(reqSelf = true) AtomicInteger req) {
        req.incrementAndGet();
        try {
            TimeUnit.MILLISECONDS.sleep(210);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static class AExp extends BusinessException {

        public AExp(String code, String desc) {
            super(code, desc);
        }
    }
}
