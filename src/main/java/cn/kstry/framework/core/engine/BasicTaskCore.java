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
package cn.kstry.framework.core.engine;

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.container.component.MethodWrapper;
import cn.kstry.framework.core.container.component.ParamInjectDef;
import cn.kstry.framework.core.container.component.TaskServiceDef;
import cn.kstry.framework.core.container.task.impl.TaskComponentProxy;
import cn.kstry.framework.core.engine.thread.InvokeMethodThreadLocal;
import cn.kstry.framework.core.engine.thread.Task;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.*;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 方法调用核心
 *
 * @author lykan
 */
public abstract class BasicTaskCore<T> implements Task<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicTaskCore.class);

    /**
     * 目标方法执行失败的错误标志
     */
    private static final Object INVOKE_ERROR_SIGN = new Object();

    /**
     * StoryEngine 组成模块
     */
    protected final StoryEngineModule engineModule;

    /**
     * 流程寄存器
     */
    protected final FlowRegister flowRegister;

    /**
     * StoryBus
     */
    protected final StoryBus storyBus;

    /**
     * 角色
     */
    protected final Role role;

    /**
     * 削减锁，控制任务提交后不能立刻执行
     */
    protected final CountDownLatch asyncTaskSwitch = new CountDownLatch(1);

    /**
     * 任务名称
     */
    private final String taskName;

    public BasicTaskCore(StoryEngineModule engineModule, FlowRegister flowRegister, StoryBus storyBus, Role role, String taskName) {
        AssertUtil.notBlank(taskName);
        AssertUtil.anyNotNull(engineModule, flowRegister, storyBus, role);
        this.engineModule = engineModule;
        this.flowRegister = flowRegister;
        this.storyBus = storyBus;
        this.role = role;
        this.taskName = taskName;
    }

    @Override
    public void openSwitch() {
        asyncTaskSwitch.countDown();
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    public FlowRegister getFlowRegister() {
        return flowRegister;
    }

    protected Object doInvokeMethod(ServiceTask serviceTask, TaskServiceDef taskServiceDef, StoryBus storyBus, Role role) {
        MethodWrapper methodWrapper = taskServiceDef.getMethodWrapper();
        TaskComponentProxy targetProxy = taskServiceDef.getTaskComponentTarget();
        List<ParamInjectDef> paramInjectDefs = methodWrapper.getParamInjectDefs();

        if (!serviceTask.iterable()) {
            return doInvokeMethod(true, null, serviceTask, storyBus, role, methodWrapper, targetProxy, paramInjectDefs);
        }

        MonitorTracking monitorTracking = storyBus.getMonitorTracking();
        Optional<Object> iteData = storyBus.getScopeDataOperator().getData(serviceTask.getIteSource()).map(d -> {
            if (!d.getClass().isArray()) {
                return d;
            }
            int arrLength = Array.getLength(d);
            if (arrLength == 0) {
                return null;
            }
            Object[] dArray = new Object[arrLength];
            for (int i = 0; i < arrLength; i++) {
                dArray[i] = Array.get(d, i);
            }
            return Stream.of(dArray).filter(Objects::nonNull).collect(Collectors.toList());
        }).filter(d -> d instanceof Iterable);
        if (!iteData.isPresent()) {
            monitorTracking.iterateCountTracking(serviceTask, 0);
            LOGGER.info("[{}] {} identity: {}, source: {}", ExceptionEnum.ITERATE_ITEM_ERROR.getExceptionCode(),
                    "Get the target collection is empty, the component will not perform traversal execution!", serviceTask.identity(), serviceTask.getIteSource());
            return null;
        }
        Iterator<?> iterator = GlobalUtil.transferNotEmpty(iteData.get(), Iterable.class).iterator();
        if (!iterator.hasNext()) {
            monitorTracking.iterateCountTracking(serviceTask, 0);
            LOGGER.info("[{}] {} identity: {}, source: {}", ExceptionEnum.ITERATE_ITEM_ERROR.getExceptionCode(),
                    "Get the target collection is empty, the component will not perform traversal execution!", serviceTask.identity(), serviceTask.getIteSource());
            return null;
        }

        if (BooleanUtils.isNotTrue(serviceTask.openAsync()) || serviceTask.getIteStrategy() == IterateStrategyEnum.ANY_SUCCESS) {
            int count = 1;
            Object result = null;
            for (int i = 0; iterator.hasNext(); i++, count++) {
                Object r = doInvokeMethod(i == 0, iterator.next(), serviceTask, storyBus, role, methodWrapper, targetProxy, paramInjectDefs);
                if (r == INVOKE_ERROR_SIGN) {
                    continue;
                }
                if (serviceTask.getIteStrategy() == IterateStrategyEnum.ANY_SUCCESS) {
                    monitorTracking.iterateCountTracking(serviceTask, i + 1);
                    return r;
                }
                result = r;
            }
            monitorTracking.iterateCountTracking(serviceTask, count);
            return result;
        }
        List<CompletableFuture<Object>> futureList = Lists.newArrayList();
        iterator.forEachRemaining(next -> {
            CompletableFuture<Object> f = CompletableFuture.supplyAsync(
                    () -> doInvokeMethod(futureList.isEmpty(), next, serviceTask, storyBus, role, methodWrapper, targetProxy, paramInjectDefs),
                    engineModule.getIteratorThreadPool());
            futureList.add(f);
        });
        monitorTracking.iterateCountTracking(serviceTask, futureList.size());
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        for (CompletableFuture<Object> f : futureList) {
            try {
                if (f.get() != INVOKE_ERROR_SIGN) {
                    return f.get();
                }
            } catch (Throwable e) {
                throw ExceptionUtil.buildException(e, ExceptionEnum.ITERATE_ITEM_ERROR, null);
            }
        }
        return null;
    }

    private Object doInvokeMethod(boolean tracking, Object itemData, ServiceTask serviceTask, StoryBus storyBus,
                                  Role role, MethodWrapper methodWrapper, TaskComponentProxy targetProxy, List<ParamInjectDef> paramInjectDefs) {
        try {
            InvokeMethodThreadLocal.setDataItem(itemData);
            InvokeMethodThreadLocal.setTaskProperty(serviceTask.getTaskProperty());
            if (CollectionUtils.isEmpty(paramInjectDefs)) {
                return ProxyUtil.invokeMethod(storyBus, methodWrapper, serviceTask, targetProxy.getTarget());
            }
            Function<ParamInjectDef, Object> paramInitStrategy = engineModule.getParamInitStrategy();
            return ProxyUtil.invokeMethod(storyBus, methodWrapper, serviceTask, targetProxy.getTarget(),
                    () -> TaskServiceUtil.getTaskParams(tracking, serviceTask, storyBus, role, targetProxy, paramInjectDefs, paramInitStrategy));
        } catch (Throwable e) {
            if (!serviceTask.iterable() || serviceTask.getIteStrategy() == null || serviceTask.getIteStrategy() == IterateStrategyEnum.ALL_SUCCESS) {
                throw e;
            }
            LOGGER.warn("[{}] {} identity: {}", ExceptionEnum.ITERATE_ITEM_ERROR.getExceptionCode(), ExceptionEnum.ITERATE_ITEM_ERROR.getDesc(), serviceTask.identity(), e);
            return INVOKE_ERROR_SIGN;
        } finally {
            InvokeMethodThreadLocal.clear();
        }
    }
}
