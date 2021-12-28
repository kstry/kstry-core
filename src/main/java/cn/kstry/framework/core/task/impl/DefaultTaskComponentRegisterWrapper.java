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
package cn.kstry.framework.core.task.impl;

import cn.kstry.framework.core.container.MethodWrapper;
import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.resource.identity.BasicIdentityResource;
import cn.kstry.framework.core.resource.identity.IdentityResource;
import cn.kstry.framework.core.role.BasicRole;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.role.SimplePermission;
import cn.kstry.framework.core.task.TaskComponentRegisterWrapper;
import cn.kstry.framework.core.task.TaskServiceWrapper;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.TaskServiceUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author lykan
 */
public class DefaultTaskComponentRegisterWrapper extends BasicIdentityResource implements TaskComponentRegisterWrapper, IdentityResource {

    private final String name;

    private final Map<String, RootTaskServiceWrapper> taskServiceMap = Maps.newHashMap();

    public DefaultTaskComponentRegisterWrapper(@Nonnull String name) {
        super(name, IdentityTypeEnum.TASK_COMPONENT);
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void addTaskService(TaskServiceWrapper taskServiceWrapper) {
        AssertUtil.isTrue(TaskServiceUtil.isTaskService(taskServiceWrapper));
        TaskServiceWrapper oldTaskService = taskServiceMap.get(taskServiceWrapper.getName());
        if (oldTaskService == null) {
            doAddTaskServiceFirst(taskServiceWrapper);
        } else if (oldTaskService.getIdentityType() == IdentityTypeEnum.SERVICE_TASK) {
            doAddTaskService(taskServiceWrapper, oldTaskService);
        } else {
            KstryException.throwException(ExceptionEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public Optional<TaskServiceWrapper> getTaskService(String name, Role role) {
        if (StringUtils.isBlank(name)) {
            return Optional.empty();
        }

        RootTaskServiceWrapper taskServiceWrapper = taskServiceMap.get(name);
        if (taskServiceWrapper == null) {
            return Optional.empty();
        }

        if (role == null) {
            role = new BasicRole();
            role.addPermission(Lists.newArrayList(new SimplePermission(name, IdentityTypeEnum.SERVICE_TASK)));
        }
        return taskServiceWrapper.getTaskAbility(role).filter(TaskServiceWrapper::isValid);
    }

    private void doAddTaskService(TaskServiceWrapper taskServiceWrapper, TaskServiceWrapper oldTaskService) {
        if (taskServiceWrapper.getIdentityType() == IdentityTypeEnum.SERVICE_TASK) {
            AssertUtil.isNull(oldTaskService.getMethodWrapper(null));
            AssertUtil.isNull(oldTaskService.getTarget(null));

            MethodWrapper methodWrapper = taskServiceWrapper.getMethodWrapper(null);
            TaskComponentRegisterProxy target = taskServiceWrapper.getTarget(null);
            ((AbilityTaskServiceWrapper) oldTaskService).setMethodWrapper(methodWrapper);
            ((AbilityTaskServiceWrapper) oldTaskService).setTarget(target);
        } else {
            ((RootTaskServiceWrapper) oldTaskService).addTaskAbility(taskServiceWrapper);
        }
    }

    private void doAddTaskServiceFirst(TaskServiceWrapper taskServiceWrapper) {
        if (taskServiceWrapper.getIdentityType() == IdentityTypeEnum.SERVICE_TASK_ABILITY) {
            RootTaskServiceWrapper newWrapper = new RootTaskServiceWrapper(taskServiceWrapper.getName());
            newWrapper.addTaskAbility(taskServiceWrapper);
            taskServiceWrapper = newWrapper;
        }
        taskServiceMap.put(taskServiceWrapper.getName(), (RootTaskServiceWrapper) taskServiceWrapper);
    }
}
