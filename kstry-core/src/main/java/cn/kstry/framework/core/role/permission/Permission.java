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
package cn.kstry.framework.core.role.permission;

import cn.kstry.framework.core.enums.PermissionType;
import cn.kstry.framework.core.resource.identity.Identity;

/**
 * 权限
 *
 * @author lykan
 */
public interface Permission extends Identity, PermissionAuth {

    /**
     * 获取权限类型
     *
     * @return 权限类型
     */
    PermissionType getPermissionType();
}
