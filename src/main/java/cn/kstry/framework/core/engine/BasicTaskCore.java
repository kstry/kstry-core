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
package cn.kstry.framework.core.engine;

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.bpmn.extend.ElementIterable;
import cn.kstry.framework.core.bpmn.impl.BasicElementIterable;
import cn.kstry.framework.core.bus.IterDataItem;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.component.validator.RequestValidator;
import cn.kstry.framework.core.container.component.MethodWrapper;
import cn.kstry.framework.core.container.component.ParamInjectDef;
import cn.kstry.framework.core.container.component.TaskInstructWrapper;
import cn.kstry.framework.core.container.component.TaskServiceDef;
import cn.kstry.framework.core.container.task.impl.TaskComponentProxy;
import cn.kstry.framework.core.engine.thread.InvokeMethodThreadLocal;
import cn.kstry.framework.core.engine.thread.Task;
import cn.kstry.framework.core.engine.thread.hook.ThreadSwitchHook;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.kv.KvScope;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    protected final Map<ThreadSwitchHook<Object>, Object> threadSwitchHookObjectMap;

    public BasicTaskCore(StoryEngineModule engineModule, FlowRegister flowRegister, StoryBus storyBus, Role role, String taskName) {
        AssertUtil.notBlank(taskName);
        AssertUtil.anyNotNull(engineModule, flowRegister, storyBus, role);
        this.engineModule = engineModule;
        this.flowRegister = flowRegister;
        this.storyBus = storyBus;
        this.role = role;
        this.taskName = taskName;
        this.threadSwitchHookObjectMap = engineModule.getThreadSwitchHookProcessor().getPreviousData(storyBus.getScopeDataOperator());
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

    /**
     * 支持批量调用
     */
    protected Object doInvokeMethod(ServiceTask serviceTask, TaskServiceDef taskServiceDef, StoryBus storyBus, Role role) {
        MethodWrapper methodWrapper = taskServiceDef.getMethodWrapper();
        TaskComponentProxy targetProxy = taskServiceDef.getTaskComponentTarget();
        List<ParamInjectDef> paramInjectDefs = methodWrapper.getParamInjectDefs();
        ElementIterable elementIterable = getElementIterable(serviceTask, methodWrapper.getElementIterable());

        // 降级方法调用时不支持迭代
        if (!elementIterable.iterable() || taskServiceDef.isDemotionNode()) {
            return doInvokeMethod(true, null, null, serviceTask, storyBus, role, methodWrapper, targetProxy, paramInjectDefs);
        }

        MonitorTracking monitorTracking = storyBus.getMonitorTracking();
        Optional<Object> iteData = storyBus.getScopeDataOperator().getData(elementIterable.getIteSource()).map(d -> {
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
            return Stream.of(dArray).collect(Collectors.toList());
        }).filter(d -> d instanceof Iterable);
        if (!iteData.isPresent()) {
            monitorTracking.iterateCountTracking(serviceTask, 0, 0);
            LOGGER.info("[{}] {} identity: {}, source: {}", ExceptionEnum.ITERATE_ITEM_ERROR.getExceptionCode(),
                    "Get the target collection is empty, the component will not perform traversal execution!", serviceTask.identity(), elementIterable.getIteSource());
            return null;
        }
        Iterator<?> iterator = GlobalUtil.transferNotEmpty(iteData.get(), Iterable.class).iterator();
        if (!iterator.hasNext()) {
            monitorTracking.iterateCountTracking(serviceTask, 0, 0);
            LOGGER.info("[{}] {} identity: {}, source: {}", ExceptionEnum.ITERATE_ITEM_ERROR.getExceptionCode(),
                    "Get the target collection is empty, the component will not perform traversal execution!", serviceTask.identity(), elementIterable.getIteSource());
            return null;
        }

        List<Object> resultList = Lists.newArrayList();
        List<ImmutablePair<Mono<?>, Integer>> monoResultList = Lists.newArrayList();
        int stride = Optional.ofNullable(elementIterable.getStride()).filter(i -> i > 0).orElse(1);
        boolean isOneStride = stride == 1;
        if (BooleanUtils.isNotTrue(elementIterable.openAsync()) || elementIterable.getIteStrategy() == IterateStrategyEnum.ANY_SUCCESS || methodWrapper.isMonoResult()) {
            int count = 0;
            List<Object> batchParamList = isOneStride ? null : Lists.newArrayList();
            for (int i = 0; iterator.hasNext(); i++) {
                Object next = iterator.next();
                if (!isOneStride) {
                    batchParamList.add(next);
                    next = batchParamList;
                }
                if (!isOneStride && batchParamList.size() < stride && iterator.hasNext()) {
                    continue;
                }
                int batchParamSize = Optional.ofNullable(batchParamList).map(List::size).orElse(0);
                IterDataItem<Object> iterDataItem = new IterDataItem<>(!isOneStride, isOneStride ? next : null, isOneStride ? Lists.newArrayList() : batchParamList, count++);
                Object r = doInvokeMethod(i == 0, elementIterable, iterDataItem, serviceTask, storyBus, role, methodWrapper, targetProxy, paramInjectDefs);
                if (r == INVOKE_ERROR_SIGN) {
                    if (BooleanUtils.isNotTrue(elementIterable.getIteAlignIndex())) {
                        continue;
                    }
                    if (isOneStride) {
                        resultList.add(null);
                    } else {
                        resultList.addAll(IntStream.range(0, batchParamSize).mapToObj(it -> null).collect(Collectors.toList()));
                    }
                    continue;
                }
                if (elementIterable.getIteStrategy() == IterateStrategyEnum.ANY_SUCCESS) {
                    monitorTracking.iterateCountTracking(serviceTask, count, stride);
                    if (r == null && BooleanUtils.isNotTrue(elementIterable.getIteAlignIndex())) {
                        return resultList;
                    }
                    if (r instanceof Mono) {
                        r = ((Mono<?>) r).block(Duration.ofMillis(storyBus.remainTimeMillis()));
                    }
                    addSuccessResult(serviceTask, isOneStride, resultList, elementIterable, r, batchParamSize);
                    return resultList;
                }
                if (methodWrapper.isMonoResult()) {
                    monoResultList.add(ImmutablePair.of((Mono<?>) r, batchParamSize));
                } else {
                    addSuccessResult(serviceTask, isOneStride, resultList, elementIterable, r, batchParamSize);
                }
                if (!isOneStride && iterator.hasNext()) {
                    batchParamList = Lists.newArrayList();
                }
            }
            if (methodWrapper.isMonoResult()) {
                monoResultList.forEach(pair -> {
                    Object res;
                    try {
                        res = pair.getLeft().block(Duration.ofMillis(storyBus.remainTimeMillis()));
                    } catch (Throwable e) {
                        if (elementIterable.getIteStrategy() == IterateStrategyEnum.ALL_SUCCESS) {
                            throw ExceptionUtil.buildException(e, ExceptionEnum.ITERATE_ITEM_ERROR, null);
                        }
                        if (BooleanUtils.isNotTrue(elementIterable.getIteAlignIndex())) {
                            return;
                        }
                        if (isOneStride) {
                            resultList.add(null);
                        } else {
                            resultList.addAll(IntStream.range(0, pair.getRight()).mapToObj(it -> null).collect(Collectors.toList()));
                        }
                        return;
                    }
                    addSuccessResult(serviceTask, isOneStride, resultList, elementIterable, res, pair.getRight());
                });
            }
            monitorTracking.iterateCountTracking(serviceTask, count, stride);
            return resultList;
        }
        return asyncIterate(elementIterable, serviceTask, storyBus, role, methodWrapper, targetProxy, monitorTracking, iterator, stride, isOneStride);
    }

    private Object asyncIterate(ElementIterable elementIterable, ServiceTask serviceTask, StoryBus storyBus, Role role, MethodWrapper methodWrapper,
                                TaskComponentProxy targetProxy, MonitorTracking monitorTracking, Iterator<?> iterator, int stride, boolean isOneStride) {
        List<Object> resultList = Lists.newArrayList();
        List<ParamInjectDef> paramInjectDefs = methodWrapper.getParamInjectDefs();

        List<Object> asyncIterator = Lists.newArrayList();
        iterator.forEachRemaining(asyncIterator::add);
        Map<CompletableFuture<Object>, Integer> batchParamSizeMap = Maps.newHashMap();
        List<CompletableFuture<Object>> futureList = Lists.newArrayList();
        if (isOneStride) {
            for (int i = 0; i < asyncIterator.size(); i++) {
                int index = i;
                Object next = asyncIterator.get(index);
                CompletableFuture<Object> f = CompletableFuture.supplyAsync(() -> {
                    engineModule.getThreadSwitchHookProcessor().usePreviousData(threadSwitchHookObjectMap, storyBus.getScopeDataOperator());
                    return doInvokeMethod(index == 0, elementIterable,
                            new IterDataItem<>(false, next, Lists.newArrayList(), index), serviceTask, storyBus, role, methodWrapper, targetProxy, paramInjectDefs);
                }, engineModule.getIteratorThreadPool().getThreadPoolExecutor());
                futureList.add(f);
            }
        } else {
            List<List<Object>> partition = Lists.partition(asyncIterator, stride);
            for (int i = 0; i < partition.size(); i++) {
                int index = i;
                List<Object> next = partition.get(index);
                CompletableFuture<Object> f = CompletableFuture.supplyAsync(() -> {
                    engineModule.getThreadSwitchHookProcessor().usePreviousData(threadSwitchHookObjectMap, storyBus.getScopeDataOperator());
                    return doInvokeMethod(index == 0, elementIterable,
                            new IterDataItem<>(true, null, next, index), serviceTask, storyBus, role, methodWrapper, targetProxy, paramInjectDefs);
                }, engineModule.getIteratorThreadPool().getThreadPoolExecutor());
                futureList.add(f);
                batchParamSizeMap.put(f, next.size());
            }
        }
        monitorTracking.iterateCountTracking(serviceTask, futureList.size(), stride);
        futureList.forEach(f -> {
            Object ro;
            try {
                ro = f.get(storyBus.remainTimeMillis(), TimeUnit.MILLISECONDS);
            } catch (Throwable e) {
                throw ExceptionUtil.buildException(e, ExceptionEnum.ITERATE_ITEM_ERROR, null);
            }
            Integer batchParamSize = batchParamSizeMap.get(f);
            AssertUtil.isTrue(isOneStride || batchParamSize != null);
            if (ro == INVOKE_ERROR_SIGN) {
                if (BooleanUtils.isNotTrue(elementIterable.getIteAlignIndex())) {
                    return;
                }
                if (isOneStride) {
                    resultList.add(null);
                } else {
                    assert batchParamSize != null;
                    resultList.addAll(IntStream.range(0, batchParamSize).mapToObj(it -> null).collect(Collectors.toList()));
                }
                return;
            }
            addSuccessResult(serviceTask, isOneStride, resultList, elementIterable, ro, batchParamSize);
        });
        return resultList;
    }

    private void addSuccessResult(ServiceTask serviceTask, boolean isOneStride, List<Object> resultList, ElementIterable elementIterable, Object ro, Integer batchParamSize) {
        if (isOneStride) {
            if (ro != null || BooleanUtils.isTrue(elementIterable.getIteAlignIndex())) {
                resultList.add(ro);
            }
            return;
        }
        AssertUtil.isTrue(ro instanceof List, ExceptionEnum.ITERATE_ITEM_ERROR,
                "The return value type in batch iteration must be list. identity: {}", serviceTask.identity());
        AssertUtil.isTrue(BooleanUtils.isNotTrue(elementIterable.getIteAlignIndex()) || Objects.equals(((Collection<?>) ro).size(), batchParamSize), ExceptionEnum.ITERATE_ITEM_ERROR,
                "Batch iteration is where the size of return value list must be equal to the number of incoming parameters. expect: {}, actual: {}, identity: {}",
                batchParamSize, ((Collection<?>) ro).size(), serviceTask.identity());
        resultList.addAll(((Collection<?>) ro).stream().filter(ri -> ri != null || BooleanUtils.isTrue(elementIterable.getIteAlignIndex())).collect(Collectors.toList()));
    }

    /**
     * 实际调用目标方法
     */
    private Object doInvokeMethod(boolean tracking, ElementIterable elementIterable, IterDataItem<?> iterDataItem, ServiceTask serviceTask, StoryBus storyBus, Role role,
                                  MethodWrapper methodWrapper, TaskComponentProxy targetProxy, List<ParamInjectDef> paramInjectDefs) {
        try {
            InvokeMethodThreadLocal.setDataItem(iterDataItem);
            InvokeMethodThreadLocal.setTaskProperty(serviceTask.getTaskProperty());
            InvokeMethodThreadLocal.setKvScope(new KvScope(methodWrapper.getKvScope(), storyBus.getBusinessId()));
            InvokeMethodThreadLocal.setServiceTask(serviceTask);
            if (CollectionUtils.isEmpty(paramInjectDefs)) {
                return ProxyUtil.invokeMethod(methodWrapper, serviceTask, targetProxy.getTarget());
            }

            Function<ParamInjectDef, Object> paramInitStrategy = engineModule.getParamInitStrategy();
            TaskInstructWrapper taskInstructWrapper = methodWrapper.getTaskInstructWrapper().orElse(null);
            return ProxyUtil.invokeMethod(methodWrapper, serviceTask, targetProxy.getTarget(), () -> {
                Object[] params = TaskServiceUtil.getTaskParams(methodWrapper.isCustomRole(), tracking,
                        serviceTask, storyBus, role, taskInstructWrapper, paramInjectDefs, paramInitStrategy, engineModule.getApplicationContext(), iterDataItem);
                TaskServiceUtil.fillTaskParams(params, serviceTask.getTaskParams(), paramInjectDefs, paramInitStrategy, storyBus.getScopeDataOperator());
                if (ArrayUtils.isNotEmpty(params)) {
                    boolean supportValidate = GlobalUtil.supportValidate();
                    for (Object param : params) {
                        if (param instanceof ParamLifecycle) {
                            ((ParamLifecycle) param).after(storyBus.getScopeDataOperator());
                        }
                        if (supportValidate) {
                            RequestValidator.validate(param);
                        }
                    }
                }
                return params;
            });
        } catch (Throwable e) {
            if (elementIterable == null || !elementIterable.iterable() || elementIterable.getIteStrategy() == null || elementIterable.getIteStrategy() == IterateStrategyEnum.ALL_SUCCESS) {
                throw e;
            }
            LOGGER.warn("[{}] {} identity: {}", ExceptionEnum.ITERATE_ITEM_ERROR.getExceptionCode(), ExceptionEnum.ITERATE_ITEM_ERROR.getDesc(), serviceTask.identity(), e);
            return INVOKE_ERROR_SIGN;
        } finally {
            InvokeMethodThreadLocal.clear();
        }
    }

    private ElementIterable getElementIterable(ServiceTask serviceTask, ElementIterable elementIterable) {
        BasicElementIterable iterable = new BasicElementIterable();
        serviceTask.getElementIterable().ifPresent(iterable::mergeProperty);
        iterable.mergeProperty(elementIterable);
        return iterable;
    }
}
