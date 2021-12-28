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
package cn.kstry.framework.core.task;

import cn.kstry.framework.core.resource.identity.IdentityResource;
import cn.kstry.framework.core.role.Role;

import java.util.Optional;

/**
 *
 * @author lykan
 */
public interface TaskComponentRegisterWrapper extends TaskComponentRegister, IdentityResource {

    /**
     * 添加 Task Service
     *
     * @param taskServiceWrapper Task Service
     */
    void addTaskService(TaskServiceWrapper taskServiceWrapper);

    /**
     * 获取  TaskService
     *
     * @param name TaskServiceName
     * @param role 角色
     * @return TaskService
     */
    Optional<TaskServiceWrapper> getTaskService(String name, Role role);
}
