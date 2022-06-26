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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.resource.identity.BasicIdentity;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.container.task.RootTaskService;
import cn.kstry.framework.core.container.task.TaskServiceWrapper;
import cn.kstry.framework.core.util.AssertUtil;

/**
 * 服务组件类包装器
 *
 * @author lykan
 */
public class TaskComponentRegisterWrapper extends BasicIdentity implements RootTaskService {

    /**
     * 保存服务节点包装类
     */
    private final Map<String, RootTaskService> rootTaskServiceMap = Maps.newHashMap();

    /**
     * 读写锁
     */
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public TaskComponentRegisterWrapper(@Nonnull String name) {
        super(name, IdentityTypeEnum.TASK_COMPONENT);
    }

    @Override
    public void addTaskService(TaskServiceWrapper taskServiceWrapper) {
        AssertUtil.notNull(taskServiceWrapper);
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            RootTaskService rootTaskService = rootTaskServiceMap.computeIfAbsent(
                    taskServiceWrapper.getName(), name -> new RootTaskServiceWrapper(taskServiceWrapper.getServiceNodeResource()));
            rootTaskService.addTaskService(taskServiceWrapper);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Optional<TaskServiceWrapper> getTaskService(String name, Role role) {
        if (StringUtils.isBlank(name)) {
            return Optional.empty();
        }
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            RootTaskService rootTaskService = rootTaskServiceMap.get(name);
            if (rootTaskService == null) {
                return Optional.empty();
            }
            return rootTaskService.getTaskService(name, role);
        } finally {
            readLock.unlock();
        }
    }
}
