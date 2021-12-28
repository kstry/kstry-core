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

import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.task.RootTaskServiceAbility;
import cn.kstry.framework.core.task.TaskServiceWrapper;
import cn.kstry.framework.core.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author lykan
 */
public class RootTaskServiceWrapper extends AbilityTaskServiceWrapper implements RootTaskServiceAbility {

    private final Set<TaskServiceWrapper> abilityTaskServiceSet = Sets.newHashSet();

    public RootTaskServiceWrapper(@Nonnull String name) {
        super(name);
    }

    @Override
    public void addTaskAbility(TaskServiceWrapper taskService) {
        AssertUtil.notNull(taskService);
        AssertUtil.equals(taskService.getIdentityType(), IdentityTypeEnum.SERVICE_TASK_ABILITY);
        AssertUtil.isTrue(taskService.getIdentityId().startsWith(getName()));
        abilityTaskServiceSet.add(taskService);
    }

    @Override
    public Optional<TaskServiceWrapper> getTaskAbility(Role role) {
        AssertUtil.notNull(role);
        boolean rootMatch = super.match(role);
        boolean isCustomRole = target != null && target.isCustomRole();
        if (CollectionUtils.isEmpty(abilityTaskServiceSet) || isCustomRole) {
            return Optional.ofNullable(rootMatch ? this : null);
        }
        List<TaskServiceWrapper> abilityList = abilityTaskServiceSet.stream().filter(ability -> ability.match(role)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(abilityList)) {
            return Optional.ofNullable(rootMatch ? this : null);
        }
        if (rootMatch) {
            abilityList.add(0, this);
        }
        AssertUtil.oneSize(abilityList, ExceptionEnum.EXECUTION_ONE_RESULT,
                "There must be one and only one ability matched in the execution! abilityId: {}",
                () -> Lists.newArrayList(JSON.toJSONString(abilityList.stream().map(TaskServiceWrapper::getIdentityId).collect(Collectors.toList()))));
        return Optional.of(abilityList.get(0));
    }
}
