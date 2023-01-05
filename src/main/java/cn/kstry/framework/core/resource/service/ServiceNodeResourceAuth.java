/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.resource.service;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;

import cn.kstry.framework.core.role.permission.Permission;
import cn.kstry.framework.core.role.permission.PermissionAuth;

/**
 * 服务节点资源，支持鉴权
 *
 * @author lykan
 */
public interface ServiceNodeResourceAuth extends ServiceNodeResource, PermissionAuth {

    default boolean auth(Permission permission) {
        if (permission == null) {
            return false;
        }
        if (permission.getPermissionType().getServiceNodeType() != getServiceNodeType()) {
            return false;
        }
        return Objects.equals(permission.getIdentityId(), permission.getPermissionType().getPermissionId(this).orElse(null));
    }

    default boolean auth(List<Permission> permissionList) {
        if (CollectionUtils.isEmpty(permissionList)) {
            return false;
        }
        return permissionList.stream().anyMatch(this::auth);
    }
}
