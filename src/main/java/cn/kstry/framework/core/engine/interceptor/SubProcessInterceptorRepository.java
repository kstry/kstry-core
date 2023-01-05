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
package cn.kstry.framework.core.engine.interceptor;

import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.OrderComparator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 子流程拦截器资源库
 *
 * @author lykan
 */
public class SubProcessInterceptorRepository {

    /**
     * 子流程拦截器集合
     */
    private final List<SubProcessInterceptor> subProcessInterceptorList;

    public SubProcessInterceptorRepository(Collection<SubProcessInterceptor> subProcessInterceptorCollection) {
        List<SubProcessInterceptor> subProcessInterceptors = new ArrayList<>(subProcessInterceptorCollection);
        OrderComparator.sort(subProcessInterceptors);
        this.subProcessInterceptorList = subProcessInterceptors;
    }

    public boolean postBeforeProcessor(StoryBus storyBus, String startEventId, String storyId) {
        AssertUtil.notNull(storyBus);
        List<SubProcessInterceptor> subInterceptorList = getSubProcessInterceptorList(startEventId, storyId);
        return subInterceptorList.stream().allMatch(interceptor -> interceptor.beforeProcessor(storyBus.getScopeDataOperator(), storyBus.getRole()));
    }

    public void postAfterProcessor(StoryBus storyBus, String startEventId, String storyId) {
        AssertUtil.notNull(storyBus);
        List<SubProcessInterceptor> subInterceptorList = getSubProcessInterceptorList(startEventId, storyId);
        subInterceptorList.forEach(interceptor -> interceptor.afterProcessor(storyBus.getScopeDataOperator(), storyBus.getRole()));
    }

    public void postFinallyProcessor(StoryBus storyBus, String startEventId, String storyId) {
        AssertUtil.notNull(storyBus);
        List<SubProcessInterceptor> subInterceptorList = getSubProcessInterceptorList(startEventId, storyId);
        subInterceptorList.forEach(interceptor -> interceptor.finallyProcessor(storyBus.getScopeDataOperator(), storyBus.getRole()));
    }

    public void postErrorProcessor(Throwable exception, StoryBus storyBus, String startEventId, String storyId) {
        AssertUtil.anyNotNull(exception, storyBus);
        List<SubProcessInterceptor> subInterceptorList = getSubProcessInterceptorList(startEventId, storyId);
        subInterceptorList.forEach(interceptor -> interceptor.errorProcessor(exception, storyBus.getScopeDataOperator(), storyBus.getRole()));
    }

    private List<SubProcessInterceptor> getSubProcessInterceptorList(String startEventId, String storyId) {
        if (CollectionUtils.isEmpty(subProcessInterceptorList)) {
            return Lists.newArrayList();
        }
        List<SubProcessInterceptor> priorityMatchList = subProcessInterceptorList.stream().filter(interceptor -> priorityMatch(interceptor, startEventId, storyId)).collect(Collectors.toList());
        List<SubProcessInterceptor> secondMatchList = subProcessInterceptorList.stream().filter(interceptor -> secondMatch(interceptor, startEventId, storyId)).collect(Collectors.toList());
        priorityMatchList.addAll(secondMatchList);
        if (CollectionUtils.isNotEmpty(priorityMatchList)) {
            return priorityMatchList;
        }
        return Lists.newArrayList();
    }

    private boolean secondMatch(SubProcessInterceptor interceptor, String startEventId, String storyId) {
        if (interceptor == null) {
            return false;
        }
        Set<SubProcessIdentity> startEventIdentitySet = interceptor.getSubProcessIdentity();
        if (CollectionUtils.isEmpty(startEventIdentitySet)) {
            return false;
        }
        if (!Objects.equals(startEventId, storyId)) {
            startEventId = getStartEventId(startEventId);
        }
        return startEventIdentitySet.contains(new SubProcessIdentity(startEventId));
    }

    private boolean priorityMatch(SubProcessInterceptor interceptor, String startEventId, String storyId) {
        if (interceptor == null) {
            return false;
        }

        AssertUtil.notBlank(storyId);
        Set<SubProcessIdentity> startEventIdentitySet = interceptor.getSubProcessIdentity();
        if (CollectionUtils.isEmpty(startEventIdentitySet)) {
            return false;
        }
        if (!Objects.equals(startEventId, storyId)) {
            startEventId = getStartEventId(startEventId);
        }
        return startEventIdentitySet.contains(new SubProcessIdentity(startEventId, storyId));
    }

    private String getStartEventId(String startEventId) {
        return startEventId.replaceAll("-\\d+$", StringUtils.EMPTY);
    }
}
