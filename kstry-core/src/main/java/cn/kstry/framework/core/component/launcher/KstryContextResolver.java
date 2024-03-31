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
package cn.kstry.framework.core.component.launcher;

import cn.kstry.framework.core.component.bpmn.SerializeProcessParser;
import cn.kstry.framework.core.component.conversion.TypeConverter;
import cn.kstry.framework.core.component.conversion.TypeConverterProcessor;
import cn.kstry.framework.core.component.dynamic.KValueDynamicComponent;
import cn.kstry.framework.core.component.dynamic.RoleDynamicComponent;
import cn.kstry.framework.core.component.jsprocess.transfer.JsonSerializeProcessParser;
import cn.kstry.framework.core.constant.ConfigPropertyNameConstant;
import cn.kstry.framework.core.constant.GlobalConstant;
import cn.kstry.framework.core.container.ComponentLifecycle;
import cn.kstry.framework.core.container.component.SpringTaskComponentRepository;
import cn.kstry.framework.core.container.component.TaskContainer;
import cn.kstry.framework.core.container.element.BasicStartEventContainer;
import cn.kstry.framework.core.container.element.StartEventContainer;
import cn.kstry.framework.core.container.processor.StartEventProcessor;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.StoryEngineModule;
import cn.kstry.framework.core.engine.interceptor.SubProcessInterceptor;
import cn.kstry.framework.core.engine.interceptor.SubProcessInterceptorRepository;
import cn.kstry.framework.core.engine.interceptor.TaskInterceptor;
import cn.kstry.framework.core.engine.interceptor.TaskInterceptorRepository;
import cn.kstry.framework.core.engine.thread.TaskServiceExecutor;
import cn.kstry.framework.core.engine.thread.hook.ThreadSwitchHookProcessor;
import cn.kstry.framework.core.enums.ExecutorType;
import cn.kstry.framework.core.kv.*;
import cn.kstry.framework.core.monitor.FieldSerializeTracking;
import cn.kstry.framework.core.monitor.SerializeTracking;
import cn.kstry.framework.core.monitor.ThreadPoolMonitor;
import cn.kstry.framework.core.resource.factory.KValueFactory;
import cn.kstry.framework.core.resource.factory.StartEventFactory;
import cn.kstry.framework.core.role.BusinessRole;
import cn.kstry.framework.core.role.BusinessRoleRegister;
import cn.kstry.framework.core.role.BusinessRoleRepository;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.KValueUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kstry 核心组件加载
 *
 * @author lykan
 */
public class KstryContextResolver extends BasicLauncher {

    @Bean
    public StartEventContainer getStartEventRepository(StartEventFactory startEventFactory, StartEventProcessor startEventProcessor) {
        return new BasicStartEventContainer(startEventFactory, startEventProcessor);
    }

    @Bean(initMethod = ComponentLifecycle.INIT, destroyMethod = ComponentLifecycle.DESTROY)
    public TaskContainer getTaskComponentContainer() {
        return new SpringTaskComponentRepository();
    }

    @Bean
    @Conditional(KvSelectorCondition.class)
    public KvSelector getKvSelector(KValueFactory kValueFactory) {
        initKValue(kValueFactory);
        BasicKvSelector kvSelector = new BasicKvSelector();
        Map<String, BasicKValue> beansOfType = applicationContext.getBeansOfType(BasicKValue.class);
        beansOfType.forEach((k, v) -> kvSelector.addKValue(v));
        return kvSelector;
    }

    @Bean
    public KvAbility getKvAbility(KvSelector kvSelector, KValueDynamicComponent kValueDynamicComponent) {
        AssertUtil.anyNotNull(kvSelector, kValueDynamicComponent);
        return new DynamicKvAbility(kvSelector, kValueDynamicComponent);
    }

    @Bean(destroyMethod = ComponentLifecycle.DESTROY)
    @Conditional(MissingTaskServiceExecutor.class)
    public TaskServiceExecutor getTaskServiceExecutor() {
        return TaskServiceExecutor.buildDefaultExecutor(ExecutorType.TASK, "kstry-task-thread-pool");
    }

    @Bean(destroyMethod = ComponentLifecycle.DESTROY)
    @Conditional(MissingMethodServiceExecutor.class)
    public TaskServiceExecutor getMethodServiceExecutor() {
        return TaskServiceExecutor.buildDefaultExecutor(ExecutorType.METHOD, "kstry-method-thread-pool");
    }

    @Bean(destroyMethod = ComponentLifecycle.DESTROY)
    @Conditional(MissingIteratorServiceExecutor.class)
    public TaskServiceExecutor getIteratorServiceExecutor() {
        return TaskServiceExecutor.buildDefaultExecutor(ExecutorType.ITERATOR, "kstry-iterator-thread-pool");
    }

    @Bean
    @Conditional(MissingSerializeTracking.class)
    public SerializeTracking getSerializeTracking() {
        return new FieldSerializeTracking();
    }

    @Bean
    public TypeConverterProcessor getTypeConverterProcessor(List<TypeConverter<?, ?>> typeConverterList) {
        return new TypeConverterProcessor(typeConverterList);
    }

