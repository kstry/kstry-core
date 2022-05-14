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
package cn.kstry.framework.core.util;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.enums.PermissionType;
import cn.kstry.framework.core.enums.ServiceNodeType;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.resource.identity.BasicIdentity;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.role.permission.Permission;
import cn.kstry.framework.core.role.permission.SimplePermission;

/**
 * 权限工具类
 *
 * @author lykan
 */
public class PermissionUtil {

    public static List<Permission> permissionList(String source) {
        return permissionList(source, null);
    }

    public static List<Permission> permissionList(String source, String seg) {
        if (StringUtils.isBlank(source)) {
            return Lists.newArrayList();
        }
        if (StringUtils.isBlank(seg)) {
            seg = ",";
        }
        return Stream.of(source.split(seg)).map(String::trim)
                .map(PermissionUtil::parsePermission).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    /**
     * 解析权限
     *
     * @param permission 权限字符串
     * @return 权限
     */
    public static Optional<Permission> parsePermission(String permission) {
        return parseResource(permission).map(Function.identity());
    }

    /**
     * 解析资源
     *
     * @param resource 资源字符串
     * @return 资源
     */
    public static Optional<InServiceNodeResource> parseResource(String resource) {
        if (StringUtils.isBlank(resource)) {
            return Optional.empty();
        }

        String r = PermissionType.SERVICE.getPrefix();
        String pr = PermissionType.COMPONENT_SERVICE.getPrefix();
        String pLowerCase = resource.toLowerCase();
        if (!pLowerCase.startsWith(r + ":") && !pLowerCase.startsWith(pr + ":")) {
            return Optional.empty();
        }

        String[] s1 = resource.split(":");
        if (s1.length != 2) {
            return Optional.empty();
        }
        String[] s2 = s1[1].split("@");
        if (s2.length == 0 || s2.length > 3) {
            return Optional.empty();
        }
        if (s2.length == 1) {
            return Optional.of(new InServiceNodeResource(PermissionType.SERVICE, null, s2[0], null));
        } else if (s2.length == 2) {
            if (Objects.equals(s1[0].toLowerCase(), r)) {
                return Optional.of(new InServiceNodeResource(PermissionType.SERVICE_ABILITY, null, s2[0], s2[1]));
            } else if (Objects.equals(s1[0].toLowerCase(), pr)) {
                return Optional.of(new InServiceNodeResource(PermissionType.COMPONENT_SERVICE, s2[0], s2[1], null));
            } else {
                throw new KstryException(ExceptionEnum.SYSTEM_ERROR);
            }
        } else {
            return Optional.of(new InServiceNodeResource(PermissionType.COMPONENT_SERVICE_ABILITY, s2[0], s2[1], s2[2]));
        }
    }

    public static class InServiceNodeResource extends BasicIdentity implements ServiceNodeResource, Permission {

        /**
         * 权限类型
         */
        private final Permission permission;

        /**
         * 服务组件名
         */
        private final String componentName;

        /**
         * 服务节点名
         */
        private final String serviceName;

        /**
         * 服务能力名
         */
        private final String abilityName;

        public InServiceNodeResource(PermissionType permissionType, String componentName, String serviceName, String abilityName) {
            super(getIdentityId(permissionType, componentName, serviceName, abilityName), IdentityTypeEnum.SERVICE_NODE_RESOURCE);
            this.componentName = componentName;
            this.serviceName = serviceName;
            this.abilityName = abilityName;
            this.permission = new SimplePermission(permissionType, this);
        }

        @Override
        public String getComponentName() {
            return componentName;
        }

        @Override
        public String getServiceName() {
            return serviceName;
        }

        @Override
        public String getAbilityName() {
            return abilityName;
        }

        @Override
        public ServiceNodeType getServiceNodeType() {
            return StringUtils.isBlank(getAbilityName()) ? ServiceNodeType.SERVICE_TASK : ServiceNodeType.SERVICE_TASK_ABILITY;
        }

        @Override
        public PermissionType getPermissionType() {
            return permission.getPermissionType();
        }

        @Override
        public boolean auth(Permission permission) {
            return permission.auth(permission);
        }

        @Override
        public boolean auth(List<Permission> permissionList) {
            return permission.auth(permissionList);
        }

        @Nonnull
        @Override
        public String getIdentityId() {
            return permission.getIdentityId();
        }

        private static String getIdentityId(PermissionType permissionType, String componentName, String serviceName, String abilityName) {
            AssertUtil.notNull(permissionType);
            AssertUtil.notTrue(StringUtils.isAllBlank(componentName, serviceName, abilityName));
            List<String> list = Lists.newArrayList(
                    componentName, serviceName, abilityName).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
            return permissionType.getPrefix() + ":" + String.join("@", list);
        }
    }
}
