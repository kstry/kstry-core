/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
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

/**
 *
 * @author lykan
 */
public class InvokeProperties {

    private final int retry;

    private final int timeout;

    private final ServiceNodeResource demotionResource;

    private final boolean strictMode;

    private boolean validDemotion;

    public InvokeProperties(Invoke invoke) {
        this.retry = invoke.retry();
        this.timeout = invoke.timeout();
        this.strictMode = invoke.strictMode();
        this.demotionResource = PermissionUtil.parseResource(invoke.demotion())
                .filter(p -> p.getPermissionType() == PermissionType.COMPONENT_SERVICE || p.getPermissionType() == PermissionType.COMPONENT_SERVICE_ABILITY)
                .orElse(null);
        this.validDemotion = this.demotionResource != null;
    }

    public int getRetry() {
        return Math.max(retry, 0);
    }

    public Integer getTimeout() {
        return timeout >= 0 ? timeout : null;
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
}
