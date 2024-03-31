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
package cn.kstry.framework.core.container.task;

import cn.kstry.framework.core.role.Role;

import java.util.Optional;

/**
 * TaskService 主节点名，包括服务节点和服务能力节点
 *
 * @author lykan
 */
public interface RootTaskService {

    /**
     * 添加服务节点或能力节点
     *
     * @param taskService TaskService
     */
    void addTaskService(TaskServiceWrapper taskService);

    /**
     * 获取服务节点或能力节点
     *
     * @param serviceName 服务名称
     * @param role 角色
     * @return TaskAbility
     */
    Optional<TaskServiceWrapper> getTaskService(String serviceName, Role role);
}
