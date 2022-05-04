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

import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.container.task.TaskServiceWrapper;
import cn.kstry.framework.core.container.task.impl.AbilityTaskServiceWrapper;
import cn.kstry.framework.core.container.task.impl.TaskComponentProxy;
import cn.kstry.framework.core.container.task.impl.TaskComponentRegisterWrapper;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.resource.service.ServiceNodeResourceItem;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 服务组件仓库
 *
 * @author lykan
 */
public abstract class TaskComponentRepository implements TaskContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskComponentRepository.class);

    /**
     * 服务节点容器
     */
    private volatile Map<String, TaskComponentRegisterWrapper> taskComponentWrapperMap = Maps.newHashMap();

    /**
     * 被注册的服务节点资源
     */
    private volatile Set<ServiceNodeResource> registeredServiceNodeResource = Sets.newHashSet();

    @Override
    public Optional<TaskServiceDef> getTaskServiceDef(String componentName, String serviceName, Role role) {
        TaskComponentRegisterWrapper taskComponentWrapper = taskComponentWrapperMap.get(componentName);
        if (taskComponentWrapper == null) {
            return Optional.empty();
        }

        Optional<TaskServiceWrapper> taskServiceOptional = taskComponentWrapper.getTaskService(serviceName, role);
        return taskServiceOptional.map(wrapper -> new TaskServiceDef(
                wrapper.getServiceNodeResource(), wrapper.getName(), wrapper.getMethodWrapper(), wrapper.getTarget()));
    }

    protected void doInit(Object target, Class<?> targetClass, String taskComponentName) {
        Method[] taskServiceMethods = MethodUtils.getMethodsWithAnnotation(targetClass, TaskService.class, false, false);
        if (ArrayUtils.isEmpty(taskServiceMethods)) {
            return;
        }

        TaskComponentProxy targetObj = new TaskComponentProxy(target);
        TaskComponentRegisterWrapper taskComponentWrapper = taskComponentWrapperMap.computeIfAbsent(taskComponentName, TaskComponentRegisterWrapper::new);
        Stream.of(taskServiceMethods).forEach(method -> {
            TaskService annotation = method.getAnnotation(TaskService.class);
            AssertUtil.notNull(annotation);
            AssertUtil.notBlank(annotation.name(), ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY,
                    "TaskService name cannot be empty! methodName: {}", method.getName());
            ServiceNodeResourceItem serviceNodeResource = new ServiceNodeResourceItem(targetObj.getName(), annotation.name(), annotation.ability());
            AssertUtil.notTrue(registeredServiceNodeResource.contains(serviceNodeResource), ExceptionEnum.COMPONENT_DUPLICATION_ERROR,
                    "TaskService with the same identity is not allowed to be set repeatedly! identity: {}", serviceNodeResource.getIdentityId());
            NoticeAnnotationWrapper noticeMethodSpecify = new NoticeAnnotationWrapper(method);
            MethodWrapper methodWrapper = new MethodWrapper(method, annotation, noticeMethodSpecify);
            taskComponentWrapper.addTaskService(new AbilityTaskServiceWrapper(targetObj, methodWrapperProcessor(methodWrapper), serviceNodeResource));
            registeredServiceNodeResource.add(serviceNodeResource);
            LOGGER.debug("Service node resource items are resolved. identity: {}", serviceNodeResource.getIdentityId());
        });
    }

    protected MethodWrapper methodWrapperProcessor(MethodWrapper methodWrapper) {
        return methodWrapper;
    }

    @Override
    public void destroy() {
        taskComponentWrapperMap = Maps.newHashMap();
        registeredServiceNodeResource = Sets.newHashSet();
    }
}
