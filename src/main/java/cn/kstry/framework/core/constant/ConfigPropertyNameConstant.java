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
public interface ConfigPropertyNameConstant {

    /**
     * Story 超时时间
     */
    String KSTRY_STORY_TIMEOUT = "kstry.story.timeout";

    /**
     * StartEvent ID 前缀
     */
    String KSTRY_STORY_PREFIX = "kstry.story.prefix";

    /**
     * 成功状态码
     */
    String KSTRY_STORY_SUCCESS_CODE = "kstry.story.success-code";

    /**
     * 链路追踪类型
     */
    String KSTRY_STORY_TRACKING_TYPE = "kstry.story.tracking.type";

    /**
     * 是否打印链路追踪
     */
    String KSTRY_STORY_TRACKING_LOG = "kstry.story.tracking.log";

    /**
     * requestId 名字
     */
    String KSTRY_STORY_REQUEST_ID_NAME = "kstry.story.request-id";

    /**
     *  kstry 线程池核心线程数
     */
    String KSTRY_THREAD_POOL_CORE_SIZE = "kstry.thread.pool.core-size";

    /**
     *  kstry 线程池最大线程数
     */
    String KSTRY_THREAD_POOL_MAX_SIZE = "kstry.thread.pool.max-size";

    /**
     *  kstry 线程池空闲线程等待时间，单位：分钟
     */
    String KSTRY_THREAD_POOL_KEEP_ALIVE_TIME = "kstry.thread.pool.keep-alive-time";

    /**
     *  kstry 线程池缓存队列长度
     */
    String KSTRY_THREAD_POOL_QUEUE_SIZE = "kstry.thread.pool.queue-size";

    /**
     * 线程监控日志打印间隔时间，默认 10s
     */
    String KSTRY_THREAD_POOL_MONITOR_DELAY = "kstry.thread.pool.monitor.delay";

    /**
     * 是否开启线程池监控，进行日志打印
     */
    String KSTRY_THREAD_POOL_MONITOR_ENABLE = "kstry.thread.pool.monitor.enable";

    /**
     *  Thread Pool shutdown 之后的等待时间
     */
    String KSTRY_THREAD_SHUTDOWN_AWAIT = "kstry.thread.pool.shutdown-await";

    /**
     *  shutdown失败之后，Thread Pool shutdownNow 之后的等待时间
     */
    String KSTRY_THREAD_SHUTDOWN_NOW_AWAIT = "kstry.thread.pool.shutdown-now-await";
}
