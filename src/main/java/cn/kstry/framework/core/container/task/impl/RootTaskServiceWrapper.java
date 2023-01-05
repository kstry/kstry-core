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
package cn.kstry.framework.core.container.task.impl;

import cn.kstry.framework.core.container.task.RootTaskService;
import cn.kstry.framework.core.container.task.TaskServiceWrapper;
import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.enums.ServiceNodeType;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.identity.BasicIdentity;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.role.ServiceTaskRole;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.TaskServiceUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @author lykan
 */
public class RootTaskServiceWrapper extends BasicIdentity implements RootTaskService {

    /**
     * 服务能力集合
     */
    private final Set<TaskServiceWrapper> taskServiceAbilitySet = Sets.newHashSet();

    /**
     * 服务节点包装
     */
    private TaskServiceWrapper taskServiceWrapper;

    /**
     * 读写锁
     */
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public RootTaskServiceWrapper(ServiceNodeResource resource) {
        super(TaskServiceUtil.joinName(resource.getComponentName(), resource.getServiceName()), IdentityTypeEnum.SERVICE_TASK);
    }

    @Override
    public void addTaskService(TaskServiceWrapper taskService) {
        AssertUtil.notNull(taskService);

        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            ServiceNodeResource serviceNodeResource = taskService.getServiceNodeResource();
            if (serviceNodeResource.getServiceNodeType() == ServiceNodeType.SERVICE_TASK) {
                AssertUtil.isNull(this.taskServiceWrapper);
                this.taskServiceWrapper = taskService;
            } else if (serviceNodeResource.getServiceNodeType() == ServiceNodeType.SERVICE_TASK_ABILITY) {
                AssertUtil.notTrue(taskServiceAbilitySet.contains(taskService));
                taskServiceAbilitySet.add(taskService);
            } else {
                throw ExceptionUtil.buildException(null, ExceptionEnum.SYSTEM_ERROR, null);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Optional<TaskServiceWrapper> getTaskService(String serviceName, Role role) {
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            List<TaskServiceWrapper> taskServiceList = Lists.newArrayList();
            List<TaskServiceWrapper> abilityList = taskServiceAbilitySet.stream().filter(ability -> ability.match(role)).collect(Collectors.toList());
            taskServiceList.addAll(abilityList);

            boolean abilityMatchSuccess = (role instanceof ServiceTaskRole) && CollectionUtils.isNotEmpty(taskServiceList);
            if (abilityMatchSuccess && taskServiceWrapper != null && role.allowedUseResource(taskServiceWrapper.getServiceNodeResource())) {
                taskServiceList.add(taskServiceWrapper);
            }
            if (!abilityMatchSuccess && taskServiceWrapper != null && taskServiceWrapper.match(role)) {
                taskServiceList.add(taskServiceWrapper);
            }
            if (CollectionUtils.isEmpty(taskServiceList)) {
                return Optional.empty();
            }
            AssertUtil.oneSize(taskServiceList, ExceptionEnum.TASK_SERVICE_MATCH_ERROR,
                    "There must be one and only one ability matched in the execution! serviceName: {}, abilityId: {}", () -> Lists.newArrayList(serviceName,
                            JSON.toJSONString(taskServiceList.stream().map(TaskServiceWrapper::getIdentityId).collect(Collectors.toList()))));
            return Optional.of(taskServiceList.get(0)).map(tsw -> {
                AssertUtil.equals(serviceName, tsw.getName());
                return tsw;
            });
        } finally {
            readLock.unlock();
        }
    }
}
