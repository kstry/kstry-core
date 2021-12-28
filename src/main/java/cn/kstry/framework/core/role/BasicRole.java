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

import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.resource.identity.Identity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

/**
 *
 * @author lykan
 */
public class BasicRole implements Role {

    /**
     * 角色名称
     */
    private String name;

    /**
     * 父级 角色集合
     */
    private final Set<Role> parentRoles = Sets.newHashSet();

    /**
     * 权限集合
     */
    private final Map<IdentityTypeEnum, List<Permission>> permissionMap = Maps.newHashMap();

    /**
     * 读写锁
     */
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public BasicRole() {

    }

    public BasicRole(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name == null ? StringUtils.EMPTY : name;
    }

    @Override
    public boolean allowPermission(@Nonnull Permission permission) {
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            InStack<Role> roleStack = new BasicInStack<>();
            roleStack.push(this);
            while (!roleStack.isEmpty()) {
                Role role = roleStack.pop().orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
                List<Permission> pList = role.getPermission().get(permission.getIdentityType());
                if (CollectionUtils.isNotEmpty(pList) && pList.stream().filter(filterPermissionPredicate(permission))
                        .map(Permission::getIdentityId).anyMatch(id -> Objects.equals(id, permission.getIdentityId()))) {
                    return true;
                }
                if (CollectionUtils.isEmpty(role.getParentRole())) {
                    continue;
                }
                roleStack.pushCollection(role.getParentRole());
            }
            return false;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void addParentRole(Set<Role> roleSet) {
        if (CollectionUtils.isEmpty(roleSet)) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            if (notExistCircularDependency(roleSet, this)) {
                this.parentRoles.addAll(roleSet);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Set<Role> getParentRole() {
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            return Sets.newHashSet(this.parentRoles);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void addPermission(List<Permission> permissionList) {
        if (CollectionUtils.isEmpty(permissionList)) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            permissionList.forEach(resource -> {
                List<Permission> resourceIdentities = permissionMap.computeIfAbsent(resource.getIdentityType(), k -> Lists.newArrayList());
                if (resourceIdentities.stream().map(Identity::getIdentityId).noneMatch(id -> Objects.equals(id, resource.getIdentityId()))) {
                    resourceIdentities.add(resource);
                }
            });
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Map<IdentityTypeEnum, List<Permission>> getPermission() {
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            return Maps.newHashMap(this.permissionMap);
        } finally {
            readLock.unlock();
        }
    }

    private boolean notExistCircularDependency(Set<Role> roleSet, Role checkRole) {
        if (CollectionUtils.isEmpty(roleSet)) {
            return true;
        }
        InStack<Role> roleStack = new BasicInStack<>();
        roleStack.pushCollection(roleSet);
        while (!roleStack.isEmpty()) {
            Role role = roleStack.pop().orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
            if (Objects.equals(role, checkRole)) {
                return false;
            }
            if (CollectionUtils.isEmpty(role.getParentRole())) {
                continue;
            }
            roleStack.pushCollection(role.getParentRole());
        }
        return true;
    }

    private Predicate<Permission> filterPermissionPredicate(Permission permission) {
        return p -> {
            if (!(p instanceof TaskComponentPermission)) {
                return true;
            }
            if (!(permission instanceof TaskComponentPermission)) {
                return false;
            }
            return Objects.equals(((TaskComponentPermission) p).getTaskComponentName(), ((TaskComponentPermission) permission).getTaskComponentName());
        };
    }
}
