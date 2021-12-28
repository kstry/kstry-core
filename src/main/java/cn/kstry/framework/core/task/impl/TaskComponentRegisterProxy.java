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
package cn.kstry.framework.core.task.impl;

import cn.kstry.framework.core.annotation.CustomRole;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.role.CustomRoleRegister;
import cn.kstry.framework.core.task.TaskComponentRegister;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import org.springframework.core.annotation.AnnotationUtils;

/**
 *
 * @author lykan
 */
public class TaskComponentRegisterProxy implements TaskComponentRegister {

    private final Object target;

    private final boolean isCustomRole;

    private final String name;

    public TaskComponentRegisterProxy(Object target) {
        AssertUtil.notNull(target);
        this.target = target;
        this.isCustomRole = (target instanceof CustomRoleRegister) || AnnotationUtils.findAnnotation(target.getClass(), CustomRole.class) != null;
        this.name = ElementParserUtil.getTaskComponentName(target).orElseThrow(() -> KstryException.buildException(ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY));
    }

    public Object getTarget() {
        return target;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public boolean isCustomRole() {
        return isCustomRole;
    }
}
