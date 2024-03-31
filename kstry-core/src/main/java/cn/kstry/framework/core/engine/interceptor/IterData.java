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
package cn.kstry.framework.core.engine.interceptor;

import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.role.Role;

public class IterData {

    private final ScopeDataOperator scopeDataOperator;

    private final ServiceNodeResource serviceNodeResource;

    private final Role role;

    public IterData(ScopeDataOperator scopeDataOperator, ServiceNodeResource serviceNodeResource, Role role) {
        this.scopeDataOperator = scopeDataOperator;
        this.serviceNodeResource = serviceNodeResource;
        this.role = role;
    }

    public ServiceNodeResource getServiceNode() {
        return serviceNodeResource;
    }

    public ScopeDataOperator getDataOperator() {
        return scopeDataOperator;
    }

    public Role getRole() {
        return role;
    }
}
