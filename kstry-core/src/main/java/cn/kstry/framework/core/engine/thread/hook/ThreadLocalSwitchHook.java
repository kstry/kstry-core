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
package cn.kstry.framework.core.engine.thread.hook;

import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.util.AsyncTaskUtil;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;

/**
 * 线程切换时处理ThreadLocal上数据的传递
 */
public class ThreadLocalSwitchHook implements ThreadSwitchHook<Map<ThreadLocal<Object>, Object>> {

    @Override
    public Map<ThreadLocal<Object>, Object> getPreviousData(ScopeDataQuery scopeDataQuery) {
        List<ThreadLocal<Object>> threadLocalSwitchList = AsyncTaskUtil.getThreadLocalSwitchList();
        if (CollectionUtils.isEmpty(threadLocalSwitchList)) {
            return Maps.newHashMap();
        }
        Map<ThreadLocal<Object>, Object> threadLocalMap = Maps.newHashMap();
        threadLocalSwitchList.forEach(t -> threadLocalMap.put(t, t.get()));
        return threadLocalMap;
    }

    @Override
    public void usePreviousData(Map<ThreadLocal<Object>, Object> threadLocalMap, ScopeDataQuery scopeDataQuery) {
        if (MapUtils.isEmpty(threadLocalMap)) {
            return;
        }
        threadLocalMap.forEach(ThreadLocal::set);
    }

    @Override
    public void clear(Map<ThreadLocal<Object>, Object> threadLocalMap, ScopeDataQuery scopeDataQuery) {
        if (MapUtils.isEmpty(threadLocalMap)) {
            return;
        }
        threadLocalMap.keySet().forEach(ThreadLocal::remove);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
