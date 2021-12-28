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
package cn.kstry.framework.core.monitor;

import cn.kstry.framework.core.constant.ConfigPropertyNameConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池监控
 *
 * @author lykan
 */
public class ThreadPoolMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolMonitor.class);

    private final String name;

    private final ThreadPoolExecutor threadPoolExecutor;

    public ThreadPoolMonitor(String name, ThreadPoolExecutor threadPoolExecutor) {
        this.name = name;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Scheduled(fixedDelayString = "${" + ConfigPropertyNameConstant.KSTRY_THREAD_POOL_MONITOR_DELAY + ":10000}")
    public void monitor() {
        handleExecutor(name, threadPoolExecutor);
    }

    public static void handleExecutor(String name, ThreadPoolExecutor threadPoolExecutor) {

        // 线程池需要执行的任务数
        long taskCount = threadPoolExecutor.getTaskCount();

        // 线程池在运行过程中已完成的任务数
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();

        // 曾经创建过的最大线程数
        long largestPoolSize = threadPoolExecutor.getLargestPoolSize();

        // 线程池里的线程数量
        long poolSize = threadPoolExecutor.getPoolSize();

        // 线程池里活跃的线程数量
        long activeCount = threadPoolExecutor.getActiveCount();

        // 配置的核心线程数
        int corePoolSize = threadPoolExecutor.getCorePoolSize();

        // 配置的最大线程数
        int maximumPoolSize = threadPoolExecutor.getMaximumPoolSize();

        // 当前线程池队列的个数
        int queueSize = threadPoolExecutor.getQueue().size();

        LOGGER.info("thread pool {} monitor. task-count:{}, completed-task-count:{}, largest-pool-size:{}, pool-size:{}, " +
                        "active-count:{}, core-pool-size:{}, maximum-pool-size:{}, queue-size:{}",
                name, taskCount, completedTaskCount, largestPoolSize, poolSize, activeCount, corePoolSize, maximumPoolSize, queueSize);
    }
}
