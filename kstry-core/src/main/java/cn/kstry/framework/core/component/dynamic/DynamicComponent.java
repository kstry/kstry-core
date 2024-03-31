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
package cn.kstry.framework.core.component.dynamic;

import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.enums.DynamicComponentType;

import java.util.Optional;

/**
 * 动态组件
 *
 * @param <Component>
 */
public interface DynamicComponent<Component> {

    /**
     * 获取动态组件的类型
     *
     * @return 动态组件的类型
     */
    DynamicComponentType getComponentType();

    /**
     * 动态获取组件
     *
     * @param defKey 默认Key，非空时，DynamicComponentCreator.getKey()将不会被调用
     * @param param 获取组件时候需要的参数
     * @param scopeDataQuery scopeDataQuery
     * @return 组件
     */
    Optional<Component> dynamicGetComponent(String defKey, Object param, ScopeDataQuery scopeDataQuery);
}
