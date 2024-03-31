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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;

/**
 * 任务线程池
 *
 * @author lykan
 */
public class TaskServiceExecutor implements TaskExecutor, ComponentLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceExecutor.class);

    private static final boolean SUPPORT_VIRTUAL_THREAD = ClassUtils.isPresent("java.lang.VirtualThread", TaskServiceExecutor.class.getClassLoader())
            && ClassUtils.isPresent("java.util.concurrent.ThreadPerTaskExecutor", TaskServiceExecutor.class.getClassLoader());

    private final ExecutorType executorType;

    private final ExecutorService executorService;

    private final String prefix;

    public TaskServiceExecutor(ExecutorType executorType, ExecutorService executorService, String prefix) {
        AssertUtil.anyNotNull(executorType, executorService);
        this.executorService = executorService;
        this.executorType = executorType;
        this.prefix = prefix == null ? StringUtils.EMPTY : prefix;
    }

    public TaskServiceExecutor(ExecutorType executorType, int corePoolSize, int maximumPoolSize, long keepAliveTime,
                               TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler, String prefix) {
        this(executorType, new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler), prefix);
    }

    public static TaskServiceExecutor buildDefaultExecutor(ExecutorType executorType, String prefix) {
        if (GlobalProperties.KSTRY_OPEN_VIRTUAL_THREAD && SUPPORT_VIRTUAL_THREAD) {
            try {
                return getVirtualTaskServiceExecutor(executorType, prefix);
            } catch (Throwable e) {
                LOGGER.warn("TaskServiceExecutor getVirtualTaskServiceExecutor error. use ThreadPoolExecutor. name: {}", prefix, e);
            }
        }
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

    public static TaskServiceExecutor buildDefaultExecutor(ExecutorType executorType, ExecutorService executorService, String prefix) {
        return new TaskServiceExecutor(executorType, executorService, prefix);
    }

    public static TaskServiceExecutor buildTaskExecutor(ExecutorType executorType, String prefix, int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                                        TimeUnit keepAliveTimeUnit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                                                        RejectedExecutionHandler handler) {
        return new TaskServiceExecutor(executorType, corePoolSize, maximumPoolSize, keepAliveTime, keepAliveTimeUnit, workQueue, threadFactory, handler, prefix);
    }

    @Override
    public AdminFuture submitAdminTask(ExecutorService executorService, MainFlowTask mainFlowTask) {
        AssertUtil.notNull(mainFlowTask);
        try {
            Future<AsyncTaskState> future = getActualExecutor(executorService).submit(mainFlowTask);
            MainTaskFuture mainTaskFuture = mainFlowTask.buildTaskFuture(future);
            AdminTaskFuture adminTaskFuture = new AdminTaskFuture(mainTaskFuture);
            mainFlowTask.setAdminFuture(adminTaskFuture);
            return adminTaskFuture;
        } finally {
            mainFlowTask.openSwitch();
        }
    }

    @Override
    public void submitFragmentTask(ExecutorService executorService, FragmentTask fragmentTask) {
        AssertUtil.notNull(fragmentTask);
        try {
            Future<AsyncTaskState> future = getActualExecutor(executorService).submit(fragmentTask);
            FragmentFuture fragmentFuture = fragmentTask.buildTaskFuture(future);
            FlowRegister flowRegister = fragmentTask.getFlowRegister();
            flowRegister.getAdminFuture().addManagedFuture(fragmentFuture, flowRegister.getStartEventId());
        } finally {
            fragmentTask.openSwitch();
        }
    }

    @Override
    public void submitMonoFlowTask(ExecutorService executorService, String parentStartEventId, MonoFlowTask flowTask) {
        AssertUtil.notNull(flowTask);
        try {
            Future<AsyncTaskState> future = getActualExecutor(executorService).submit(flowTask);
            MonoFlowFuture monoFlowFuture = flowTask.buildTaskFuture(future);
            FlowRegister flowRegister = flowTask.getFlowRegister();
            flowRegister.getAdminFuture().addManagedFuture(parentStartEventId, monoFlowFuture, flowRegister.getStartEventId());
        } finally {
            flowTask.openSwitch();
        }
    }

    @Override
    public InvokeFuture submitMethodInvokeTask(ExecutorService executorService, MethodInvokeTask methodInvokeTask) {
        AssertUtil.notNull(methodInvokeTask);
        try {
            Future<Object> future = getActualExecutor(executorService).submit(methodInvokeTask);
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
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor asyncThreadPool = (ThreadPoolExecutor) executorService;
            LOGGER.info("Begin shutdown time slot thread pool! name: {}, active count: {}", prefix, asyncThreadPool.getActiveCount());
            asyncThreadPool.shutdown();
            try {
                TimeUnit.MILLISECONDS.sleep(GlobalProperties.ENGINE_SHUTDOWN_SLEEP_SECONDS);
            } catch (Throwable e) {
                LOGGER.warn("Time slot thread pool close task are interrupted on shutdown! name: {}", prefix, e);
            }
            if (asyncThreadPool.isShutdown() && asyncThreadPool.getActiveCount() == 0) {
                LOGGER.info("[shutdown] interrupting tasks in the thread pool success! thread pool close success!, name: {}", prefix);
                return;
            } else {
                LOGGER.info("Interrupting tasks in the thread pool that have not yet finished! begin shutdownNow! name: {}, active count: {}",
                        prefix, asyncThreadPool.getActiveCount());
                asyncThreadPool.shutdownNow();
            }
            try {
                TimeUnit.MILLISECONDS.sleep(GlobalProperties.ENGINE_SHUTDOWN_NOW_SLEEP_SECONDS);
            } catch (Throwable e) {
                LOGGER.warn("time slot thread pool close task are interrupted on shutdown! name: {}", prefix, e);
            }
            if (asyncThreadPool.isShutdown() && asyncThreadPool.getActiveCount() == 0) {
                LOGGER.info("[shutdownNow] interrupting tasks in the thread pool success! thread pool close success! name: {}", prefix);
            } else {
                LOGGER.error("[shutdownNow] interrupting tasks in the thread pool error! thread pool close error! name: {}", prefix);
            }
            return;
        }

        LOGGER.info("Begin shutdown time slot thread pool! name: {}", prefix);
        executorService.shutdown();
        try {
            TimeUnit.MILLISECONDS.sleep(GlobalProperties.ENGINE_SHUTDOWN_SLEEP_SECONDS);
        } catch (Throwable e) {
            LOGGER.warn("Time slot thread pool close task are interrupted on shutdown! name: {}", prefix, e);
        }
        if (executorService.isShutdown()) {
            LOGGER.info("[shutdown] interrupting tasks in the thread pool success! thread pool close success! name: {}", prefix);
            return;
        } else {
            LOGGER.info("Interrupting tasks in the thread pool that have not yet finished! begin shutdownNow! name: {}", prefix);
            executorService.shutdownNow();
        }
        try {
            TimeUnit.MILLISECONDS.sleep(GlobalProperties.ENGINE_SHUTDOWN_NOW_SLEEP_SECONDS);
        } catch (Throwable e) {
            LOGGER.warn("time slot thread pool close task are interrupted on shutdown! name: {}", prefix, e);
        }
        if (executorService.isShutdown()) {
            LOGGER.info("[shutdownNow] interrupting tasks in the thread pool success! thread pool close success! name: {}", prefix);
        } else {
            LOGGER.error("[shutdownNow] interrupting tasks in the thread pool error! thread pool close error! name: {}", prefix);
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

    public ExecutorService getExecutorService() {
        return executorService;
    }

    private ExecutorService getActualExecutor(ExecutorService es) {
        return es == null ? executorService : es;
    }

    private static TaskServiceExecutor getVirtualTaskServiceExecutor(ExecutorType executorType, String prefix) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object virtual1 = MethodUtils.invokeStaticMethod(Thread.class, "ofVirtual");
        Object virtual2 = MethodUtils.invokeMethod(virtual1, "name", prefix + "-", 0);
        Object virtual3 = MethodUtils.invokeMethod(virtual2, "uncaughtExceptionHandler", (Thread.UncaughtExceptionHandler) (t, e) ->
                LOGGER.info("[{}] thread uncaught exception. name: {}", ExceptionEnum.ASYNC_TASK_ERROR, t.getName(), e)
        );
        ThreadFactory factory = (ThreadFactory) MethodUtils.invokeMethod(virtual3, "factory");
        ExecutorService executorService = (ExecutorService) MethodUtils.invokeStaticMethod(Executors.class, "newThreadPerTaskExecutor", factory);
        return buildDefaultExecutor(executorType, executorService, prefix);
    }
}
