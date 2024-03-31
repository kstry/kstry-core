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
package cn.kstry.framework.core.component.dynamic.creator;

import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.TaskServiceUtil;

import java.util.Optional;

/**
 * 动态角色创建器
 */
public interface DynamicRole extends DynamicComponentCreator<Role>, ComponentCacheSupport {

    /**
     * 获取动态组件版本，默认 -1，不是用缓存
     *
     * @param key key
     * @return version
     */
    @Override
    default long version(String key) {
        return -1;
    }

    /**
     * 获取Key
     *
     * @param scopeDataQuery scopeDataQuery
     * @return 组件
     */
    @Override
    default String getKey(ScopeDataQuery scopeDataQuery) {
        return TaskServiceUtil.joinName(scopeDataQuery.getStartId(), scopeDataQuery.getBusinessId().orElse(null));
    }

    @Override
    default Optional<Role> getComponent(String key, Object param) {
        return getRole(key);
    }

    /**
     * 获取角色信息
     *
     * @param key   默认：startEventId@businessId
     * @return 角色
     */
    Optional<Role> getRole(String key);
}
