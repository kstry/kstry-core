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
package cn.kstry.framework.core.component.limiter.strategy;

import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.component.limiter.RateLimiterConfig;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.RateLimiterException;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.util.GlobalUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 抛出异常
 */
public class ExceptionFailAcquireStrategy implements FailAcquireStrategy {

    public static final String NAME = "exception";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void accept(ScopeDataOperator dataOperator, RateLimiterConfig rateLimiterConfig) {
        String componentName = StringUtils.EMPTY;
        String serviceName = StringUtils.EMPTY;
        String abilityName = StringUtils.EMPTY;
        ServiceNodeResource serviceNodeResource = dataOperator.getServiceNodeResource().orElse(null);
        if (serviceNodeResource != null) {
            componentName = serviceNodeResource.getComponentName();
            serviceName = serviceNodeResource.getServiceName();
            abilityName = serviceNodeResource.getAbilityName();
        }
        throw new RateLimiterException(ExceptionEnum.RATE_LIMITER_ERROR.getExceptionCode(),
                GlobalUtil.format("The service node is subject to rate limiting! component: {}, service: {}, ability: {}, limiter: {}", componentName, serviceName, abilityName, rateLimiterConfig.getIdentityId()), null);
    }
}
