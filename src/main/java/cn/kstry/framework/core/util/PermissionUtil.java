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

import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.role.Permission;
import cn.kstry.framework.core.role.SimplePermission;
import cn.kstry.framework.core.role.TaskComponentPermission;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author lykan
 */
public class PermissionUtil {

    private static final String R = "r";

    private static final String PR = "pr";

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
        if (StringUtils.isBlank(permission)) {
            return Optional.empty();
        }
        String pLowerCase = permission.toLowerCase();
        if (!pLowerCase.startsWith(R + ":") && !pLowerCase.startsWith(PR + ":")) {
            return Optional.empty();
        }
        String[] s1 = permission.split(":");
        if (s1.length != 2) {
            return Optional.empty();
        }
        String[] s2 = s1[1].split("@");
        if (s2.length == 0 || s2.length > 3) {
            return Optional.empty();
        }
        if (s2.length == 1) {
            return Optional.of(new SimplePermission(s2[0], IdentityTypeEnum.SERVICE_TASK));
        } else if (s2.length == 2) {
            if (Objects.equals(s1[0].toLowerCase(), R)) {
                return Optional.of(new SimplePermission(TaskServiceUtil.joinName(s2[0], s2[1]), IdentityTypeEnum.SERVICE_TASK_ABILITY));
            } else if (Objects.equals(s1[0].toLowerCase(), PR)) {
                return Optional.of(new TaskComponentPermission(s2[0], s2[1], IdentityTypeEnum.SERVICE_TASK));
            } else {
                throw new KstryException(ExceptionEnum.SYSTEM_ERROR);
            }
        } else {
            return Optional.of(new TaskComponentPermission(s2[0], TaskServiceUtil.joinName(s2[1], s2[2]), IdentityTypeEnum.SERVICE_TASK_ABILITY));
        }
    }
}
