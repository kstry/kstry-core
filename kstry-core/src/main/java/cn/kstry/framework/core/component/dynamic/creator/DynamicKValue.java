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
import cn.kstry.framework.core.exception.BusinessException;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.kv.KvScope;
import cn.kstry.framework.core.util.GlobalUtil;

import java.util.Optional;

/**
 * 动态键值对
 */
public interface DynamicKValue extends DynamicComponentCreator<Object>, ComponentCacheSupport {

    /**
     * 获取动态键值对版本，默认 -1，不使用缓存
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
        throw new BusinessException(ExceptionEnum.BUSINESS_INVOKE_ERROR.getExceptionCode(), "Method is not allowed to be called!");
    }

    @Override
    default Optional<Object> getComponent(String key, Object param) {
        return getValue(key, GlobalUtil.transferNotEmpty(param, KvScope.class));
    }

    /**
     * 根据键获取值信息
     *
     * @param key       键
     * @param kvScope   kvScope
     * @return 值
     */
    Optional<Object> getValue(String key, KvScope kvScope);
}
