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
package cn.kstry.framework.core.annotation;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定调用服务节点时的调用属性
 * 服务节点执行出现异常时先重试，失败后再降级，再次失败后将参考严格模式策略决定是抛出异常结束流程还是继续向下执行
 *
 * @author lykan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface Invoke {

    /**
     * 服务节点执行失败时重试次数。默认为 0 代表不重试。举例：为 1 时代表如果执行失败，将进行一次重试【注意：流程配置中也可指定服务节点失败重试次数属性，流程配置指定后，注解方式将会失效】
     *
     * @return 重试次数
     */
    int retry() default 0;

    /**
     *
     * 执行服务节点时允许的最大等待时间。默认为 -1 代表不设置超时时间，一直等待【注意：流程配置中也可指定服务节点调用超时时间属性，流程配置指定后，注解方式将会失效】
     *
     * @return 超时时间，单位：ms
     */
    int timeout() default -1;

    /**
     * 指定服务节点执行失败（包含重试）或超时后的降级处理表达式。异常发生后会根据表达式从容器中找到对应的服务节点执行
     * 降级表达式举例：
     *  1> pr:risk-control@check-img  服务组件名是 risk-control 且 服务名是 init-base-info 的服务节点
     *  2> pr:risk-control@check-img@triple  服务组件名是 risk-control 且 服务名是 init-base-info 且 能力点名是 triple 的服务节点
     *
     * @return 服务节点降级处理表达式
     */
    String demotion() default StringUtils.EMPTY;

    /**
     * 严格模式，控制服务节点执行失败后是否要抛出异常，默认是严格模式，节点抛出异常后结束整个 Story 流程
     * 关闭严格模式后，节点抛出异常时忽略该节点继续向下执行【注意：流程配置中也可指定服务节点严格模式属性，两者同时为严格模式时才会是严格模式，有任一个是非严格模式时，任务就是非严格模式】
     *
     * @return 是否是严格模式
     */
    boolean strictMode() default true;

    /**
     * 抛出异常包含以下异常时，进行重试。默认情况下列表为空，任何异常都将重试
     *
     * @since 1.1.3
     * @return 异常列表
     */
    Class<? extends Throwable>[] retryIncludeExp() default {};

    /**
     * 抛出异常不包含以下异常时，进行重试。默认情况下列表为空，任何异常都将重试
     *
     * @since 1.1.3
     * @return 异常列表
     */
    Class<? extends Throwable>[] retryExcludeExp() default {};

    /**
     * 指定服务节点执行器，不为空时从Spring容器中使用名称获取 ThreadPoolExecutor 实例来作为执行器执行当前服务节点，默认不指定自定义执行器
     */
    String executor() default StringUtils.EMPTY;
}
