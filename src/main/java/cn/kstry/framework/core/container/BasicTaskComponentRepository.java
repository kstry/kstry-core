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
package cn.kstry.framework.core.container;

import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.task.TaskComponentRegisterWrapper;
import cn.kstry.framework.core.task.TaskServiceWrapper;
import cn.kstry.framework.core.task.facade.TaskServiceDef;
import cn.kstry.framework.core.task.impl.AbilityTaskServiceWrapper;
import cn.kstry.framework.core.task.impl.DefaultTaskComponentRegisterWrapper;
import cn.kstry.framework.core.task.impl.TaskComponentRegisterProxy;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.TaskServiceUtil;
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
 * @author lykan
 */
public abstract class BasicTaskComponentRepository implements TaskContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicTaskComponentRepository.class);

    private Map<String, TaskComponentRegisterWrapper> taskComponentWrapperMap = Maps.newHashMap();
    private Set<String> registerTaskService = Sets.newHashSet();

    @Override
    public Optional<TaskServiceDef> getTaskServiceDef(String componentName, String serviceName, Role role) {

        TaskComponentRegisterWrapper taskComponentWrapper = taskComponentWrapperMap.get(componentName);
        if (taskComponentWrapper == null) {
            return Optional.empty();
        }

        Optional<TaskServiceWrapper> taskServiceOptional = taskComponentWrapper.getTaskService(serviceName, role);
        return taskServiceOptional.map(
                wrapper -> new TaskServiceDef(wrapper.getName(), wrapper.getMethodWrapper(role), wrapper.getTarget(role))
        );
    }

    protected void doInit(Object target, Class<?> targetClass, String taskComponentName) {
        Method[] taskServiceMethods = MethodUtils.getMethodsWithAnnotation(targetClass, TaskService.class, false, false);
        if (ArrayUtils.isEmpty(taskServiceMethods)) {
            return;
        }

        TaskComponentRegisterProxy targetObj = new TaskComponentRegisterProxy(target);
        TaskComponentRegisterWrapper taskComponentWrapper =
                taskComponentWrapperMap.computeIfAbsent(taskComponentName, DefaultTaskComponentRegisterWrapper::new);
        Stream.of(taskServiceMethods).forEach(method -> {
            TaskService annotation = method.getAnnotation(TaskService.class);
            AssertUtil.notNull(annotation);
            AssertUtil.notBlank(annotation.name(), ExceptionEnum.ANNOTATION_USAGE_ERROR, "TaskService name cannot be empty!");
            String registerTaskServiceKey = TaskServiceUtil.joinName(targetObj.getName(), TaskServiceUtil.joinName(annotation.name(), annotation.ability()));
            if (registerTaskService.contains(registerTaskServiceKey)) {
                LOGGER.warn("TaskService with the same identity is not allowed to be set repeatedly! taskService: {}, target: {}",
                        registerTaskServiceKey, target.getClass().getName());
                return;
            }
            NoticeAnnotationWrapper noticeMethodSpecify = new NoticeAnnotationWrapper(method);
            MethodWrapper methodWrapper = new MethodWrapper(method, annotation, noticeMethodSpecify);
            methodWrapper = methodWrapperProcessor(methodWrapper);

            AbilityTaskServiceWrapper ability = TaskServiceUtil.buildTaskService(annotation.name(), annotation.ability());
            ability.setTarget(targetObj);
            ability.setMethodWrapper(methodWrapper);
            taskComponentWrapper.addTaskService(ability);
            registerTaskService.add(registerTaskServiceKey);
        });
    }

    protected MethodWrapper methodWrapperProcessor(MethodWrapper methodWrapper) {
        return methodWrapper;
    }

    @Override
    public void destroy() {
        taskComponentWrapperMap = Maps.newHashMap();
        registerTaskService = Sets.newHashSet();
    }
}
