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
package cn.kstry.framework.core.engine.thread.hook;

import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.exception.ExceptionEnum;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 线程切换钩子处理器
 */
public class ThreadSwitchHookProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadSwitchHookProcessor.class);

    /**
     * 线程切换拦截器列表
     */
    private final List<ThreadSwitchHook<Object>> threadSwitchHookList;

    public ThreadSwitchHookProcessor(List<ThreadSwitchHook<Object>> threadSwitchHookList) {
        this.threadSwitchHookList = threadSwitchHookList;
    }

    public Map<ThreadSwitchHook<Object>, Object> getPreviousData(ScopeDataQuery scopeDataQuery) {
        HashMap<ThreadSwitchHook<Object>, Object> resultMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(threadSwitchHookList)) {
            return resultMap;
        }
        threadSwitchHookList.forEach(hook -> {
            Object previousData = null;
            try {
                previousData = hook.getPreviousData(scopeDataQuery);
            } catch (Throwable e) {
                LOGGER.warn("[{}] {}", ExceptionEnum.THREAD_SWITCH_HOOK_ERROR.getExceptionCode(), ExceptionEnum.THREAD_SWITCH_HOOK_ERROR.getDesc(), e);
            }
            resultMap.put(hook, previousData);
        });
        return resultMap;
    }

    public void usePreviousData(Map<ThreadSwitchHook<Object>, Object> dataMap, ScopeDataQuery scopeDataQuery) {
        if (CollectionUtils.isEmpty(threadSwitchHookList) || MapUtils.isEmpty(dataMap)) {
            return;
        }
        threadSwitchHookList.forEach(hook -> {
            try {
                hook.usePreviousData(dataMap.get(hook), scopeDataQuery);
            } catch (Throwable e) {
                LOGGER.warn("[{}] {}", ExceptionEnum.THREAD_SWITCH_HOOK_ERROR.getExceptionCode(), ExceptionEnum.THREAD_SWITCH_HOOK_ERROR.getDesc(), e);
            }
        });
    }
}
