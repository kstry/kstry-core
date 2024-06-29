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

import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.bpmn.impl.SequenceFlowExpression;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.component.expression.ConditionExpression;
import cn.kstry.framework.core.component.expression.ExpressionAliasParser;
import cn.kstry.framework.core.component.limiter.strategy.ExceptionFailAcquireStrategy;
import cn.kstry.framework.core.component.limiter.strategy.FailAcquireStrategy;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BasicNodeRateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicNodeRateLimiter.class);

    protected final ExpressionAliasParser expressionAliasParser;

    protected final Map<String, FailAcquireStrategy> failAcquireStrategyMap;

    public BasicNodeRateLimiter(List<FailAcquireStrategy> failAcquireStrategyList, ExpressionAliasParser expressionAliasParser) {
        AssertUtil.notNull(expressionAliasParser);
        this.failAcquireStrategyMap = CollectionUtils.isEmpty(failAcquireStrategyList)
                ? ImmutableMap.of()
                : ImmutableMap.copyOf(failAcquireStrategyList.stream().collect(Collectors.toMap(FailAcquireStrategy::name, Function.identity(), (x, y) -> {
            throw ExceptionUtil.buildException(null, ExceptionEnum.COMPONENT_DUPLICATION_ERROR, GlobalUtil.format("FailAcquireStrategy is not allowed to be registered repeatedly. name: {}", x.name()));
        })));
        this.expressionAliasParser = expressionAliasParser;
    }

    protected ConditionExpression buildConditionExpression(ServiceNodeResource serviceNodeResource, RateLimiterConfig rateLimiterConfig) {
        try {
            ConditionExpression conditionExpression = null;
            if (StringUtils.isNotBlank(rateLimiterConfig.getExpression())) {
                SequenceFlowExpression sequenceFlowExpression = new SequenceFlowExpression(rateLimiterConfig.getExpression());
                sequenceFlowExpression.setId(GlobalUtil.uuid(BpmnTypeEnum.EXPRESSION));
                sequenceFlowExpression.setName(rateLimiterConfig.getExpression());
                conditionExpression = sequenceFlowExpression.getConditionExpression().orElse(null);
            }
            if (conditionExpression != null) {
                conditionExpression.parseConditionExpression(expressionAliasParser);
            }
            return conditionExpression;
        } catch (Exception e) {
            LOGGER.error("[{}] ServiceTask flow-limiting expression parsing failure. identity: {}, expression: {}",
                    ExceptionEnum.EXPRESSION_INVOKE_ERROR.getExceptionCode(), serviceNodeResource.getIdentityId(), rateLimiterConfig.getExpression(), e);
            return null;
        }
    }

    protected boolean notNeedLimiter(StoryBus storyBus, ConditionExpression conditionExpression) {
        return Optional.ofNullable(conditionExpression).map(c -> !c.condition(storyBus)).orElse(false);
    }

    protected Optional<FailAcquireStrategy> getFailStrategy(String failStrategy) {
        Optional<FailAcquireStrategy> optional = Optional.ofNullable(failStrategy).filter(StringUtils::isNotBlank).map(failAcquireStrategyMap::get);
        if (optional.isPresent()) {
            return optional;
        }
        LOGGER.warn("[{}] Failure to get the specified flow-limiting failure strategy: {}, use the default strategy: {}",
                ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY.getExceptionCode(), failStrategy, ExceptionFailAcquireStrategy.NAME);
        return Optional.ofNullable(failAcquireStrategyMap.get(ExceptionFailAcquireStrategy.NAME));
    }
}
