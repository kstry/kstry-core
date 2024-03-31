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
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.exception.BusinessException;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 动态子流程创建器
 */
public interface DynamicSubProcess extends DynamicComponentCreator<SubProcessLink> {

    @Override
    default String getKey(ScopeDataQuery scopeDataQuery) {
        throw new BusinessException(ExceptionEnum.BUSINESS_INVOKE_ERROR.getExceptionCode(), "Method is not allowed to be called!");
    }

    @Override
    default Optional<SubProcessLink> getComponent(String key, Object param) {
        if (StringUtils.isBlank(key)) {
            return Optional.empty();
        }

        Optional<SubProcessLink> subProcessLinkOptional = getSubProcessLink(key);
        if (subProcessLinkOptional.isPresent()) {
            return subProcessLinkOptional;
        }
        List<SubProcessLink> subProcessLinks = getSubProcessLinks();
        if (CollectionUtils.isEmpty(subProcessLinks)) {
            return Optional.empty();
        }

        List<SubProcessLink> links = subProcessLinks.stream().filter(link -> Objects.equals(link.getSubProcessId(), key)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(links)) {
            return Optional.empty();
        }
        AssertUtil.oneSize(links, ExceptionEnum.COMPONENT_DUPLICATION_ERROR, "Duplicate subProcessLink when getting dynamic subProcessLinks. key: {}", key);
        return Optional.of(links.get(0));
    }

    default Optional<SubProcessLink> getSubProcessLink(String key) {
        return Optional.empty();
    }

    default List<SubProcessLink> getSubProcessLinks() {
        return Lists.newArrayList();
    }
}
