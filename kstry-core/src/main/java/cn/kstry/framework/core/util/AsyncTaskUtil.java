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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.engine.thread.ThreadLocalSwitch;
import cn.kstry.framework.core.exception.ExceptionEnum;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;

import java.lang.ref.WeakReference;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * AsyncTaskUtil
 *
 * @author lykan
 */
public class AsyncTaskUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTaskUtil.class);

    public static Runnable proxyRunnable(Runnable task) {
        AssertUtil.notNull(task, ExceptionEnum.NOT_ALLOW_EMPTY);
        Map<ThreadLocal<Object>, Object> threadLocalMap = getThreadLocalMap();
        if (MapUtils.isEmpty(threadLocalMap)) {
            return task;
        }
        return () -> {
            try {
                threadLocalMap.forEach(ThreadLocal::set);
                task.run();
            } finally {
                threadLocalMap.keySet().forEach(ThreadLocal::remove);
            }
        };
    }

    public static <T> Callable<T> proxyCallable(Callable<T> task) {
        AssertUtil.notNull(task, ExceptionEnum.NOT_ALLOW_EMPTY);
        Map<ThreadLocal<Object>, Object> threadLocalMap = getThreadLocalMap();
        if (MapUtils.isEmpty(threadLocalMap)) {
            return task;
        }
        return () -> {
            try {
                threadLocalMap.forEach(ThreadLocal::set);
                return task.call();
            } finally {
                threadLocalMap.keySet().forEach(ThreadLocal::remove);
            }
        };
    }

    public static <T> Supplier<T> proxySupplier(Supplier<T> task) {
        AssertUtil.notNull(task, ExceptionEnum.NOT_ALLOW_EMPTY);
        Map<ThreadLocal<Object>, Object> threadLocalMap = getThreadLocalMap();
        if (MapUtils.isEmpty(threadLocalMap)) {
            return task;
        }
        return () -> {
            try {
                threadLocalMap.forEach(ThreadLocal::set);
                return task.get();
            } finally {
                threadLocalMap.keySet().forEach(ThreadLocal::remove);
            }
        };
    }

    public static <T, R> Function<T, R> proxyFunction(Function<T, R> task) {
        AssertUtil.notNull(task, ExceptionEnum.NOT_ALLOW_EMPTY);
        Map<ThreadLocal<Object>, Object> threadLocalMap = getThreadLocalMap();
        if (MapUtils.isEmpty(threadLocalMap)) {
            return task;
        }
        return param -> {
            try {
                threadLocalMap.forEach(ThreadLocal::set);
                return task.apply(param);
            } finally {
                threadLocalMap.keySet().forEach(ThreadLocal::remove);
            }
        };
    }

    public static ExecutorService proxyExecutor(ExecutorService executorService) {
        AssertUtil.notNull(executorService, ExceptionEnum.NOT_ALLOW_EMPTY);
        return (ExecutorService) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{ExecutorService.class}, (proxy, method, args) -> {
            if (args == null || args.length <= 0) {
                return method.invoke(executorService, args);
            }
            Object arg0 = args[0];
            if (arg0 instanceof Runnable) {
                args[0] = proxyRunnable((Runnable) arg0);
            } else if (arg0 instanceof Callable) {
                args[0] = proxyCallable((Callable<?>) arg0);
            } else if (arg0 instanceof Supplier) {
                args[0] = proxySupplier((Supplier<?>) arg0);
            } else if (arg0 instanceof Function) {
                args[0] = proxyFunction((Function<?, ?>) arg0);
            } else if (arg0 instanceof List && ((List<?>) arg0).stream().allMatch(o -> o instanceof Callable)) {
                args[0] = ((List<?>) arg0).stream().map(o -> proxyCallable((Callable<?>) o)).collect(Collectors.toList());
            }
            return method.invoke(executorService, args);
        });
    }

    @SuppressWarnings("unchecked")
    public static List<ThreadLocal<Object>> getThreadLocalSwitchList() {
        List<ThreadLocal<Object>> threadLocalSwitchList = Lists.newArrayList();
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
                if (!(threadLocal instanceof ThreadLocalSwitch) && !(threadLocal instanceof NamedThreadLocal)) {
                    continue;
                }
                List<String> ignoreCopyPrefix = GlobalProperties.KSTRY_THREAD_IGNORE_COPY_PREFIX;
                if (CollectionUtils.isNotEmpty(ignoreCopyPrefix) && ignoreCopyPrefix.stream().filter(StringUtils::isNotBlank).anyMatch(prefix -> {
                    String name;
                    if (threadLocal instanceof ThreadLocalSwitch) {
                        name = ((ThreadLocalSwitch<?>) threadLocal).getName();
                    } else {
                        name = ((NamedThreadLocal<?>) threadLocal).toString();
                    }
                    return StringUtils.isNotBlank(name) && name.startsWith(prefix);
                })) {
                    continue;
                }
                threadLocalSwitchList.add(threadLocal);
            }
        } catch (Exception e) {
            LOGGER.warn("[{}] An exception occurred while getting ThreadLocal.", ExceptionEnum.STORY_FLOW_ERROR.getExceptionCode(), e);
        }
        return threadLocalSwitchList;
    }

    public static Map<ThreadLocal<Object>, Object> getThreadLocalMap() {
        List<ThreadLocal<Object>> threadLocalSwitchList = getThreadLocalSwitchList();
        if (CollectionUtils.isEmpty(threadLocalSwitchList)) {
            return Maps.newHashMap();
        }
        Map<ThreadLocal<Object>, Object> threadLocalMap = Maps.newHashMap();
        threadLocalSwitchList.forEach(t -> threadLocalMap.put(t, t.get()));
        return threadLocalMap;
    }
}
