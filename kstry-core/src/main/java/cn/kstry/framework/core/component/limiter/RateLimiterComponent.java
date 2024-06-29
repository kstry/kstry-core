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
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RateLimiterComponent {

    private final Map<String, NodeRateLimiter> rateLimiterMap;

    public RateLimiterComponent(List<NodeRateLimiter> nodeRateLimiters) {
        if (CollectionUtils.isEmpty(nodeRateLimiters)) {
            rateLimiterMap = ImmutableMap.of();
        } else {
            rateLimiterMap = ImmutableMap.copyOf(nodeRateLimiters.stream().filter(r -> StringUtils.isNotBlank(r.getName())).collect(Collectors.toMap(NodeRateLimiter::getName, Function.identity(), (x, y) -> {
                throw ExceptionUtil.buildException(null, ExceptionEnum.COMPONENT_DUPLICATION_ERROR, GlobalUtil.format("RateLimiter is not allowed to be registered repeatedly. name: {}", x.getName()));
            })));
        }
    }

    public Optional<FailAcquireStrategy> tryAcquire(StoryBus storyBus, ServiceNodeResource serviceNodeResource, RateLimiterConfig rateLimiterConfig) {
        if (rateLimiterConfig == null || !rateLimiterConfig.valid() || serviceNodeResource == null || storyBus == null) {
            return Optional.empty();
        }
        NodeRateLimiter nodeRateLimiter = rateLimiterMap.get(rateLimiterConfig.getName());
        if (nodeRateLimiter == null) {
            return Optional.empty();
        }
        return nodeRateLimiter.getNodeRateLimiter(storyBus, serviceNodeResource, rateLimiterConfig);
    }
}
