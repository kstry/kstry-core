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

import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.component.expression.ConditionExpression;
import cn.kstry.framework.core.component.expression.ExpressionAliasParser;
import cn.kstry.framework.core.component.limiter.strategy.FailAcquireStrategy;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 本地单实例限流
 */
public class LocalSingleNodeRateLimiter extends BasicNodeRateLimiter implements NodeRateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalSingleNodeRateLimiter.class);

    public static final String LOCAL_SINGLE_NODE_RATE_LIMITER = "local_single_node_rate_limiter";

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Map<String, InRateLimiter> globalRateLimiter = Maps.newHashMap();

    public LocalSingleNodeRateLimiter(List<FailAcquireStrategy> failAcquireStrategyList, ExpressionAliasParser expressionAliasParser) {
        super(failAcquireStrategyList, expressionAliasParser);
    }

    @Override
    public Optional<FailAcquireStrategy> getNodeRateLimiter(StoryBus storyBus, ServiceNodeResource serviceNodeResource, RateLimiterConfig rateLimiterConfig) {
        if (storyBus == null || serviceNodeResource == null || rateLimiterConfig == null) {
            return Optional.empty();
        }
        InRateLimiter inRateLimiter;
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        boolean needUnReadLock = true;
        try {
            inRateLimiter = globalRateLimiter.get(serviceNodeResource.getIdentityId());
            if (inRateLimiter == null || !Objects.equals(inRateLimiter.rateLimiterConfig, rateLimiterConfig)) {
                needUnReadLock = false;
                readLock.unlock();
                inRateLimiter = refreshGlobalRateLimiter(serviceNodeResource, rateLimiterConfig);
            }
        } finally {
            if (needUnReadLock) {
                readLock.unlock();
            }
        }
        return getFailAcquireStrategy(storyBus, inRateLimiter);
    }

    private InRateLimiter refreshGlobalRateLimiter(ServiceNodeResource serviceNodeResource, RateLimiterConfig rateLimiterConfig) {
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            String key = serviceNodeResource.getIdentityId();
            InRateLimiter inRateLimiter = globalRateLimiter.get(key);
            if (inRateLimiter != null && Objects.equals(inRateLimiter.rateLimiterConfig, rateLimiterConfig)) {
                return inRateLimiter;
            }
            inRateLimiter = new InRateLimiter(rateLimiterConfig, buildConditionExpression(serviceNodeResource, rateLimiterConfig));
            RateLimiterConfig oldConfig = Optional.ofNullable(globalRateLimiter.remove(key)).map(r -> r.rateLimiterConfig).orElse(null);
            globalRateLimiter.put(key, inRateLimiter);
            LOGGER.debug("LocalSingleNodeRateLimiter globalRateLimiter refresh. identity: {}, old: {}, new: {}", serviceNodeResource.getIdentityId(), oldConfig, rateLimiterConfig);
            return inRateLimiter;
        } finally {
            writeLock.unlock();
        }
    }

    @SuppressWarnings("all")
    private Optional<FailAcquireStrategy> getFailAcquireStrategy(StoryBus storyBus, InRateLimiter inRateLimiter) {
        if (inRateLimiter == null || !inRateLimiter.valid()) {
            return Optional.empty();
        }
        if (notNeedLimiter(storyBus, inRateLimiter.conditionExpression)) {
            return Optional.empty();
        }
        if (inRateLimiter.rateLimiter == null) {
            return getFailStrategy(inRateLimiter.rateLimiterConfig.getFailStrategy());
        }
        boolean tryAcquire;
        RateLimiterConfig rateLimiterConfig = inRateLimiter.rateLimiterConfig;
        if (rateLimiterConfig.getAcquireTimeout() <= 0) {
            tryAcquire = inRateLimiter.rateLimiter.tryAcquire();
        } else {
            tryAcquire = inRateLimiter.rateLimiter.tryAcquire(rateLimiterConfig.getAcquireTimeout(), TimeUnit.MILLISECONDS);
        }
        if (tryAcquire) {
            return Optional.empty();
        }
        return getFailStrategy(inRateLimiter.rateLimiterConfig.getFailStrategy());
    }

    @SuppressWarnings("all")
    private static class InRateLimiter {

        private final RateLimiter rateLimiter;

        private final RateLimiterConfig rateLimiterConfig;

        private final ConditionExpression conditionExpression;

        public InRateLimiter(RateLimiterConfig config, ConditionExpression conditionExpression) {
            AssertUtil.notNull(config);
            if (!config.valid() || config.getPermits() <= 0.0) {
                this.rateLimiter = null;
            } else if (config.getWarmupPeriod() <= 0) {
                this.rateLimiter = RateLimiter.create(config.getPermits());
            } else {
                this.rateLimiter = RateLimiter.create(config.getPermits(), config.getWarmupPeriod(), TimeUnit.MILLISECONDS);
            }
            this.rateLimiterConfig = config;
            this.conditionExpression = conditionExpression;
        }

        public boolean valid() {
            return rateLimiterConfig.valid();
        }
    }

    @Override
    public String getName() {
        return LOCAL_SINGLE_NODE_RATE_LIMITER;
    }
}
