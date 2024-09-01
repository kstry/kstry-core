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
package cn.kstry.framework.core.constant;

/**
 *
 * @author lykan
 */
public interface ConfigPropertyNameConstant {

    /**
     * Story 超时时间，默认：3000ms
     */
    String KSTRY_STORY_TIMEOUT = "kstry.story.timeout";

    /**
     * 成功状态码，默认：200
     */
    String KSTRY_STORY_SUCCESS_CODE = "kstry.story.success-code";

    /**
     * 链路追踪类型，默认：none
     */
    String KSTRY_STORY_TRACKING_TYPE = "kstry.story.tracking.type";

    /**
     * 是否打印链路追踪，默认：false
     */
    String KSTRY_STORY_TRACKING_LOG = "kstry.story.tracking.log";

    /**
     * 限制环路流程循环的最大次数，默认：Long.MAX_VALUE
     */
    String KSTRY_STORY_MAX_CYCLE_COUNT = "kstry.story.max-cycle-count";

    /**
     * 监控中节点出入参快照最大长度，超出将进行截取。默认为 -1 代表不限制， 0 时不记录入参， >0 时会对节点出入参快照截取到该字段长度。仅在 TrackingTypeEnum == SERVICE_DETAIL 时生效
     */
    String KSTRY_STORY_TRACKING_PARAMS_LENGTH_LIMIT = "kstry.story.tracking.value-max-length";

    /**
     * requestId 名字，默认：ks-request-id
     */
    String KSTRY_STORY_REQUEST_ID_NAME = "kstry.story.request-id";

    /**
     * 允许流程配置中指定服务节点入参，默认：true
     */
    String KSTRY_STORY_DEFINE_NODE_PARAMS = "kstry.story.define-node-params";

    /**
     *  kstry 线程池核心线程数，默认：Math.max(Runtime.getRuntime().availableProcessors(), 2)
     */
    String KSTRY_THREAD_POOL_CORE_SIZE = "kstry.thread.pool.core-size";

    /**
     * ThreadLocal切换线程拷贝时需要忽略的前缀，可逗号隔开配置多个
     */
    String KSTRY_THREAD_IGNORE_COPY_PREFIX = "kstry.thread.ignore-copy-prefix";

    /**
     *  kstry 线程池最大线程数，默认：Math.max(Runtime.getRuntime().availableProcessors(), 2) * 2
     */
    String KSTRY_THREAD_POOL_MAX_SIZE = "kstry.thread.pool.max-size";

    /**
     *  kstry 线程池空闲线程等待时间，单位：分钟，默认：10
     */
    String KSTRY_THREAD_POOL_KEEP_ALIVE_TIME = "kstry.thread.pool.keep-alive-time";

    /**
     *  kstry 线程池缓存队列长度，默认：2000
     */
    String KSTRY_THREAD_POOL_QUEUE_SIZE = "kstry.thread.pool.queue-size";

    /**
     * 线程监控日志打印间隔时间，默认 10s
     */
    String KSTRY_THREAD_POOL_MONITOR_DELAY = "kstry.thread.pool.monitor.delay";

    /**
     * 是否开启线程池监控，进行日志打印，默认：true
     */
    String KSTRY_THREAD_POOL_MONITOR_ENABLE = "kstry.thread.pool.monitor.enable";

    /**
     *  Thread Pool shutdown 之后的等待时间，单位：ms，默认：3000
     */
    String KSTRY_THREAD_SHUTDOWN_AWAIT = "kstry.thread.pool.shutdown-await";

    /**
     *  shutdown失败之后，Thread Pool shutdownNow 之后的等待时间，单位：ms，默认：5000
     */
    String KSTRY_THREAD_SHUTDOWN_NOW_AWAIT = "kstry.thread.pool.shutdown-now-await";

    /**
     * 使用JDK21+时，可以使用该参数开启虚拟线程
     */
    String KSTRY_THREAD_OPEN_VIRTUAL = "kstry.thread.open-virtual";

    /**
     * 类型转换中日期格式，默认：yyyy-MM-dd HH:mm:ss
     */
    String TYPE_CONVERTER_DATE_FORMAT = "kstry.converter.date-format";
}
