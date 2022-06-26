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
package cn.kstry.framework.core.component.launcher;

import cn.kstry.framework.core.constant.ConfigPropertyNameConstant;
import cn.kstry.framework.core.container.ComponentLifecycle;
import cn.kstry.framework.core.container.component.CustomRoleComponentRepository;
import cn.kstry.framework.core.container.component.TaskContainer;
import cn.kstry.framework.core.container.element.BasicStartEventContainer;
import cn.kstry.framework.core.container.element.StartEventContainer;
import cn.kstry.framework.core.container.processor.StartEventPostProcessor;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.StoryEngineModule;
import cn.kstry.framework.core.engine.interceptor.SubProcessInterceptor;
import cn.kstry.framework.core.engine.interceptor.SubProcessInterceptorRepository;
import cn.kstry.framework.core.engine.thread.TaskThreadPoolExecutor;
import cn.kstry.framework.core.enums.ExecutorType;
import cn.kstry.framework.core.kv.*;
import cn.kstry.framework.core.monitor.ThreadPoolMonitor;
import cn.kstry.framework.core.resource.factory.KValueFactory;
import cn.kstry.framework.core.resource.factory.StartEventFactory;
import cn.kstry.framework.core.role.BusinessRole;
import cn.kstry.framework.core.role.BusinessRoleRegister;
import cn.kstry.framework.core.role.BusinessRoleRepository;
import cn.kstry.framework.core.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.OrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Kstry 核心组件加载
 *
 * @author lykan
 */
public class KstryContextResolver implements ApplicationContextAware, InitializingBean {

    private ConfigurableApplicationContext applicationContext;

    @Bean
    public StartEventContainer getStartEventRepository(StartEventFactory startEventFactory) {
        BasicStartEventContainer startEventContainer = new BasicStartEventContainer(startEventFactory);

        List<StartEventPostProcessor> startEventPostProcessorList = new ArrayList<>(applicationContext.getBeansOfType(StartEventPostProcessor.class).values());
        OrderComparator.sort(startEventPostProcessorList);
        startEventPostProcessorList.forEach(startEventContainer::registerStartEventPostProcessor);
        startEventContainer.refreshStartEvent();
        return startEventContainer;
    }

    @Bean(initMethod = ComponentLifecycle.INIT, destroyMethod = ComponentLifecycle.DESTROY)
    public TaskContainer getTaskComponentContainer() {
        return new CustomRoleComponentRepository();
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
    public KvAbility getKvAbility(KvSelector kvSelector) {
        AssertUtil.notNull(kvSelector);
        return new BasicKvAbility(kvSelector);
    }

    @Bean(destroyMethod = ComponentLifecycle.DESTROY)
    @Conditional(MissingTaskThreadPoolExecutor.class)
    public TaskThreadPoolExecutor getTaskThreadPoolExecutor() {
        return TaskThreadPoolExecutor.buildDefaultExecutor(ExecutorType.TASK, "kstry-task-thread-pool");
    }

    @Bean(destroyMethod = ComponentLifecycle.DESTROY)
    @Conditional(MissingMethodThreadPoolExecutor.class)
    public TaskThreadPoolExecutor getMethodThreadPoolExecutor() {
        return TaskThreadPoolExecutor.buildDefaultExecutor(ExecutorType.METHOD, "kstry-method-thread-pool");
    }

    @Bean
    public StoryEngine getFlowEngine(StartEventContainer startEventContainer,
                                     TaskContainer taskContainer, List<TaskThreadPoolExecutor> taskThreadPoolExecutor) {
        StoryEngineModule storyEngineModule = new StoryEngineModule(taskThreadPoolExecutor, startEventContainer, taskContainer, def -> {
            AssertUtil.notNull(def);
            if (def.isSpringInitialization()) {
                return applicationContext.getBean(def.getParamType());
            }
            return ElementParserUtil.newInstance(def.getParamType()).orElse(null);
        }, getSubProcessInterceptorRepository());
        return new StoryEngine(storyEngineModule, getBusinessRoleRepository());
    }

    @Bean
    @Conditional(ThreadPoolMonitorCondition.class)
    public ThreadPoolMonitor getThreadPoolMonitor(List<TaskThreadPoolExecutor> taskThreadPoolExecutor) {
        return new ThreadPoolMonitor(taskThreadPoolExecutor);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = GlobalUtil.transferNotEmpty(applicationContext, ConfigurableApplicationContext.class);
    }

    @Override
    public void afterPropertiesSet() {
        PropertyUtil.initGlobalProperties(applicationContext.getEnvironment());
    }

    private void initKValue(KValueFactory kValueFactory) {
        AssertUtil.notNull(kValueFactory);

        List<KValue> kValueList = kValueFactory.getResourceList();
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        KValueUtil.initKValue(kValueList, activeProfiles, applicationContext.getBeanFactory());
    }

    private BusinessRoleRepository getBusinessRoleRepository() {
        Map<String, BusinessRoleRegister> businessRoleRegisterMap = this.applicationContext.getBeansOfType(BusinessRoleRegister.class);
        List<BusinessRole> businessRoleList = businessRoleRegisterMap.values()
                .stream().map(BusinessRoleRegister::register).flatMap(Collection::stream).collect(Collectors.toList());
        return new BusinessRoleRepository(businessRoleList);
    }

    private SubProcessInterceptorRepository getSubProcessInterceptorRepository() {
        Map<String, SubProcessInterceptor> subProcessInterceptorMap = this.applicationContext.getBeansOfType(SubProcessInterceptor.class);
        return new SubProcessInterceptorRepository(subProcessInterceptorMap.values());
    }

    private static class MissingTaskThreadPoolExecutor implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Map<String, TaskThreadPoolExecutor> beansOfType = context.getBeanFactory().getBeansOfType(TaskThreadPoolExecutor.class);
            return CollectionUtils.isEmpty(beansOfType.values().stream().filter(b -> b.getExecutorType() == ExecutorType.TASK).collect(Collectors.toList()));
        }
    }

    private static class MissingMethodThreadPoolExecutor implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Map<String, TaskThreadPoolExecutor> beansOfType = context.getBeanFactory().getBeansOfType(TaskThreadPoolExecutor.class);
            return CollectionUtils.isEmpty(beansOfType.values().stream().filter(b -> b.getExecutorType() == ExecutorType.METHOD).collect(Collectors.toList()));
        }
    }

    private static class KvSelectorCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Map<String, KvSelector> beansOfType = context.getBeanFactory().getBeansOfType(KvSelector.class);
            return MapUtils.isEmpty(beansOfType);
        }
    }

    private static class ThreadPoolMonitorCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String enable = context.getEnvironment().getProperty(ConfigPropertyNameConstant.KSTRY_THREAD_POOL_MONITOR_ENABLE);
            return BooleanUtils.isNotFalse(BooleanUtils.toBooleanObject(enable));
        }
    }
}
