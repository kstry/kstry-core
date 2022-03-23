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
package cn.kstry.framework.core.engine;

import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.exception.ExceptionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author lykan
 */
public class AsyncFlowTask extends BasicFlowTask implements Callable<AsyncTaskState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncFlowTask.class);

    private final AsyncPropertyDef asyncPropertyDef;

    private final FlowRegister flowRegister;

    private final String startEventId;

    private final CountDownLatch asyncTaskSwitch = new CountDownLatch(1);

    public AsyncFlowTask(AsyncPropertyDef asyncPropertyDef, FlowRegister flowRegister) {
        super(asyncPropertyDef.getAsyncThreadPool(), asyncPropertyDef.getTaskContainer(), asyncPropertyDef.getParamInitStrategy());
        this.asyncPropertyDef = asyncPropertyDef;
        this.flowRegister = flowRegister;
        this.startEventId = flowRegister.getStartFlowElement().getId();
    }

    @Override
    public AsyncPropertyDef getAsyncPropertyDef() {
        return asyncPropertyDef;
    }

    @Override
    public AsyncTaskState call() {
        MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, flowRegister.getRequestId());
        try {
            LOGGER.debug("async story task starts execution. startId: {}", getStartEventId());
            AsyncTaskCell asyncTaskCell = flowRegister.getAsyncTaskCell();
            if (asyncTaskCell.isCancelled()) {
                LOGGER.info("[{}] Task interrupted. Story task was cancelled! startId: {}",
                        ExceptionEnum.TASK_CANCELLED.getExceptionCode(), flowRegister.getStartFlowElement().getId());
                return AsyncTaskState.ERROR;
            }
            try {
                asyncTaskSwitch.await();
                doExe(asyncPropertyDef.getRole(), asyncPropertyDef.getStoryBus(), this.flowRegister);
                return AsyncTaskState.SUCCESS;
            } catch (Throwable e) {
                asyncTaskCell.errorNotice(e);
                asyncTaskCell.cancel();
                return AsyncTaskState.ERROR;
            }
        } finally {
            MDC.clear();
        }
    }

    public void openSwitch() {
        asyncTaskSwitch.countDown();
    }

    public String getStartEventId() {
        return startEventId;
    }
}