    @Bean
    public StoryEngine getFlowEngine(StartEventContainer startEventContainer, RoleDynamicComponent roleDynamicComponent, TaskContainer taskContainer,
                                     List<TaskServiceExecutor> taskServiceExecutor, ThreadSwitchHookProcessor threadSwitchHookProcessor,
                                     SerializeProcessParser<?> serializeProcessParser, SerializeTracking serializeTracking, TypeConverterProcessor typeConverterProcessor) {
        StoryEngineModule storyEngineModule = new StoryEngineModule(taskServiceExecutor, startEventContainer, taskContainer, def -> {
            AssertUtil.notNull(def);
            if (def.isSpringInitialization()) {
                return applicationContext.getBean(def.getParamType());
            }
            return ElementParserUtil.newInstance(def.getParamType()).orElse(null);
        }, getSubProcessInterceptorRepository(), getTaskInterceptorRepository(),
                threadSwitchHookProcessor, applicationContext, serializeProcessParser, serializeTracking, typeConverterProcessor);
        return new StoryEngine(storyEngineModule, getBusinessRoleRepository(roleDynamicComponent));
    }

    @Bean
    @Conditional(ThreadPoolMonitorCondition.class)
    public ThreadPoolMonitor getThreadPoolMonitor(List<TaskServiceExecutor> taskServiceExecutor) {
        return new ThreadPoolMonitor(taskServiceExecutor);
    }

    @Bean
    @Conditional(MissingSerializeProcessParser.class)
    public SerializeProcessParser<?> serializeProcessParser() {
        return new JsonSerializeProcessParser();
    }

    private void initKValue(KValueFactory kValueFactory) {
        AssertUtil.notNull(kValueFactory);
        List<KValue> kValueList = kValueFactory.getResourceList();
        Map<String, BasicKValue> kValueMap = KValueUtil.getKValueMap(kValueList, Arrays.asList(applicationContext.getEnvironment().getActiveProfiles()));
        if (MapUtils.isNotEmpty(kValueMap)) {
            kValueMap.forEach((k, v) -> applicationContext.getBeanFactory().registerSingleton(GlobalUtil.format(GlobalConstant.KV_SCOPE_DEFAULT_BEAN_NAME, v.getScope()), v));
        }
    }

    private BusinessRoleRepository getBusinessRoleRepository(RoleDynamicComponent roleDynamicComponent) {
        Map<String, BusinessRoleRegister> businessRoleRegisterMap = this.applicationContext.getBeansOfType(BusinessRoleRegister.class);
        List<BusinessRole> businessRoleList = businessRoleRegisterMap.values()
                .stream().map(BusinessRoleRegister::register).flatMap(Collection::stream).collect(Collectors.toList());
        return new BusinessRoleRepository(roleDynamicComponent, businessRoleList);
    }

    private SubProcessInterceptorRepository getSubProcessInterceptorRepository() {
        Map<String, SubProcessInterceptor> subProcessInterceptorMap = this.applicationContext.getBeansOfType(SubProcessInterceptor.class);
        return new SubProcessInterceptorRepository(subProcessInterceptorMap.values());
    }

    private TaskInterceptorRepository getTaskInterceptorRepository() {
        Map<String, TaskInterceptor> taskInterceptorMap = this.applicationContext.getBeansOfType(TaskInterceptor.class);
        return new TaskInterceptorRepository(taskInterceptorMap.values());
    }

    private static class MissingTaskServiceExecutor implements Condition {

        @Override
        public boolean matches(ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
            if (context.getBeanFactory() == null) {
                return false;
            }
            Map<String, TaskServiceExecutor> beansOfType = context.getBeanFactory().getBeansOfType(TaskServiceExecutor.class);
            return CollectionUtils.isEmpty(beansOfType.values().stream().filter(b -> b.getExecutorType() == ExecutorType.TASK).collect(Collectors.toList()));
        }
    }

    private static class MissingMethodServiceExecutor implements Condition {

        @Override
        public boolean matches(ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
            if (context.getBeanFactory() == null) {
                return false;
            }
            Map<String, TaskServiceExecutor> beansOfType = context.getBeanFactory().getBeansOfType(TaskServiceExecutor.class);
            return CollectionUtils.isEmpty(beansOfType.values().stream().filter(b -> b.getExecutorType() == ExecutorType.METHOD).collect(Collectors.toList()));
        }
    }

    private static class MissingIteratorServiceExecutor implements Condition {
        @Override
        public boolean matches(ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
            if (context.getBeanFactory() == null) {
                return false;
            }
            Map<String, TaskServiceExecutor> beansOfType = context.getBeanFactory().getBeansOfType(TaskServiceExecutor.class);
            return CollectionUtils.isEmpty(beansOfType.values().stream().filter(b -> b.getExecutorType() == ExecutorType.ITERATOR).collect(Collectors.toList()));
        }
    }

    private static class MissingSerializeProcessParser implements Condition {

        @Override
        public boolean matches(ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
            if (context.getBeanFactory() == null) {
                return false;
            }
            return MapUtils.isEmpty(context.getBeanFactory().getBeansOfType(SerializeProcessParser.class));
        }
    }

    private static class KvSelectorCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
            if (context.getBeanFactory() == null) {
                return false;
            }
            Map<String, KvSelector> beansOfType = context.getBeanFactory().getBeansOfType(KvSelector.class);
            return MapUtils.isEmpty(beansOfType);
        }
    }

    private static class ThreadPoolMonitorCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
            String enable = context.getEnvironment().getProperty(ConfigPropertyNameConstant.KSTRY_THREAD_POOL_MONITOR_ENABLE);
            return BooleanUtils.isNotFalse(BooleanUtils.toBooleanObject(enable));
        }
    }

    private static class MissingSerializeTracking implements Condition {

        @Override
        public boolean matches(ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
            if (context.getBeanFactory() == null) {
                return false;
            }
            return MapUtils.isEmpty(context.getBeanFactory().getBeansOfType(SerializeTracking.class));
        }
    }
}
