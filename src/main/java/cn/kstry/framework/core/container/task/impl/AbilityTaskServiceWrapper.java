/*
 *
 *  * Copyright (c) 2020-2022, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.container.task.impl;

import javax.annotation.Nonnull;

import cn.kstry.framework.core.container.component.MethodWrapper;
import cn.kstry.framework.core.enums.ServiceNodeType;
import cn.kstry.framework.core.resource.identity.BasicIdentity;
import cn.kstry.framework.core.resource.service.ServiceNodeResourceAuth;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.role.ServiceTaskRole;
import cn.kstry.framework.core.container.task.TaskServiceWrapper;
import cn.kstry.framework.core.util.AssertUtil;

/**
 * 服务节点包装类
 *
 * @author lykan
 */
public class AbilityTaskServiceWrapper extends BasicIdentity implements TaskServiceWrapper {

    /**
     * 服务节点组件代理类
     */
    protected final TaskComponentProxy target;

    /**
     * 服务节点方法包装类
     */
    private final MethodWrapper methodWrapper;

    /**
     * 服务节点标
     */
    private final ServiceNodeResourceAuth serviceNodeResource;

    public AbilityTaskServiceWrapper(TaskComponentProxy target, MethodWrapper methodWrapper, ServiceNodeResourceAuth serviceNodeResource) {
        super(serviceNodeResource.getIdentityId(), serviceNodeResource.getServiceNodeType().getType());

        AssertUtil.anyNotNull(target, methodWrapper, serviceNodeResource);
        this.target = target;
        this.methodWrapper = methodWrapper;
        this.serviceNodeResource = serviceNodeResource;
    }

    @Override
    public String getName() {
        return serviceNodeResource.getServiceName();
    }

    @Override
    public TaskComponentProxy getTarget() {
        return target;
    }

    @Override
    public MethodWrapper getMethodWrapper() {
        return methodWrapper;
    }

    @Override
    public ServiceNodeResourceAuth getServiceNodeResource() {
        return serviceNodeResource;
    }

    @Override
    public ServiceNodeType getServiceNodeType() {
        return serviceNodeResource.getServiceNodeType();
    }

    @Override
    public boolean match(@Nonnull Role role) {
        if (target.isCustomRole()) {
            return getServiceNodeType() == ServiceNodeType.SERVICE_TASK;
        }
        if (getServiceNodeType() == ServiceNodeType.SERVICE_TASK && (role instanceof ServiceTaskRole)) {
            return true;
        }
        return role.allowedUseResource(serviceNodeResource);
    }
}
