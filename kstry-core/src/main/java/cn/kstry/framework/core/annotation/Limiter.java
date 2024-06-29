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
package cn.kstry.framework.core.annotation;

import cn.kstry.framework.core.component.limiter.LocalSingleNodeRateLimiter;
import cn.kstry.framework.core.component.limiter.strategy.ExceptionFailAcquireStrategy;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务节点限速器
 *
 * @author lykan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface Limiter {

    /**
     * 使用限速器名称，默认：本地单实例限流
     */
    String name() default LocalSingleNodeRateLimiter.LOCAL_SINGLE_NODE_RATE_LIMITER;

    /**
     * 每秒钟发放的令牌数量，默认-1：无限流，支持0
     */
    double permits() default -1;

    /**
     * 预热时间，单位ms，默认0：无预热
     */
    int warmupPeriod() default 0;

    /**
     * 获取令牌的最大等待时长，单位ms，默认0：不等待。
     */
    int acquireTimeout() default 0;

    /**
     * 失败策略，默认抛出异常
     */
    String failStrategy() default ExceptionFailAcquireStrategy.NAME;

    /**
     * 限流是否生效的判断表达式，默认为空，直接生效
     */
    String expression() default StringUtils.EMPTY;
}
