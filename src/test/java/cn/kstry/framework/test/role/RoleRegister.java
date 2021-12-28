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
package cn.kstry.framework.test.role;

import cn.kstry.framework.core.role.BasicRole;
import cn.kstry.framework.core.role.BusinessRole;
import cn.kstry.framework.core.role.BusinessRoleRegister;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.PermissionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * @author lykan
 */
@Component
public class RoleRegister implements BusinessRoleRegister {

    @Override
    public List<BusinessRole> register() {
        return Lists.newArrayList(getR1(), getR2(), getR3());
    }

    private BusinessRole getR1() {
        Role role = new BasicRole();
        role.addPermission(PermissionUtil.permissionList("r:say_number"));
        BasicRole parentRole = new BasicRole();

        parentRole.addPermission(PermissionUtil.permissionList("r:say_number@say_number_increase, r:say_number@say_number_square"));
        role.addParentRole(Sets.newHashSet(parentRole));
        return new BusinessRole("story-def-test-role-001", role);
    }

    private BusinessRole getR2() {
        Role role = new BasicRole();
        role.addPermission(PermissionUtil.permissionList("pr:hello_service@say_hello1", null));
        return new BusinessRole("aq-110", "story-def-test-role-004", role);
    }

    private BusinessRole getR3() {
        Role role = new BasicRole();
        role.addPermission(PermissionUtil.permissionList("r:say_hello, r:say_hello2", null));
        return new BusinessRole("story-def-test-role-00401", role);
    }
}
