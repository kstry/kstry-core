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

import cn.kstry.framework.core.annotation.EnableKstry;
import cn.kstry.framework.core.constant.ConfigPropertyNameConstant;
import cn.kstry.framework.core.container.processor.ImmutablePostProcessor;
import cn.kstry.framework.core.container.processor.MarkIndexPostProcessor;
import cn.kstry.framework.core.container.processor.RearrangeFlowPostProcessor;
import cn.kstry.framework.core.container.processor.TileSubProcessPostProcessor;
import cn.kstry.framework.core.container.processor.VerifyFlowPostProcessor;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.enums.ConfigTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.kv.*;
import cn.kstry.framework.core.monitor.ThreadPoolMonitor;
import cn.kstry.framework.core.resource.config.ClassPathConfigResource;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.resource.config.PropertiesConfigResource;
import cn.kstry.framework.core.role.BusinessRole;
import cn.kstry.framework.core.role.BusinessRoleRegister;
import cn.kstry.framework.core.role.BusinessRoleRepository;
import cn.kstry.framework.core.util.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lykan
 */
public class KstryContextResolver implements ApplicationContextAware, InitializingBean {

    private ConfigurableApplicationContext applicationContext;

    @Bean
    @Conditional(ClassPathResourceCondition.class)
    public ConfigResource getClassPathStartEventResource() {
        return new ClassPathConfigResource(getBpmnPath(this.applicationContext));
    }

    @Bean
    @Conditional(PropertiesResourceCondition.class)
    public ConfigResource getPropertiesResource() {
        return new PropertiesConfigResource(getPropertiesPath(this.applicationContext));
    }

    @Bean
    public StartEventContainer getStartEventRepository() {
        Map<String, ConfigResource> beansOfType = this.applicationContext.getBeansOfType(ConfigResource.class);
        Collection<ConfigResource> resources = beansOfType.values();

        BasicStartEventContainer startEventContainer = new BasicStartEventContainer(resources);
        startEventContainer.registerStartEventPostProcessor(new VerifyFlowPostProcessor());
        startEventContainer.registerStartEventPostProcessor(new RearrangeFlowPostProcessor());
        startEventContainer.registerStartEventPostProcessor(new MarkIndexPostProcessor());
        startEventContainer.registerStartEventPostProcessor(new TileSubProcessPostProcessor());
        startEventContainer.registerStartEventPostProcessor(new ImmutablePostProcessor());
        startEventContainer.initStartEvent();
        return startEventContainer;
    }

    @Bean
    @Conditional(MissingPropertySourcesPlaceholderConfigurer.class)
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public TaskContainer getTaskComponentContainer() {
        return new CustomRoleComponentRepository();
    }

    @Bean
    @Conditional(KvSelectorCondition.class)
    public KvSelector getKvSelector() {
        initKValue();
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

    @Bean
    @Conditional(MissingTaskThreadPoolExecutor.class)
    public TaskThreadPoolExecutor getTaskThreadPoolExecutor() {
        return TaskThreadPoolExecutor.buildDefaultExecutor();
    }

    @Bean(destroyMethod = "destroy")
    public StoryEngine getFlowEngine(StartEventContainer startEventContainer, TaskContainer taskContainer, TaskThreadPoolExecutor taskThreadPoolExecutor) {
        Map<String, BusinessRoleRegister> businessRoleRegisterMap = this.applicationContext.getBeansOfType(BusinessRoleRegister.class);
        List<BusinessRole> businessRoleList = businessRoleRegisterMap.values().stream()
                .map(BusinessRoleRegister::register).flatMap(Collection::stream).collect(Collectors.toList());
        BusinessRoleRepository businessRoleRepository = new BusinessRoleRepository(businessRoleList);
        return new StoryEngine(businessRoleRepository, startEventContainer, taskThreadPoolExecutor, taskContainer, def -> {
            AssertUtil.notNull(def);
            if (def.isSpringInitialization()) {
                return applicationContext.getBean(def.getParamType());
            }
            return ElementParserUtil.newInstance(def.getParamType()).orElse(null);
        });
    }

    @Bean
    @Conditional(ThreadPoolMonitorCondition.class)
    public ThreadPoolMonitor getThreadPoolMonitor(TaskThreadPoolExecutor taskThreadPoolExecutor) {
        return new ThreadPoolMonitor("kstry-task-thread-pool", taskThreadPoolExecutor);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = GlobalUtil.transferNotEmpty(applicationContext, ConfigurableApplicationContext.class);
    }

    @Override
    public void afterPropertiesSet() {
        PropertyUtil.initGlobalProperties(applicationContext.getEnvironment());
    }

    private static String getBpmnPath(ListableBeanFactory beanFactory) {
        EnableKstry enableKstryAnn = getEnableKstryAnn(beanFactory);
        return enableKstryAnn.bpmnPath();
    }

    private static String getPropertiesPath(ListableBeanFactory beanFactory) {
        EnableKstry enableKstryAnn = getEnableKstryAnn(beanFactory);
        return enableKstryAnn.propertiesPath();
    }

    private static EnableKstry getEnableKstryAnn(ListableBeanFactory beanFactory) {
        Map<String, Object> enableKstryMap = beanFactory.getBeansWithAnnotation(EnableKstry.class);
        AssertUtil.oneSize(enableKstryMap.values(), ExceptionEnum.ENABLE_KSTRY_NUMBER_ERROR);

        Object target = enableKstryMap.values().iterator().next();
        Class<?> targetClass = ProxyUtil.noneProxyClass(target);
        return AnnotationUtils.findAnnotation(targetClass, EnableKstry.class);
    }

    private static class ClassPathResourceCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String bpmnPath = getBpmnPath(context.getBeanFactory());
            return StringUtils.isNotBlank(bpmnPath);
        }
    }

    private static class PropertiesResourceCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String propertiesPath = getPropertiesPath(context.getBeanFactory());
            return StringUtils.isNotBlank(propertiesPath);
        }
    }

    private static class MissingTaskThreadPoolExecutor implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Map<String, TaskThreadPoolExecutor> beansOfType = context.getBeanFactory().getBeansOfType(TaskThreadPoolExecutor.class);
            return MapUtils.isEmpty(beansOfType);
        }
    }

    private static class MissingPropertySourcesPlaceholderConfigurer implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Map<String, PropertySourcesPlaceholderConfigurer> beansOfType = context.getBeanFactory().getBeansOfType(PropertySourcesPlaceholderConfigurer.class);
            return MapUtils.isEmpty(beansOfType);
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

    private void initKValue() {
        Map<String, ConfigResource> beansOfType = applicationContext.getBeansOfType(ConfigResource.class);
        Collection<ConfigResource> resources = beansOfType.values();
        List<ConfigResource> propConfigs = resources.stream()
                .filter(resource -> resource.getConfigType() == ConfigTypeEnum.PROPERTIES).collect(Collectors.toList());
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        KValueUtil.initKValue(propConfigs, activeProfiles, applicationContext.getBeanFactory());
    }
}
