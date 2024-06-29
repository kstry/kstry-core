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
package cn.kstry.framework.core.monitor;

/**
 * 限流信息
 *
 * @author lykan
 */
public class LimiterTracking {

    private Boolean limited;

    private String limiterName;

    private Double maxPermits;

    private Integer acquireTimeout;

    private String failStrategy;

    private ExpressionTracking expression;

    public Boolean getLimited() {
        return limited;
    }

    public void setLimited(Boolean limited) {
        this.limited = limited;
    }

    public String getLimiterName() {
        return limiterName;
    }

    public void setLimiterName(String limiterName) {
        this.limiterName = limiterName;
    }

    public Double getMaxPermits() {
        return maxPermits;
    }

    public void setMaxPermits(Double maxPermits) {
        this.maxPermits = maxPermits;
    }

    public Integer getAcquireTimeout() {
        return acquireTimeout;
    }

    public void setAcquireTimeout(Integer acquireTimeout) {
        this.acquireTimeout = acquireTimeout;
    }

    public String getFailStrategy() {
        return failStrategy;
    }

    public void setFailStrategy(String failStrategy) {
        this.failStrategy = failStrategy;
    }

    public ExpressionTracking getExpression() {
        return expression;
    }

    public void setExpression(ExpressionTracking expression) {
        this.expression = expression;
    }
}
