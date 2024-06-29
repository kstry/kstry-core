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

import cn.kstry.framework.core.annotation.Limiter;
import cn.kstry.framework.core.component.limiter.strategy.ExceptionFailAcquireStrategy;
import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.resource.identity.BasicIdentity;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 限流器配置
 */
public class RateLimiterConfig extends BasicIdentity {

    /**
     * 限流器名称
     */
    private final String name;

    /**
     * 每秒钟发放的令牌数量，默认-1：无限流，支持0
     */
    private final double permits;

    /**
     * 预热时间，单位ms，默认0：无预热
     */
    private final int warmupPeriod;

    /**
     * 获取令牌的最大等待时长，单位ms，默认0：不等待。
     */
    private final int acquireTimeout;

    /**
     * 失败策略
     */
    private final String failStrategy;

    /**
     * 限流是否生效的判断表达式，默认为空，直接生效
     */
    private final String expression;

    public RateLimiterConfig(Limiter limiter) {
        this(limiter.name(), limiter.permits(), limiter.warmupPeriod(), limiter.acquireTimeout(), limiter.failStrategy(), limiter.expression());
    }

    public RateLimiterConfig(String name, double permits, int warmupPeriod, int acquireTimeout, String failStrategy, String expression) {
        super(String.join("|", name, String.valueOf(permits), String.valueOf(warmupPeriod), String.valueOf(acquireTimeout), failStrategy, expression), IdentityTypeEnum.RATE_LIMITER);
        this.name = name;
        this.permits = permits;
        this.warmupPeriod = warmupPeriod;
        this.acquireTimeout = acquireTimeout;
        this.failStrategy = failStrategy;
        this.expression = expression;
    }

    public RateLimiterConfig merge(RateLimiterConfig config) {
        if (config == null) {
            return this;
        }
        if (getPermits() < 0.0 && config.getPermits() < 0.0) {
            return this;
        }
        RateLimiterBuilder builder = RateLimiterBuilder.of(config);
        if (StringUtils.isNotBlank(getName()) && !Objects.equals(getName(), LocalSingleNodeRateLimiter.LOCAL_SINGLE_NODE_RATE_LIMITER)) {
            builder.name(getName());
        }
        if (getPermits() >= 0.0) {
            builder.permits(getPermits());
        }
        if (getWarmupPeriod() > 0) {
            builder.warmupPeriod(getWarmupPeriod());
        }
        if (getAcquireTimeout() > 0) {
            builder.acquireTimeout(getAcquireTimeout());
        }
        if (StringUtils.isNotBlank(getFailStrategy()) && !ExceptionFailAcquireStrategy.NAME.equals(getFailStrategy())) {
            builder.failStrategy(getFailStrategy());
        }
        if (StringUtils.isNotBlank(getExpression())) {
            builder.expression(getExpression());
        }
        return builder.build();
    }

    public String getName() {
        return name;
    }

    public double getPermits() {
        return permits;
    }

    public int getWarmupPeriod() {
        return warmupPeriod;
    }

    public int getAcquireTimeout() {
        return acquireTimeout;
    }

    public String getFailStrategy() {
        return failStrategy;
    }

    public String getExpression() {
        return expression;
    }

    public boolean valid() {
        return permits >= 0.0 && StringUtils.isNotBlank(name);
    }
}
