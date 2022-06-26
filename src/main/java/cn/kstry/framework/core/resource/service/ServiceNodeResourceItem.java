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
package cn.kstry.framework.core.resource.service;

import cn.kstry.framework.core.enums.PermissionType;
import org.apache.commons.lang3.StringUtils;

import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.enums.ServiceNodeType;
import cn.kstry.framework.core.resource.identity.BasicIdentity;
import cn.kstry.framework.core.util.TaskServiceUtil;

/**
 * 服务节点资源项
 *
 * @author lykan
 */
public class ServiceNodeResourceItem extends BasicIdentity implements ServiceNodeResourceAuth {

    /**
     * 服务组件名
     */
    private final String componentName;

    /**
     * 服务节点名
     */
    private final String serviceName;

    /**
     * 服务能力名
     */
    private final String abilityName;

    public ServiceNodeResourceItem(String componentName, String serviceName, String abilityName) {
        super(PermissionType.COMPONENT_SERVICE.getPrefix() + ":" +
                TaskServiceUtil.joinName(componentName, TaskServiceUtil.joinName(serviceName, abilityName)), IdentityTypeEnum.SERVICE_NODE_RESOURCE);
        this.componentName = componentName;
        this.serviceName = serviceName;
        this.abilityName = abilityName;
    }

    @Override
    public String getComponentName() {
        return componentName;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getAbilityName() {
        return abilityName;
    }

    @Override
    public ServiceNodeType getServiceNodeType() {
        return StringUtils.isBlank(getAbilityName()) ? ServiceNodeType.SERVICE_TASK : ServiceNodeType.SERVICE_TASK_ABILITY;
    }
}
