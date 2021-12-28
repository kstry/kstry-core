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

import cn.kstry.framework.core.container.MethodWrapper;
import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.resource.identity.BasicIdentityResource;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.role.TaskComponentPermission;
import cn.kstry.framework.core.task.TaskServiceWrapper;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.TaskServiceUtil;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

/**
 *
 * @author lykan
 */
public class AbilityTaskServiceWrapper extends BasicIdentityResource implements TaskServiceWrapper {

    private final String name;

    protected TaskComponentRegisterProxy target;

    private MethodWrapper methodWrapper;

    public AbilityTaskServiceWrapper(@Nonnull String name) {
        this(name, null, IdentityTypeEnum.SERVICE_TASK);
    }

    public AbilityTaskServiceWrapper(@Nonnull String name, String ability, @Nonnull IdentityTypeEnum identityType) {
        super(TaskServiceUtil.joinName(name, ability), identityType);
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public TaskComponentRegisterProxy getTarget(Role role) {
        if (role != null) {
            AssertUtil.isTrue(match(role));
        }
        return target;
    }

    public void setTarget(TaskComponentRegisterProxy target) {
        AssertUtil.notNull(target);
        this.target = target;
    }

    @Override
    public MethodWrapper getMethodWrapper(Role role) {
        if (role != null) {
            AssertUtil.isTrue(match(role));
        }
        return methodWrapper;
    }

    public void setMethodWrapper(MethodWrapper methodWrapper) {
        AssertUtil.notNull(methodWrapper);
        this.methodWrapper = methodWrapper;
    }

    @Override
    public boolean isValid() {
        return getTarget(null) != null && getMethodWrapper(null) != null;
    }

    @Override
    public boolean match(@Nonnull Role role) {
        if (target != null && target.isCustomRole()) {
            return true;
        }
        TaskComponentPermission permission = new TaskComponentPermission(this.getIdentityId(), this.getIdentityType());
        if (target != null && StringUtils.isNotBlank(target.getName())) {
            permission.setTaskComponentName(target.getName());
        }
        return role.allowPermission(permission);
    }
}
