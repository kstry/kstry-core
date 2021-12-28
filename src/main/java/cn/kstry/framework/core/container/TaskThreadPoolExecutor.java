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
package cn.kstry.framework.core.container;

import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.engine.AsyncFlowTask;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author lykan
 */
public class TaskThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskThreadPoolExecutor.class);

    public TaskThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                  TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public static TaskThreadPoolExecutor buildDefaultExecutor() {
        return new TaskThreadPoolExecutor(
                GlobalProperties.THREAD_POOL_CORE_SIZE,
                GlobalProperties.THREAD_POOL_MAX_SIZE,
                GlobalProperties.THREAD_POOL_KEEP_ALIVE_TIME, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(GlobalProperties.KSTRY_THREAD_POOL_QUEUE_SIZE),
                new ThreadFactoryBuilder().setNameFormat("kstry-task-thread-pool-%d").build(),
                (r, executor) -> {
                    if (r instanceof AsyncFlowTask) {
                        AsyncFlowTask asyncFlowTask = GlobalUtil.transferNotEmpty(r, AsyncFlowTask.class);
                        KstryException kstryException = new KstryException(ExceptionEnum.ASYNC_QUEUE_OVERFLOW);
                        LOGGER.error(kstryException.getMessage() + " startId: {}", asyncFlowTask.getStartEventId());
                    }
                }
        );
    }
}
