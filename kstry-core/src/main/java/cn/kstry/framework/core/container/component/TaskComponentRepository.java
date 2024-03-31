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
package cn.kstry.framework.core.container.component;

import cn.kstry.framework.core.annotation.CustomRole;
import cn.kstry.framework.core.annotation.TaskInstruct;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.constant.GlobalConstant;
import cn.kstry.framework.core.container.task.TaskServiceWrapper;
import cn.kstry.framework.core.container.task.impl.AbilityTaskServiceWrapper;
import cn.kstry.framework.core.container.task.impl.TaskComponentProxy;
import cn.kstry.framework.core.container.task.impl.TaskComponentRegisterWrapper;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.resource.service.ServiceNodeResourceItem;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.TaskServiceUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
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
    private final Map<String, TaskComponentRegisterWrapper> taskComponentWrapperMap = Maps.newHashMap();

    /**
     * 被注册的服务节点资源
     */
    private final Map<ServiceNodeResource, MethodWrapper> registeredServiceNodeResource = Maps.newHashMap();

    /**
     * 任务指令容器
     */
    private final Map<String, ServiceNodeResource> taskInstructResourceMap = Maps.newHashMap();

    @Override
    public Optional<TaskServiceDef> getTaskServiceDef(String componentName, String serviceName, Role role) {
        if (StringUtils.isBlank(componentName)) {
            return Optional.empty();
        }
        TaskComponentRegisterWrapper taskComponentWrapper = taskComponentWrapperMap.get(componentName);
        if (taskComponentWrapper == null) {
            return Optional.empty();
        }

        Optional<TaskServiceWrapper> taskServiceOptional = taskComponentWrapper.getTaskService(serviceName, role);
        return taskServiceOptional.map(wrapper -> new TaskServiceDef(wrapper.getServiceNodeResource(), wrapper.getName(), wrapper.getMethodWrapper(), wrapper.getTarget()));
    }

    protected void doInit(Object target, Class<?> targetClass, String taskComponentName, boolean scanSuper) {
        Method[] taskServiceMethods = MethodUtils.getMethodsWithAnnotation(targetClass, TaskService.class, false, false);
        List<Method> taskServiceMethodList = filterTaskServiceMethods(taskServiceMethods, targetClass, scanSuper);
        if (CollectionUtils.isEmpty(taskServiceMethodList)) {
            return;
        }
        TaskComponentProxy targetObj = new TaskComponentProxy(target, taskComponentName);
        TaskComponentRegisterWrapper taskComponentWrapper = taskComponentWrapperMap.computeIfAbsent(taskComponentName, TaskComponentRegisterWrapper::new);
        taskServiceMethodList.forEach(method -> {
            boolean isCustomRole = method.getAnnotation(CustomRole.class) != null;
            TaskService annotation = method.getAnnotation(TaskService.class);
            AssertUtil.notNull(annotation);
            String taskServiceName = StringUtils.isBlank(annotation.name()) ? method.getName() : annotation.name();
            AssertUtil.notBlank(taskServiceName, ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY, "TaskService name cannot be empty! methodName: {}", method.getName());
            ServiceNodeResourceItem serviceNodeResource = new ServiceNodeResourceItem(targetObj.getName(), taskServiceName, annotation.ability(), annotation.desc());
            AssertUtil.notTrue(registeredServiceNodeResource.containsKey(serviceNodeResource), ExceptionEnum.COMPONENT_DUPLICATION_ERROR,
                    "TaskService with the same identity is not allowed to be set repeatedly! identity: {}", serviceNodeResource.getIdentityId());
            TaskInstructWrapper taskInstruct = getTaskInstructWrapper(method, taskServiceName).orElse(null);
            if (taskInstruct != null) {
                AssertUtil.notBlank(taskInstruct.getName(), ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY, "TaskInstruct name cannot be empty! methodName: {}", method.getName());
                AssertUtil.notTrue(taskInstructResourceMap.containsKey(taskInstruct.getName()), ExceptionEnum.COMPONENT_DUPLICATION_ERROR,
                        "TaskInstruct with the same name is not allowed to be set repeatedly! instruct: {}", taskInstruct.getName());
                taskInstructResourceMap.put(taskInstruct.getName(), serviceNodeResource);
            }
            NoticeAnnotationWrapper noticeMethodSpecify = new NoticeAnnotationWrapper(method);
            MethodWrapper methodWrapper = new MethodWrapper(method, annotation, noticeMethodSpecify, taskInstruct, isCustomRole);
            taskComponentWrapper.addTaskService(new AbilityTaskServiceWrapper(targetObj, methodWrapperProcessor(methodWrapper), serviceNodeResource));
            registeredServiceNodeResource.put(serviceNodeResource, methodWrapper);
            LOGGER.debug("Service node resource items are resolved. identity: {}", serviceNodeResource.getIdentityId());
        });
    }

    private List<Method> filterTaskServiceMethods(Method[] taskServiceMethods, Class<?> targetClass, boolean scanSuper) {
        if (ArrayUtils.isEmpty(taskServiceMethods)) {
            return Lists.newArrayList();
        }
        List<Method> taskServiceMethodList = Arrays.stream(taskServiceMethods).filter(tsm -> {
            if (scanSuper) {
                return true;
            }
            return targetClass.isAssignableFrom(tsm.getDeclaringClass());
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(taskServiceMethodList)) {
            return Lists.newArrayList();
        }

        List<Method> methodList = Lists.newArrayList();
        taskServiceMethodList.stream().collect(Collectors.groupingBy(m -> {
            TaskService annotation = m.getAnnotation(TaskService.class);
            String name = StringUtils.isBlank(annotation.name()) ? StringUtils.uncapitalize(m.getName()) : annotation.name();
            return TaskServiceUtil.joinName(name, annotation.ability());
        })).forEach((ts, list) -> {
            if (list.size() <= 1) {
                methodList.add(list.get(0));
                return;
            }

            boolean methodNameEquals = list.stream().map(Method::getName).distinct().count() == 1;
            boolean paramsCountEquals = list.stream().map(Method::getParameterCount).distinct().count() == 1;
            AssertUtil.isTrue(methodNameEquals && paramsCountEquals, ExceptionEnum.COMPONENT_DUPLICATION_ERROR,
                    "TaskService with the same identity is not allowed to be set repeatedly! methodNames: {}", list.stream().map(Method::getName).collect(Collectors.toList()));

            list.sort((m1, m2) -> {
                if (!ElementParserUtil.isAssignable(m1.getReturnType(), m2.getReturnType())) {
                    return -1;
                }
                Class<?>[] p1List = m1.getParameterTypes();
                Class<?>[] p2List = m2.getParameterTypes();
                if (p1List.length == 0) {
                    return 0;
                }
                for (int i = 0; i < p1List.length; i++) {
                    if (!ElementParserUtil.isAssignable(p1List[i], p2List[i])) {
                        return -1;
                    }
                }
                return 0;
            });

            LOGGER.info("[{}] TaskService with the same identity is repeatedly defined! select the most accurate method as the serviceNode. methodName: {}, returnType: {}, params: {}",
                    ExceptionEnum.COMPONENT_DUPLICATION_ERROR.getExceptionCode(),
                    list.get(0).getName(), list.get(0).getReturnType().getName(), Stream.of(list.get(0).getParameterTypes()).map(Class::getName).collect(Collectors.toList()));
            methodList.add(list.get(0));
        });
        return methodList;
    }

    @Override
    public Optional<ServiceNodeResource> getServiceNodeResourceByInstruct(String instruction) {
        if (StringUtils.isBlank(instruction)) {
            return Optional.empty();
        }
        return Optional.ofNullable(taskInstructResourceMap.get(instruction));
    }

    @Override
    public Set<ServiceNodeResource> getServiceNodeResource() {
        return Sets.newHashSet(registeredServiceNodeResource.keySet());
    }

    protected MethodWrapper methodWrapperProcessor(MethodWrapper methodWrapper) {
        return methodWrapper;
    }

    protected void repositoryPostProcessor() {
        registeredServiceNodeResource.forEach((k, v) -> {
            InvokeProperties invokeProperties = v.getInvokeProperties();
            ServiceNodeResource demotionResource = invokeProperties.getDemotionResource();
            if (demotionResource == null) {
                return;
            }

            MethodWrapper methodWrapper = registeredServiceNodeResource.get(demotionResource);
            if (methodWrapper == null) {
                LOGGER.warn("[{}] Service node not matched to demotion policy! identityId: {}, demotion: {}",
                        ExceptionEnum.DEMOTION_DEFINITION_ERROR.getExceptionCode(), k.getIdentityId(), demotionResource.getIdentityId());
                invokeProperties.invalidDemotion();
                return;
            }

            Class<?> returnType = methodWrapper.getMethod().getReturnType();
            if (!Objects.equals(GlobalConstant.VOID, returnType.getName()) && !ElementParserUtil.isAssignable(returnType, v.getMethod().getReturnType())) {
                LOGGER.warn("[{}] The return type of the demotion method and the main method do not match! identityId: {}, demotion: {}",
                        ExceptionEnum.DEMOTION_DEFINITION_ERROR.getExceptionCode(), k.getIdentityId(), demotionResource.getIdentityId());
                invokeProperties.invalidDemotion();
            }
        });
    }

    private Optional<TaskInstructWrapper> getTaskInstructWrapper(Method method, String taskService) {
        TaskInstruct annotation = method.getAnnotation(TaskInstruct.class);
        if (annotation == null) {
            return Optional.empty();
        }
        return Optional.of(new TaskInstructWrapper(annotation, taskService));
    }
}
