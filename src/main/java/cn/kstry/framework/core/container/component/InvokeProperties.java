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
package cn.kstry.framework.core.container.component;

import cn.kstry.framework.core.annotation.Invoke;
import cn.kstry.framework.core.enums.PermissionType;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.util.PermissionUtil;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 *
 * @author lykan
 */
public class InvokeProperties {

    /**
     * 服务节点调用失败重试次数，在@Invoke注解中定义
     */
    private final int retry;

    /**
     * 服务节点调用超时时间，在@Invoke注解中定义
     * 非降级调用场景中，会以流程配置中服务节点的timeout属性为主（流程配置中配置了就用流程配置中的超时时间，没有配置时尝试获取@Invoke超时时间）该属性为辅
     * 在降级调用时，会以该超时时间为主流程配置中服务节点的timeout属性为辅
     */
    private final int timeout;

    /**
     * 服务节点调用是否是严格模式，在@Invoke注解中定义
     * 服务节点调用是否为严格模式。与流程配置中服务节点的严格模式相同。两者同时为严格模式时才会是严格默认，有任一是非严格模式时，任务调用将
     */
    private final boolean strictMode;

    /**
     * 抛出异常包含以下异常时，进行重试。默认情况下列表为空，任何异常都将重试
     */
    private final List<Class<? extends Throwable>> retryIncludeExceptionList;

    /**
     * 抛出异常不包含以下异常时，进行重试。默认情况下列表为空，任何异常都将重试
     */
    private final List<Class<? extends Throwable>> retryExcludeExceptionList;

    /**
     * 自定义执行器名称
     */
    private final String customExecutorName;

    /**
     * 服务节点调用失败时降低调用的资源位置，在@Invoke注解中定义
     */
    private final ServiceNodeResource demotionResource;

    private boolean validDemotion;

    public InvokeProperties(Invoke invoke) {
        this.retry = invoke.retry();
        this.timeout = invoke.timeout();
        this.strictMode = invoke.strictMode();
        this.customExecutorName = invoke.executor();
        this.retryIncludeExceptionList = ImmutableList.copyOf(invoke.retryIncludeExp());
        this.retryExcludeExceptionList = ImmutableList.copyOf(invoke.retryExcludeExp());
        this.demotionResource = PermissionUtil.parseResource(invoke.demotion())
                .filter(p -> p.getPermissionType() == PermissionType.COMPONENT_SERVICE || p.getPermissionType() == PermissionType.COMPONENT_SERVICE_ABILITY).orElse(null);
        this.validDemotion = this.demotionResource != null;
    }

    public int getRetry() {
        return Math.max(retry, 0);
    }

    public Integer getTimeout() {
        return timeout >= 0 ? timeout : null;
    }

    public List<Class<? extends Throwable>> getRetryIncludeExceptionList() {
        return retryIncludeExceptionList;
    }

    public List<Class<? extends Throwable>> getRetryExcludeExceptionList() {
        return retryExcludeExceptionList;
    }

    public ServiceNodeResource getDemotionResource() {
        return validDemotion ? demotionResource : null;
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    void invalidDemotion() {
        this.validDemotion = false;
    }

    public String getCustomExecutorName() {
        return customExecutorName;
    }
}
