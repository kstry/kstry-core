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
package cn.kstry.framework.core.component.limiter;

import cn.kstry.framework.core.component.limiter.strategy.ExceptionFailAcquireStrategy;
import org.apache.commons.lang3.StringUtils;

/**
 * RateLimiterConfig构建类
 *
 * @author lykan
 */
public class RateLimiterBuilder {

    /**
     * 限流器名称
     */
    private String name = LocalSingleNodeRateLimiter.LOCAL_SINGLE_NODE_RATE_LIMITER;

    /**
     * 每秒钟发放的令牌数量，默认-1：无限流，支持0
     */
    private double permits = -1;

    /**
     * 预热时间，单位ms，默认0：无预热
     */
    private int warmupPeriod = 0;

    /**
     * 获取令牌的最大等待时长，单位ms，默认0：不等待。
     */
    private int acquireTimeout = 0;

    /**
     * 失败策略
     */
    private String failStrategy = ExceptionFailAcquireStrategy.NAME;

    /**
     * 限流是否生效的判断表达式，默认为空，直接生效
     */
    private String expression = StringUtils.EMPTY;


    public RateLimiterBuilder name(String name) {
        this.name = name;
        return this;
    }

    public RateLimiterBuilder permits(double permits) {
        this.permits = permits;
        return this;
    }

    public RateLimiterBuilder warmupPeriod(int warmupPeriod) {
        this.warmupPeriod = warmupPeriod;
        return this;
    }

    public RateLimiterBuilder acquireTimeout(int acquireTimeout) {
        this.acquireTimeout = acquireTimeout;
        return this;
    }

    public RateLimiterBuilder failStrategy(String failStrategy) {
        this.failStrategy = failStrategy;
        return this;
    }

    public RateLimiterBuilder expression(String expression) {
        this.expression = expression;
        return this;
    }

    public RateLimiterConfig build() {
        return new RateLimiterConfig(name, permits, warmupPeriod, acquireTimeout, failStrategy, expression);
    }

    public static RateLimiterBuilder of(RateLimiterConfig config) {
        RateLimiterBuilder rateLimiterBuilder = new RateLimiterBuilder();
        if (config == null) {
            return rateLimiterBuilder;
        }
        rateLimiterBuilder.name = config.getName();
        rateLimiterBuilder.permits = config.getPermits();
        rateLimiterBuilder.expression = config.getExpression();
        rateLimiterBuilder.warmupPeriod = config.getWarmupPeriod();
        rateLimiterBuilder.failStrategy = config.getFailStrategy();
        rateLimiterBuilder.acquireTimeout = config.getAcquireTimeout();
        return rateLimiterBuilder;
    }
}
