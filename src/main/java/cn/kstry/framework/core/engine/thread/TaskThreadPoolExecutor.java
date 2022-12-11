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
package cn.kstry.framework.core.engine.thread;

import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.container.ComponentLifecycle;
import cn.kstry.framework.core.engine.FlowRegister;
import cn.kstry.framework.core.engine.future.*;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.enums.ExecutorType;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 任务线程池
 *
 * @author lykan
 */
public class TaskThreadPoolExecutor extends ThreadPoolExecutor implements TaskExecutor, ComponentLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskThreadPoolExecutor.class);

    private final ExecutorType executorType;

    private String prefix;

    public TaskThreadPoolExecutor(ExecutorType executorType, int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                  TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.executorType = executorType;
    }

    public static TaskThreadPoolExecutor buildDefaultExecutor(ExecutorType executorType, String prefix) {
        return buildTaskExecutor(
                executorType,
                prefix,
                GlobalProperties.THREAD_POOL_CORE_SIZE,
                GlobalProperties.THREAD_POOL_MAX_SIZE,
                GlobalProperties.THREAD_POOL_KEEP_ALIVE_TIME, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(GlobalProperties.KSTRY_THREAD_POOL_QUEUE_SIZE),
                new ThreadFactoryBuilder().setNameFormat(prefix + "-%d").build(),
                (r, executor) -> {
                    KstryException kstryException = new KstryException(ExceptionEnum.ASYNC_QUEUE_OVERFLOW);
                    String taskName = r.getClass().getName();
                    if (r instanceof Task) {
                        Task<?> task = GlobalUtil.transferNotEmpty(r, Task.class);
                        taskName = task.getTaskName();
                    }
                    LOGGER.error(kstryException.getMessage() + " taskName: {}", taskName);
                }
        );
    }

    public static TaskThreadPoolExecutor buildTaskExecutor(ExecutorType executorType, String prefix, int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                                           TimeUnit keepAliveTimeUnit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        TaskThreadPoolExecutor poolExecutor = new TaskThreadPoolExecutor(executorType, corePoolSize, maximumPoolSize, keepAliveTime, keepAliveTimeUnit, workQueue, threadFactory, handler);
        poolExecutor.setPrefix(prefix);
        return poolExecutor;
    }

    @Override
    public AdminFuture submitAdminTask(MainFlowTask mainFlowTask) {
        AssertUtil.notNull(mainFlowTask);
        try {
            Future<AsyncTaskState> future = submit(mainFlowTask);
            MainTaskFuture mainTaskFuture = mainFlowTask.buildTaskFuture(future);
            AdminTaskFuture adminTaskFuture = new AdminTaskFuture(mainTaskFuture);
            mainFlowTask.setAdminFuture(adminTaskFuture);
            return adminTaskFuture;
        } finally {
            mainFlowTask.openSwitch();
        }
    }

    @Override
    public void submitFragmentTask(FragmentTask fragmentTask) {
        AssertUtil.notNull(fragmentTask);
        try {
            Future<AsyncTaskState> future = submit(fragmentTask);
            FragmentFuture fragmentFuture = fragmentTask.buildTaskFuture(future);
            FlowRegister flowRegister = fragmentTask.getFlowRegister();
            flowRegister.getAdminFuture().addManagedFuture(fragmentFuture, flowRegister.getStartEventId());
        } finally {
            fragmentTask.openSwitch();
        }
    }

    @Override
    public void submitMonoFlowTask(String parentStartEventId, MonoFlowTask flowTask) {
        AssertUtil.notNull(flowTask);
        try {
            Future<AsyncTaskState> future = submit(flowTask);
            MonoFlowFuture monoFlowFuture = flowTask.buildTaskFuture(future);
            FlowRegister flowRegister = flowTask.getFlowRegister();
            flowRegister.getAdminFuture().addManagedFuture(parentStartEventId, monoFlowFuture, flowRegister.getStartEventId());
        } finally {
            flowTask.openSwitch();
        }
    }

    @Override
    public InvokeFuture submitMethodInvokeTask(MethodInvokeTask methodInvokeTask) {
        AssertUtil.notNull(methodInvokeTask);
        try {
            Future<Object> future = submit(methodInvokeTask);
            InvokeFuture invokeFuture = methodInvokeTask.buildTaskFuture(future);
            FlowRegister flowRegister = methodInvokeTask.getFlowRegister();
            flowRegister.getAdminFuture().addManagedFuture(invokeFuture, flowRegister.getStartEventId());
            return invokeFuture;
        } finally {
            methodInvokeTask.openSwitch();
        }
    }

    @Override
    public void destroy() {
        ThreadPoolExecutor asyncThreadPool = this;
        LOGGER.info("Begin shutdown time slot thread pool! active count: {}", asyncThreadPool.getActiveCount());
        asyncThreadPool.shutdown();
        try {
            TimeUnit.MILLISECONDS.sleep(GlobalProperties.ENGINE_SHUTDOWN_SLEEP_SECONDS);
        } catch (Throwable e) {
            LOGGER.warn("Time slot thread pool close task are interrupted on shutdown!", e);
        }
        if (asyncThreadPool.isShutdown() && asyncThreadPool.getActiveCount() == 0) {
            LOGGER.info("[shutdown] interrupting tasks in the thread pool success! thread pool close success!");
            return;
        } else {
            LOGGER.info("Interrupting tasks in the thread pool that have not yet finished! begin shutdownNow! active count: {}",
                    asyncThreadPool.getActiveCount());
            asyncThreadPool.shutdownNow();
        }
        try {
            TimeUnit.MILLISECONDS.sleep(GlobalProperties.ENGINE_SHUTDOWN_NOW_SLEEP_SECONDS);
        } catch (Throwable e) {
            LOGGER.warn("time slot thread pool close task are interrupted on shutdown!", e);
        }
        if (asyncThreadPool.isShutdown() && asyncThreadPool.getActiveCount() == 0) {
            LOGGER.info("[shutdownNow] interrupting tasks in the thread pool success! thread pool close success!");
        } else {
            LOGGER.error("[shutdownNow] interrupting tasks in the thread pool error! thread pool close error!");
        }
    }

    @Override
    public ExecutorType getExecutorType() {
        return executorType;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
