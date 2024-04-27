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
package cn.kstry.framework.core.engine;

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.bpmn.extend.ElementIterable;
import cn.kstry.framework.core.bus.IterDataItem;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.container.component.InvokeProperties;
import cn.kstry.framework.core.container.component.MethodWrapper;
import cn.kstry.framework.core.container.component.ParamInjectDef;
import cn.kstry.framework.core.container.component.TaskServiceDef;
import cn.kstry.framework.core.container.task.impl.TaskComponentProxy;
import cn.kstry.framework.core.engine.future.InvokeFuture;
import cn.kstry.framework.core.engine.thread.InvokeMethodThreadLocal;
import cn.kstry.framework.core.engine.thread.MethodInvokeTask;
import cn.kstry.framework.core.engine.thread.Task;
import cn.kstry.framework.core.engine.thread.hook.ThreadSwitchHook;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.kv.KvScope;
import cn.kstry.framework.core.monitor.DemotionInfo;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.ProxyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

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
    protected static final Object INVOKE_ERROR_SIGN = new Object();

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

    /**
     * 参数解析器
     */
    private final TaskParamParser taskParamParser;

    protected final Map<ThreadSwitchHook<Object>, Object> threadSwitchHookObjectMap;

    public BasicTaskCore(StoryEngineModule engineModule, FlowRegister flowRegister, StoryBus storyBus, Role role, String taskName) {
        AssertUtil.notBlank(taskName);
        AssertUtil.anyNotNull(engineModule, flowRegister, storyBus, role);
        this.engineModule = engineModule;
        this.flowRegister = flowRegister;
        this.storyBus = storyBus;
        this.role = role;
        this.taskName = taskName;
        this.taskParamParser = new TaskParamParser(engineModule);
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

    protected Object retryInvokeMethod(boolean tracking, ElementIterable elementIterable,
                                       IterDataItem<Object> iterDataItem, TaskServiceDef taskServiceDef, ServiceTask serviceTask, StoryBus storyBus, Role role) {
        MethodWrapper methodWrapper = taskServiceDef.getMethodWrapper();
        InvokeProperties invokeProperties = methodWrapper.getInvokeProperties();
        if (taskServiceDef.isDemotionNode()) {
            return doInvokeMethod(tracking, null, taskServiceDef, serviceTask, storyBus, role);
        }

        Supplier<Optional<TaskServiceDef>> needDemotionSupplier = getNeedDemotionSupplier(role, invokeProperties);
        int retry = Optional.ofNullable(serviceTask.getRetryTimes()).filter(t -> t > 0).orElse(invokeProperties.getRetry());
        for (int i = 0; i <= retry; i++) {
            try {
                MethodInvokeTask.MethodInvokePedometer pedometer =
                        new MethodInvokeTask.MethodInvokePedometer(retry - i, needDemotionSupplier, false, invokeProperties.isStrictMode());
                return doRetryInvokeMethod(tracking, elementIterable, iterDataItem, pedometer, serviceTask, taskServiceDef, storyBus, role);
            } catch (Throwable exception) {
                KstryException ke = ExceptionUtil.buildException(exception, ExceptionEnum.SERVICE_INVOKE_ERROR, null);
                if (flowRegister.getAdminFuture().isCancelled(flowRegister.getStartEventId())) {
                    throw ke;
                }
                String taskName = GlobalUtil.getTaskName(serviceTask, flowRegister.getRequestId());
                int iFinal = i;
                ke.log(e -> LOGGER.warn("[{}] Target method execution failed, retry for the {}th time. identity: {}, taskName: {}, exception: {}",
                        ExceptionEnum.SERVICE_INVOKE_ERROR.getExceptionCode(), iFinal, serviceTask.identity(), taskName, exception.getMessage(), e));
                if (i >= retry || notAllowRetry(exception, invokeProperties)) {
                    Optional<TaskServiceDef> serviceDefOptional = needDemotionSupplier.get();
                    if (!serviceDefOptional.isPresent()) {
                        if (needIterateIgnore(elementIterable)) {
                            LOGGER.warn("[{}] {} identity: {}", ExceptionEnum.ITERATE_ITEM_ERROR.getExceptionCode(), ExceptionEnum.ITERATE_ITEM_ERROR.getDesc(), serviceTask.identity(), exception);
                            return INVOKE_ERROR_SIGN;
                        }
                        throw ke;
                    }
                    MethodInvokeTask.MethodInvokePedometer pedometer = new MethodInvokeTask.MethodInvokePedometer(0, needDemotionSupplier, true, invokeProperties.isStrictMode());
                    DemotionInfo demotionInfo = new DemotionInfo();
                    demotionInfo.setRetryTimes(i);
                    demotionInfo.setDemotionNodeId(serviceDefOptional.get().getGetServiceNodeResource().getIdentityId());
                    demotionInfo.setDemotionSuccess(true);
                    try {
                        flowRegister.getMonitorTracking().demotionTaskTracking(serviceTask, demotionInfo);
                        return doRetryInvokeMethod(tracking, elementIterable, iterDataItem, pedometer, serviceTask, serviceDefOptional.get(), storyBus, role);
                    } catch (Throwable ex) {
                        demotionInfo.setDemotionSuccess(false);
                        demotionInfo.setDemotionException(ex);
                        KstryException kex = ExceptionUtil.buildException(ex, ExceptionEnum.DEMOTION_DEFINITION_ERROR, null);
                        kex.log(e -> LOGGER.warn("[{}] Target method execution failed, demotion policy execution failed. identity: {}, taskName: {}, exception: {}",
                                e.getErrorCode(), serviceTask.identity(), taskName, ex.getMessage(), e));
                        if (needIterateIgnore(elementIterable)) {
                            LOGGER.warn("[{}] {} demotion identity: {}", ExceptionEnum.ITERATE_ITEM_ERROR.getExceptionCode(), ExceptionEnum.ITERATE_ITEM_ERROR.getDesc(), serviceTask.identity(), ex);
                            return INVOKE_ERROR_SIGN;
                        }
                        throw kex;
                    } finally {
                        flowRegister.getMonitorTracking().demotionTaskTracking(serviceTask, demotionInfo);
                    }
                } else {
                    DemotionInfo demotionInfo = new DemotionInfo();
                    demotionInfo.setRetryTimes(i + 1);
                    flowRegister.getMonitorTracking().demotionTaskTracking(serviceTask, demotionInfo);
                }
            }
        }
        throw ExceptionUtil.buildException(null, ExceptionEnum.SERVICE_INVOKE_ERROR, null);
    }

    private Object doRetryInvokeMethod(boolean tracking, ElementIterable elementIterable, IterDataItem<Object> iterDataItem,
                                       MethodInvokeTask.MethodInvokePedometer pedometer, ServiceTask serviceTask, TaskServiceDef taskServiceDef, StoryBus storyBus, Role role) {
        boolean isIterable = elementIterable != null && iterDataItem != null;
        MethodWrapper methodWrapper = taskServiceDef.getMethodWrapper();
        InvokeProperties invokeProperties = methodWrapper.getInvokeProperties();
        Integer timeout = getTaskTimeout(pedometer.isDemotion(), serviceTask, invokeProperties);
        ExecutorService executor = null;
        if (StringUtils.isNotBlank(invokeProperties.getCustomExecutorName())) {
            boolean notNeedIterate = !isIterable || !elementIterable.iterable() || taskServiceDef.isDemotionNode();
            if (notNeedIterate || notNeedAsyncIterate(methodWrapper, elementIterable)) {
                executor = engineModule.getApplicationContext().getBean(invokeProperties.getCustomExecutorName(), ExecutorService.class);
            }
        }
        if (executor == null && (timeout == null || methodWrapper.isMonoResult())) {
            return doInvokeMethod(tracking, iterDataItem, taskServiceDef, serviceTask, storyBus, role);
        }
        MethodInvokeTask methodInvokeTask = new MethodInvokeTask(tracking, elementIterable, iterDataItem, pedometer, flowRegister, engineModule, serviceTask, taskServiceDef, storyBus, role);
        InvokeFuture invokeFuture = engineModule.getMethodThreadPool().submitMethodInvokeTask(executor, methodInvokeTask);
        return invokeFuture.invokeMethod(timeout == null ? storyBus.remainTimeMillis() : timeout, flowRegister.getMonitorTracking(), serviceTask);
    }

    /**
     * 实际调用目标方法
     */
    protected Object doInvokeMethod(boolean tracking, IterDataItem<?> iterDataItem, TaskServiceDef taskServiceDef, ServiceTask serviceTask, StoryBus storyBus, Role role) {
        MethodWrapper methodWrapper = taskServiceDef.getMethodWrapper();
        TaskComponentProxy targetProxy = taskServiceDef.getTaskComponentTarget();
        List<ParamInjectDef> paramInjectDefs = methodWrapper.getParamInjectDefs();
        try {
            InvokeMethodThreadLocal.setDataItem(iterDataItem);
            InvokeMethodThreadLocal.setTaskProperty(serviceTask.getTaskProperty());
            InvokeMethodThreadLocal.setKvScope(new KvScope(methodWrapper.getKvScope(), storyBus.getBusinessId()));
            InvokeMethodThreadLocal.setServiceTask(serviceTask);
            if (CollectionUtils.isEmpty(paramInjectDefs)) {
                return ProxyUtil.invokeMethod(methodWrapper, serviceTask, targetProxy.getTarget());
            }
            return ProxyUtil.invokeMethod(methodWrapper, serviceTask, targetProxy.getTarget(), () ->
                    taskParamParser.parseParams(tracking, iterDataItem, serviceTask, storyBus, role, methodWrapper, paramInjectDefs)
            );
        } finally {
            InvokeMethodThreadLocal.clear();
        }
    }

    boolean notNeedAsyncIterate(MethodWrapper methodWrapper, ElementIterable elementIterable) {
        if (elementIterable == null) {
            return true;
        }
        return BooleanUtils.isNotTrue(elementIterable.openAsync()) || elementIterable.getIteStrategy() == IterateStrategyEnum.ANY_SUCCESS || methodWrapper.isMonoResult();
    }

    private boolean notAllowRetry(Throwable exception, InvokeProperties invokeProperties) {
        if (CollectionUtils.isEmpty(invokeProperties.getRetryIncludeExceptionList()) && CollectionUtils.isEmpty(invokeProperties.getRetryExcludeExceptionList())) {
            return false;
        }
        if (CollectionUtils.isNotEmpty(invokeProperties.getRetryIncludeExceptionList()) && CollectionUtils.isNotEmpty(invokeProperties.getRetryExcludeExceptionList())) {
            return invokeProperties.getRetryIncludeExceptionList().stream().noneMatch(c -> c.isAssignableFrom(exception.getClass()))
                    || invokeProperties.getRetryExcludeExceptionList().stream().anyMatch(c -> c.isAssignableFrom(exception.getClass()));
        }
        if (CollectionUtils.isNotEmpty(invokeProperties.getRetryIncludeExceptionList())) {
            return invokeProperties.getRetryIncludeExceptionList().stream().noneMatch(c -> c.isAssignableFrom(exception.getClass()));
        }
        return invokeProperties.getRetryExcludeExceptionList().stream().anyMatch(c -> c.isAssignableFrom(exception.getClass()));
    }

    Supplier<Optional<TaskServiceDef>> getNeedDemotionSupplier(Role role, InvokeProperties invokeProperties) {
        return () -> {
            try {
                ServiceNodeResource demotionResource = invokeProperties.getDemotionResource();
                if (demotionResource == null) {
                    return Optional.empty();
                }
                Optional<TaskServiceDef> resultOptional = engineModule.getTaskContainer()
                        .getTaskServiceDef(demotionResource.getComponentName(), demotionResource.getServiceName(), role)
                        .filter(def -> {
                            String an1 = def.getGetServiceNodeResource().getAbilityName();
                            String an2 = demotionResource.getAbilityName();
                            return StringUtils.isAllBlank(an1, an2) || Objects.equals(an1, an2);
                        }).map(def -> {
                            def.setDemotionNode(true);
                            return def;
                        });
                if (!resultOptional.isPresent()) {
                    LOGGER.warn("[{}] {} demotion: {}",
                            ExceptionEnum.DEMOTION_DEFINITION_ERROR.getExceptionCode(), ExceptionEnum.DEMOTION_DEFINITION_ERROR.getDesc(), demotionResource.getIdentityId());
                }
                return resultOptional;
            } catch (Throwable e) {
                LOGGER.warn(e.getMessage(), e);
                return Optional.empty();
            }
        };
    }

    Integer getTaskTimeout(boolean isDemotion, ServiceTask serviceTask, InvokeProperties invokeProperties) {
        return isDemotion
                ? Optional.ofNullable(invokeProperties.getTimeout()).filter(t -> t >= 0).orElse(serviceTask.getTimeout())
                : Optional.ofNullable(serviceTask.getTimeout()).filter(t -> t >= 0).orElse(invokeProperties.getTimeout());
    }

    protected boolean needIterateIgnore(ElementIterable elementIterable) {
        return elementIterable != null && elementIterable.iterable() && elementIterable.getIteStrategy() != IterateStrategyEnum.ALL_SUCCESS;
    }
}
