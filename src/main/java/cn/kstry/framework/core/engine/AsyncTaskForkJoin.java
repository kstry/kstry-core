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

import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author lykan
 */
public class AsyncTaskForkJoin extends AsyncPropertyDef {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTaskForkJoin.class);

    public void submitTask(FlowRegister flowRegister) {
        if (flowRegister.getAsyncTaskCell().isCancelled()) {
            LOGGER.info("[{}] Task interrupted. Story task was cancelled! startId: {}",
                    ExceptionEnum.TASK_CANCELLED.getExceptionCode(), flowRegister.getStartFlowElement().getId());
            return;
        }

        AsyncFlowTask asyncFlowTask = new AsyncFlowTask(this, flowRegister);
        ThreadPoolExecutor asyncThreadPool = getAsyncThreadPool();
        AssertUtil.notNull(asyncThreadPool);
        try {
            Future<AsyncTaskState> taskFuture = asyncThreadPool.submit(asyncFlowTask);
            LOGGER.debug("submit async story task. startId: {}", asyncFlowTask.getStartEventId());
            flowRegister.addTaskFuture(taskFuture);
        } finally {
            asyncFlowTask.openSwitch();
        }
    }
}
