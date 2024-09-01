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
package cn.kstry.framework.test.role;

import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.component.dynamic.creator.DynamicRole;
import cn.kstry.framework.core.role.*;
import cn.kstry.framework.core.util.KeyUtil;
import cn.kstry.framework.core.util.PermissionUtil;
import cn.kstry.framework.core.util.TaskServiceUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author lykan
 */
@Component
public class RoleRegister implements BusinessRoleRegister, DynamicRole {

    @Override
    public List<BusinessRole> register() {
        return Lists.newArrayList(getR1(), getR3());
    }

    private BusinessRole getR1() {
        Role role = new BasicRole();
        role.addPermission(PermissionUtil.permissionList("r:say_number"));

        BasicRole parentRole = new BasicRole();
        parentRole.addPermission(PermissionUtil.permissionList("r:say_number@say_number_increase, r:say_number@say_number_square"));
        role.addParentRole(Sets.newHashSet(parentRole));
        return new BusinessRole("story-def-test-role-001", role);
    }

    private BusinessRole getR3() {
        Role role = new BasicRole();
        role.addPermission(PermissionUtil.permissionList("pr:say_info2@say_number, pr:say_info@say_number"));
        return new BusinessRole("story-def-test-role-00401", role);
    }

    private final Role role4;

    {
        role4 = new ServiceTaskRole();
        role4.addPermission(PermissionUtil.permissionList(KeyUtil.pr("hello_service", "say_hello1"), null));
    }

    @Override
    public Optional<Role> getRole(String key) {
        if (Objects.equals(TaskServiceUtil.joinName("story-def-test-role-004", "aq-110"), key)) {
            return Optional.of(role4);
        }
        return Optional.empty();
    }

    @Override
    public String getKey(ScopeDataQuery scopeDataQuery) {
        Assert.assertEquals(scopeDataQuery.getRawData("req.number").orElse(null), 8);
        return DynamicRole.super.getKey(scopeDataQuery);
    }

    @Override
    public long version(String key) {
        return -1;
    }
}
