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

import cn.kstry.framework.core.annotation.CustomRole;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.role.CustomRoleRegister;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.ProxyUtil;
import com.google.common.collect.Sets;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author lykan
 */
public class CustomRoleComponentRepository extends SpringTaskComponentRepository {

    @Override
    public void init() {
        Map<String, CustomRoleRegister> beansOfClass = applicationContext.getBeansOfType(CustomRoleRegister.class);
        Map<String, Object> beansOfClassAnnotation = applicationContext.getBeansWithAnnotation(CustomRole.class);

        Set<Object> objSet = Sets.newHashSet();
        objSet.addAll(beansOfClass.values());
        objSet.addAll(beansOfClassAnnotation.values());
        GlobalUtil.sortObjExtends(objSet).forEach(target -> {
            if (target instanceof CustomRoleRegister) {
                CustomRoleRegister v = (CustomRoleRegister) target;
                AssertUtil.notBlank(v.getName(), ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY, "CustomRoleRegister name cannot be empty!");
                doInit(v, v.getClass(), v.getName());
                return;
            }
            Class<?> targetClass = ProxyUtil.noneProxyClass(target);
            CustomRole taskComponent = AnnotationUtils.findAnnotation(targetClass, CustomRole.class);
            AssertUtil.notNull(taskComponent);
            AssertUtil.notBlank(taskComponent.name(), ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY, "CustomRole name cannot be empty!");
            doInit(target, targetClass, taskComponent.name());
        });
        super.init();
    }
}
