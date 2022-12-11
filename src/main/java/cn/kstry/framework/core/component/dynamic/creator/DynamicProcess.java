/*
 *
 *  * Copyright (c) 2020-2022, Lykan (jiashuomeng@gmail.com).
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
import cn.kstry.framework.core.component.bpmn.link.BpmnLink;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * 动态子流程创建器
 */
public interface DynamicProcess extends DynamicComponentCreator<BpmnLink>, ComponentCacheSupport {

    /**
     * 动态流程版本，默认0 使用缓存。version增加之后，缓存失效重新调用getBpmnLink()生成新的流程
     *
     * @param key key，默认：startEventId
     * @return 版本
     */
    @Override
    default long version(String key) {
        return 0;
    }

    @Override
    default String getKey(ScopeDataQuery scopeDataQuery) {
        return scopeDataQuery.getStartId();
    }

    @Override
    default Optional<BpmnLink> getComponent(String key, Object param) {
        if (StringUtils.isBlank(key)) {
            return Optional.empty();
        }
        return getBpmnLink(key);
    }

    Optional<BpmnLink> getBpmnLink(String startId);
}
