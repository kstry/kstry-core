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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.enums.PermissionType;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 *
 * @author lykan
 */
public class CustomRoleInfo {

    public static Optional<ServiceNodeResource> buildCustomRole(String customRole) {
        if (StringUtils.isBlank(customRole)) {
            return Optional.empty();
        }
        String prefix = PermissionType.COMPONENT_SERVICE.getPrefix() + ":";
        if (!customRole.startsWith(prefix)) {
            customRole = prefix + customRole;
        }
        return PermissionUtil.parseResource(customRole).map(r -> GlobalUtil.transferNotEmpty(r, ServiceNodeResource.class));
    }
}
