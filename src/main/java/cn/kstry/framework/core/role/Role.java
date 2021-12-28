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
package cn.kstry.framework.core.role;

import cn.kstry.framework.core.enums.IdentityTypeEnum;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author lykan
 */
public interface Role {

    String getName();

    /**
     * 判断当前角色是否拥有该权限
     *
     * @param permission 权限
     * @return 允许：true
     */
    boolean allowPermission(@Nonnull Permission permission);

    /**
     * parent role 中不能出现自身的引用
     *
     * @param roleSet 父级角色
     */
    void addParentRole(Set<Role> roleSet);

    /**
     * 获取 parent role
     *
     * @return parent role（副本）
     */
    Set<Role> getParentRole();

    /**
     * 添加权限
     *
     * @param permissionList 资源列表
     */
    void addPermission(List<Permission> permissionList);

    /**
     * 获取 角色具备的权限
     *
     * @return 权限（副本）
     */
    Map<IdentityTypeEnum, List<Permission>> getPermission();
}
