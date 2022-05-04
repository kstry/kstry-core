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
import cn.kstry.framework.core.enums.ServiceNodeType;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.resource.identity.Identity;
import cn.kstry.framework.core.role.permission.Permission;
import cn.kstry.framework.core.role.permission.PermissionAuth;
import com.google.common.collect.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 角色基类
 *
 * @author lykan
 */
public class BasicRole implements Role {

    /**
     * 角色名称
     */
    private final String name;

    /**
     * 父级 角色集合
     */
    private final Set<Role> parentRoles = Sets.newHashSet();

    /**
     * 权限桶
     */
    private final Map<ServiceNodeType, List<Permission>> permissionBucket = Maps.newHashMap();

    /**
     * 读写锁
     */
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public BasicRole() {
        this(null);
    }

    public BasicRole(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name == null ? StringUtils.EMPTY : name;
    }

    @Override
    public boolean allowedUseResource(PermissionAuth permissionAuth) {
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            InStack<Role> roleStack = new BasicInStack<>();
            roleStack.push(this);
            while (!roleStack.isEmpty()) {
                Role role = roleStack.pop().orElseThrow(() -> KstryException.buildException(null, ExceptionEnum.SYSTEM_ERROR, null));
                if (permissionAuth.auth(role.getPermission().get(permissionAuth.getServiceNodeType()))) {
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
            return ImmutableSet.copyOf(this.parentRoles);
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
            permissionList.forEach(permission -> {
                List<Permission> list = permissionBucket.computeIfAbsent(permission.getPermissionType().getServiceNodeType(), k -> Lists.newArrayList());
                if (list.stream().map(Identity::getIdentityId).noneMatch(id -> Objects.equals(id, permission.getIdentityId()))) {
                    list.add(permission);
                }
            });
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Map<ServiceNodeType, List<Permission>> getPermission() {
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            return ImmutableMap.copyOf(permissionBucket);
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
            Role role = roleStack.pop().orElseThrow(() -> KstryException.buildException(null, ExceptionEnum.SYSTEM_ERROR, null));
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
}
