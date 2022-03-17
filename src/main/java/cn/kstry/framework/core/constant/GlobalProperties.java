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
package cn.kstry.framework.core.constant;

/**
 *
 * @author lykan
 */
public class GlobalProperties {

    /**
     * StartEvent ID 前缀
     */
    public static String START_EVENT_ID_PREFIX = "story-def-";

    /**
     * Story 执行成功后的 code
     */
    public static String STORY_SUCCESS_CODE = "200";

    /**
     * Async 任务默认超时时间
     */
    public static int ASYNC_TASK_DEFAULT_TIMEOUT = 3000;

    /**
     * Thread Pool shutdown 之后的等待时间
     */
    public static long ENGINE_SHUTDOWN_SLEEP_SECONDS = 3000;

    /**
     *  kstry 线程池核心线程数
     */
    public static int THREAD_POOL_CORE_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 2);

    /**
     *  kstry 线程池最大线程数
     */
    public static int THREAD_POOL_MAX_SIZE = THREAD_POOL_CORE_SIZE * 2;

    /**
     *  kstry 线程池空闲线程等待时间，单位：分钟
     */
    public static long THREAD_POOL_KEEP_ALIVE_TIME = 10;

    /**
     *  kstry 线程池缓存队列长度
     */
    public static int KSTRY_THREAD_POOL_QUEUE_SIZE = 10_000;

    /**
     * requestId 名字
     */
    public static String KSTRY_STORY_REQUEST_ID_NAME = "ks-request-id";

    /**
     * shutdown失败之后，Thread Pool shutdownNow 之后的等待时间
     */
    public static long ENGINE_SHUTDOWN_NOW_SLEEP_SECONDS = 5000;

    /**
     * 链路追踪类型
     */
    public static String STORY_MONITOR_TRACKING_TYPE = "none";

    /**
     * 是否打印链路追踪日志
     */
    public static boolean KSTRY_STORY_TRACKING_LOG = false;
}
