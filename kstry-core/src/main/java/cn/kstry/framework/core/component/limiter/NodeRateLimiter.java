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
import cn.kstry.framework.core.component.limiter.strategy.FailAcquireStrategy;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;

import java.util.Optional;

/**
 * 限流器定义
 */
public interface NodeRateLimiter {

    /**
     * 限流器名称
     */
    String getName();

    /**
     * 获取服务节点限流器
     */
    Optional<FailAcquireStrategy> getNodeRateLimiter(StoryBus storyBus, ServiceNodeResource serviceNodeResource, RateLimiterConfig rateLimiterConfig);
}
