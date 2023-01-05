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
package cn.kstry.framework.core.engine.future;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

/**
 * 流程片段 Future
 *
 * @author lykan
 */
public class FragmentTaskFuture<T> implements FragmentFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentTaskFuture.class);

    /**
     * 线程池提交的 Future 结果
     */
    protected final Future<T> future;

    /**
     * 任务名
     */
    private final String taskName;

    /**
     * Future 是否被中断
     */
    private boolean taskCancelled;

    public FragmentTaskFuture(Future<T> future, String taskName) {
        AssertUtil.notNull(future);
        AssertUtil.notBlank(taskName);
        this.future = future;
        this.taskName = taskName;
        this.taskCancelled = false;
    }

    @Override
    public boolean cancel(String startEventId) {
        if (taskCancelled) {
            return false;
        }
        taskCancelled = true;
        boolean success = !future.isCancelled() && future.cancel(true);
        if (success) {
            LOGGER.info("[{}] Error occurred. Story task was cancelled! taskName: {}", ExceptionEnum.TASK_CANCELLED.getExceptionCode(), getTaskName());
        }
        return true;
    }

    @Override
    public boolean isCancelled(String startEventId) {
        return taskCancelled;
    }

    @Override
    public String getTaskName() {
        return this.taskName;
    }
}
