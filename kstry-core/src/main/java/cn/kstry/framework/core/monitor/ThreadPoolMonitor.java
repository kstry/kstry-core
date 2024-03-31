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
package cn.kstry.framework.core.monitor;

import cn.kstry.framework.core.constant.ConfigPropertyNameConstant;
import cn.kstry.framework.core.engine.thread.TaskServiceExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池监控
 *
 * @author lykan
 */
public class ThreadPoolMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolMonitor.class);

    private final List<TaskServiceExecutor> taskServiceExecutor;

    public ThreadPoolMonitor(List<TaskServiceExecutor> taskServiceExecutor) {
        this.taskServiceExecutor = taskServiceExecutor;
    }

    @Scheduled(fixedDelayString = "${" + ConfigPropertyNameConstant.KSTRY_THREAD_POOL_MONITOR_DELAY + ":10000}")
    public void monitor() {
        taskServiceExecutor.forEach(ThreadPoolMonitor::handleExecutor);
    }

    public static void handleExecutor(TaskServiceExecutor taskServiceExecutor) {
        ExecutorService executorService = taskServiceExecutor.getExecutorService();
        if (!(executorService instanceof ThreadPoolExecutor)) {
            return;
        }
        ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;

        // 线程池需要执行的任务数
        long taskCount = tpe.getTaskCount();

        // 线程池在运行过程中已完成的任务数
        long completedTaskCount = tpe.getCompletedTaskCount();

        // 曾经创建过的最大线程数
        long largestPoolSize = tpe.getLargestPoolSize();

        // 线程池里的线程数量
        long poolSize = tpe.getPoolSize();

        // 线程池里活跃的线程数量
        long activeCount = tpe.getActiveCount();

        // 配置的核心线程数
        int corePoolSize = tpe.getCorePoolSize();

        // 配置的最大线程数
        int maximumPoolSize = tpe.getMaximumPoolSize();

        // 当前线程池队列的个数
        int queueSize = tpe.getQueue().size();

        if (taskCount > 0) {
            LOGGER.info("Thread pool {} monitor. task-count: {}, completed-task-count: {}, largest-pool-size: {}, pool-size: {}, " +
                            "active-count: {}, core-pool-size: {}, maximum-pool-size: {}, queue-size: {}", taskServiceExecutor.getPrefix(),
                    taskCount, completedTaskCount, largestPoolSize, poolSize, activeCount, corePoolSize, maximumPoolSize, queueSize);
        }
    }
}
