/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
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
import cn.kstry.framework.core.engine.thread.ThreadLocalSwitch;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.ProxyUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * 线程切换时处理ThreadLocal上数据的传递
 */
public class ThreadLocalSwitchHook implements ThreadSwitchHook<Map<ThreadLocalSwitch<Object>, Object>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLocalSwitchHook.class);

    @Override
    public Map<ThreadLocalSwitch<Object>, Object> getPreviousData(ScopeDataQuery scopeDataQuery) {
        List<ThreadLocalSwitch<Object>> threadLocalSwitchList = getThreadLocalSwitchList();
        if (CollectionUtils.isEmpty(threadLocalSwitchList)) {
            return Maps.newHashMap();
        }
        Map<ThreadLocalSwitch<Object>, Object> threadLocalMap = Maps.newHashMap();
        threadLocalSwitchList.forEach(t -> threadLocalMap.put(t, t.get()));
        return threadLocalMap;
    }

    @Override
    public void usePreviousData(Map<ThreadLocalSwitch<Object>, Object> threadLocalMap, ScopeDataQuery scopeDataQuery) {
        if (MapUtils.isEmpty(threadLocalMap)) {
            return;
        }
        threadLocalMap.forEach(ThreadLocalSwitch::set);
    }

    @Override
    public void clear(Map<ThreadLocalSwitch<Object>, Object> threadLocalMap, ScopeDataQuery scopeDataQuery) {
        if (MapUtils.isEmpty(threadLocalMap)) {
            return;
        }
        threadLocalMap.keySet().forEach(ThreadLocalSwitch::remove);
    }

    @SuppressWarnings("unchecked")
    private List<ThreadLocalSwitch<Object>> getThreadLocalSwitchList() {
        List<ThreadLocalSwitch<Object>> threadLocalSwitchList = Lists.newArrayList();
        try {
            Object threadLocals = ProxyUtil.getFieldValue(Thread.currentThread(), "threadLocals").orElse(null);
            if (threadLocals == null) {
                return threadLocalSwitchList;
            }
            Object[] table = (Object[]) ProxyUtil.getFieldValue(threadLocals, "table").orElse(new Object[0]);
            if (ArrayUtils.isEmpty(table)) {
                return threadLocalSwitchList;
            }
            for (Object entry : table) {
                if (entry == null) {
                    continue;
                }
                ThreadLocal<Object> threadLocal = ((WeakReference<ThreadLocal<Object>>) entry).get();
                if (!(threadLocal instanceof ThreadLocalSwitch)) {
                    continue;
                }
                threadLocalSwitchList.add((ThreadLocalSwitch<Object>) threadLocal);
            }
        } catch (Exception e) {
            LOGGER.warn("[{}] An exception occurred while getting ThreadLocal.", ExceptionEnum.STORY_FLOW_ERROR.getExceptionCode(), e);
        }
        return threadLocalSwitchList;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
