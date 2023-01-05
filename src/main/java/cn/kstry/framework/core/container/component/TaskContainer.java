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

import cn.kstry.framework.core.container.ComponentLifecycle;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.role.Role;

import java.util.Optional;
import java.util.Set;

/**
 * 服务节点容器
 *
 * @author lykan
 */
public interface TaskContainer extends ComponentLifecycle {

    /**
     * 根据 （组件名称 + 服务名称 + 角色） 获取代码中的执行节点
     *
     * @param componentName  ComponentName
     * @param serviceName ServiceName
     * @param role Role
     * @return TaskServiceDef
     */
    Optional<TaskServiceDef> getTaskServiceDef(String componentName, String serviceName, Role role);

    /**
     * 获取资源集
     *
     * @return 资源集
     */
    Set<ServiceNodeResource> getServiceNodeResource();

    /**
     * 根据指令名称获取资源标识
     *
     * @return 资源标识
     */
    Optional<ServiceNodeResource> getServiceNodeResourceByInstruct(String instruction);
}
