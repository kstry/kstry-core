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

import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.task.TaskComponentRegister;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.ProxyUtil;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author lykan
 */
public class SpringTaskComponentRepository extends BasicTaskComponentRepository implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Override
    public void init() {
        Map<String, TaskComponentRegister> beansOfClass = applicationContext.getBeansOfType(TaskComponentRegister.class);
        Map<String, Object> beansOfClassAnnotation = applicationContext.getBeansWithAnnotation(TaskComponent.class);

        Set<Object> objSet = Sets.newHashSet();
        objSet.addAll(beansOfClass.values());
        objSet.addAll(beansOfClassAnnotation.values());
        GlobalUtil.sortObjExtends(objSet).forEach(target -> {
            if (target instanceof TaskComponentRegister) {
                TaskComponentRegister v = (TaskComponentRegister) target;
                AssertUtil.notBlank(v.getName(), ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY, "TaskComponentRegister name cannot be empty!");
                doInit(v, v.getClass(), v.getName());
                return;
            }
            Class<?> targetClass = ProxyUtil.noneProxyClass(target);
            TaskComponent taskComponent = AnnotationUtils.findAnnotation(targetClass, TaskComponent.class);
            AssertUtil.notNull(taskComponent);
            AssertUtil.notBlank(taskComponent.name(), ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY, "TaskComponent name cannot be empty!");
            doInit(target, targetClass, taskComponent.name());
        });
    }

    @Override
    protected MethodWrapper methodWrapperProcessor(MethodWrapper methodWrapper) {
        if (methodWrapper == null) {
            return null;
        }

        List<MethodWrapper.ParamInjectDef> paramInjectDefs = methodWrapper.getParamInjectDefs();
        if (CollectionUtils.isEmpty(paramInjectDefs)) {
            return methodWrapper;
        }

        paramInjectDefs.forEach(def -> {
            if (def == null || !def.isSpringInitialization()) {
                return;
            }

            ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
            BeanDefinition beanDefinition = new RootBeanDefinition(def.getParamType());
            beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            beanDefinition.setLazyInit(true);
            beanFactory.registerBeanDefinition(def.getParamType().getName(), beanDefinition);
        });
        return methodWrapper;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
