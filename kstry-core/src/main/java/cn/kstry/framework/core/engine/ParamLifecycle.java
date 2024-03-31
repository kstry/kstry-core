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
package cn.kstry.framework.core.engine;

import cn.kstry.framework.core.bus.ScopeDataOperator;

/**
 * 参数生命周期接口
 *
 * @author lykan
 */
public interface ParamLifecycle {

    /**
     * 在参数未被初始化前调用
     *
     * @param scopeDataOperator 数据操作入口
     */
    default void before(ScopeDataOperator scopeDataOperator) {
        // do nothing
    }

    /**
     * 在参数未被初始化前调用
     *
     * @param scopeDataOperator 数据操作入口
     */
    default void after(ScopeDataOperator scopeDataOperator) {
        // do nothing
    }
}
