/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
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

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.SequenceFlow;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.bus.BasicStoryBus;
import cn.kstry.framework.core.component.hook.FlowElementHook;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.container.MethodWrapper;
import cn.kstry.framework.core.container.TaskContainer;
import cn.kstry.framework.core.engine.facade.CustomRoleInfo;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.task.facade.TaskServiceDef;
import cn.kstry.framework.core.task.impl.TaskComponentRegisterProxy;
import cn.kstry.framework.core.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

/**
 *
 * @author lykan
 */
public abstract class BasicFlowTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicFlowTask.class);

    private final ThreadPoolExecutor asyncThreadPool;

    private final TaskContainer taskContainer;

    private final Function<MethodWrapper.ParamInjectDef, Object> paramInitStrategy;

    public BasicFlowTask(ThreadPoolExecutor asyncThreadPool, TaskContainer taskContainer, Function<MethodWrapper.ParamInjectDef, Object> paramInitStrategy) {
        AssertUtil.notNull(taskContainer);
        AssertUtil.notNull(asyncThreadPool);
        AssertUtil.notNull(paramInitStrategy);
        this.asyncThreadPool = asyncThreadPool;
        this.taskContainer = taskContainer;
        this.paramInitStrategy = paramInitStrategy;
    }

    protected void doExe(Role role, BasicStoryBus storyBus, FlowRegister flowRegister) {
        for (Optional<FlowElement> next = flowRegister.nextElement(storyBus); next.isPresent(); next = flowRegister.nextElement(storyBus)) {
            FlowElement flowElement = next.get();
            if (ElementPropertyUtil.needOpenAsync(flowElement)) {
                submitAsyncTask(role, storyBus, flowRegister, flowElement);
                continue;
            }

            if (flowElement.getElementType() != BpmnTypeEnum.SERVICE_TASK) {
                continue;
            }

            ServiceTask serviceTask = (ServiceTask) flowElement;
            Optional<TaskServiceDef> taskServiceDefOptional =
                    getTaskContainer().getTaskServiceDef(serviceTask.getTaskComponent(), serviceTask.getTaskService(), role);
            if (!taskServiceDefOptional.isPresent() && serviceTask.allowAbsent()) {
                continue;
            }

            TaskServiceDef taskServiceDef = taskServiceDefOptional.orElseThrow(() ->
                    KstryException.buildException(ExceptionEnum.TASK_SERVICE_MATCH_ERROR, ExceptionEnum.TASK_SERVICE_MATCH_ERROR.getDesc()
                            + GlobalUtil.format(" service task id: {}, name: {}", serviceTask.getId(), serviceTask.getName())));
            flowRegister.getMonitorTracking().getServiceNodeTracking(flowElement).ifPresent(nodeTracking -> {
                MethodWrapper methodWrapper = taskServiceDef.getMethodWrapper();
                nodeTracking.setMethodName(methodWrapper.getMethod().getName());
                nodeTracking.setTargetName(taskServiceDef.getTaskComponentTarget().getTarget().getClass().getName());
                nodeTracking.setAbility(Optional.ofNullable(methodWrapper.getAbility()).filter(StringUtils::isNotBlank).orElse(null));
            });

            Object result;
            try {
                result = doInvokeMethod(serviceTask, taskServiceDef, storyBus, role);
            } catch (Throwable exception) {
                flowRegister.getMonitorTracking().finishTaskTracking(flowElement, exception);
                if (!serviceTask.strictMode()) {
                    LOGGER.warn(exception.getMessage(), exception);
                    continue;
                }
                throw exception;
            }
            if (result instanceof Mono) {
                monoResultHandler(role, storyBus, flowRegister, serviceTask, taskServiceDef, result);
                break;
            }
            resultHandler(role, storyBus, serviceTask, result, taskServiceDef);
            flowRegister.getMonitorTracking().finishTaskTracking(flowElement, null);
        }
    }

    protected TaskContainer getTaskContainer() {
        return taskContainer;
    }

    protected Function<MethodWrapper.ParamInjectDef, Object> getParamInitStrategy() {
        return paramInitStrategy;
    }

    public ThreadPoolExecutor getAsyncThreadPool() {
        return asyncThreadPool;
    }

    protected abstract AsyncPropertyDef getAsyncPropertyDef();

    @SuppressWarnings("unchecked")
    private void monoResultHandler(Role role, BasicStoryBus scopeData,
                                   FlowRegister flowRegister, ServiceTask serviceTask, TaskServiceDef taskServiceDef, Object result) {
        GlobalUtil.transferNotEmpty(result, Mono.class).subscribe(new FlowTaskSubscriber(flowRegister) {

            @Override
            protected void doNextHook(Object value) {
                resultHandler(role, scopeData, serviceTask, value, taskServiceDef);
                flowRegister.getMonitorTracking().finishTaskTracking(serviceTask, null);
                AsyncFlowTask asyncFlowTask = new AsyncFlowTask(getAsyncPropertyDef(), flowRegister);
                try {
                    Future<AsyncTaskState> taskFuture = getAsyncThreadPool().submit(asyncFlowTask);
                    LOGGER.debug("submit async story task. startId: {}", asyncFlowTask.getStartEventId());
                    flowRegister.addTaskFuture(taskFuture);
                } finally {
                    asyncFlowTask.openSwitch();
                }
                dispose();
            }

            @Override
            protected void doErrorHook(Throwable throwable) {
                flowRegister.getMonitorTracking().finishTaskTracking(serviceTask, throwable);
                flowRegister.getAsyncTaskCell().errorNotice(throwable);
                flowRegister.getAsyncTaskCell().cancel();
            }

            @Override
            protected void doCompleteHook() {
                resultHandler(role, scopeData, serviceTask, null, taskServiceDef);
                flowRegister.getMonitorTracking().finishTaskTracking(serviceTask, null);
                AsyncFlowTask asyncFlowTask = new AsyncFlowTask(getAsyncPropertyDef(), flowRegister);
                try {
                    Future<AsyncTaskState> taskFuture = getAsyncThreadPool().submit(asyncFlowTask);
                    flowRegister.addTaskFuture(taskFuture);
                } finally {
                    asyncFlowTask.openSwitch();
                }
            }
        });
    }

    private void resultHandler(Role role, BasicStoryBus scopeData, ServiceTask serviceTask, Object result, TaskServiceDef taskServiceDef) {
        scopeData.noticeResult(serviceTask, result, taskServiceDef);
        customRoleInfo(role, scopeData, serviceTask);
    }

    private void customRoleInfo(Role role, BasicStoryBus scopeData, ServiceTask serviceTask) {
        Optional<TaskServiceDef> taskServiceDefOptional;
        TaskServiceDef taskServiceDef;
        CustomRoleInfo customRoleInfo = serviceTask.getCustomRoleInfo();
        if (customRoleInfo == null) {
            return;
        }
        taskServiceDefOptional = getTaskContainer().getTaskServiceDef(customRoleInfo.getTaskComponentName(), customRoleInfo.getTaskServiceName(), role);
        if (!taskServiceDefOptional.isPresent() && serviceTask.allowAbsent()) {
            return;
        }
        taskServiceDef = taskServiceDefOptional.orElseThrow(() ->
                KstryException.buildException(ExceptionEnum.TASK_SERVICE_MATCH_ERROR, ExceptionEnum.TASK_SERVICE_MATCH_ERROR.getDesc()
                        + GlobalUtil.format(" service task id: {}, name: {}", serviceTask.getId(), serviceTask.getName())));
        doInvokeMethod(serviceTask, taskServiceDef, scopeData, role);
    }

    @SuppressWarnings("unchecked")
    private void submitAsyncTask(Role role, BasicStoryBus storyBus, FlowRegister flowRegister, FlowElement flowElement) {
        assert flowElement instanceof FlowElementHook;
        FlowElementHook<List<FlowElement>> asyncFlowHook = (FlowElementHook<List<FlowElement>>) flowElement;
        asyncFlowHook.hook(list -> {
            AsyncTaskForkJoin asyncForkJoin = new AsyncTaskForkJoin();
            asyncForkJoin.setRole(role);
            asyncForkJoin.setStoryBus(storyBus);
            asyncForkJoin.setTaskContainer(getTaskContainer());
            asyncForkJoin.setParamInitStrategy(getParamInitStrategy());
            asyncForkJoin.setAsyncThreadPool(getAsyncThreadPool());
            for (FlowElement asyncFlow : list) {
                SequenceFlow sequenceFlow = (SequenceFlow) asyncFlow;
                FlowRegister asyncFlowRegister = flowRegister.asyncFlowRegister(sequenceFlow);
                asyncForkJoin.submitTask(asyncFlowRegister);
            }
        });
        asyncFlowHook.trigger();
    }

    private Object doInvokeMethod(ServiceTask serviceTask, TaskServiceDef taskServiceDef, BasicStoryBus storyBus, Role role) {
        MethodWrapper methodWrapper = taskServiceDef.getMethodWrapper();
        TaskComponentRegisterProxy targetProxy = taskServiceDef.getTaskComponentTarget();
        AssertUtil.notNull(methodWrapper.getMethod());
        AssertUtil.notNull(targetProxy.getTarget());

        List<MethodWrapper.ParamInjectDef> paramInjectDefs = methodWrapper.getParamInjectDefs();
        if (CollectionUtils.isEmpty(paramInjectDefs)) {
            return ProxyUtil.invokeMethod(storyBus, methodWrapper, targetProxy.getTarget());
        }

        return ProxyUtil.invokeMethod(storyBus, methodWrapper, targetProxy.getTarget(),
                () -> TaskServiceUtil.getTaskParams(serviceTask, storyBus, role, targetProxy, paramInjectDefs, getParamInitStrategy()));
    }

    private static abstract class FlowTaskSubscriber extends BaseSubscriber<Object> {

        private final FlowRegister flowRegister;

        public FlowTaskSubscriber(FlowRegister flowRegister) {
            this.flowRegister = flowRegister;
        }

        @Override
        protected void hookOnSubscribe(@Nonnull Subscription subscription) {
            request(1);
        }

        @Override
        protected void hookOnNext(@Nonnull Object value) {
            try {
                MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, flowRegister.getRequestId());
                if (flowRegister.getAsyncTaskCell().isCancelled()) {
                    LOGGER.info("[{}] Task interrupted. Story task was cancelled! startId: {}",
                            ExceptionEnum.TASK_CANCELLED.getExceptionCode(), flowRegister.getStartFlowElement().getId());
                    return;
                }
                doNextHook(value);
            } finally {
                MDC.clear();
            }
        }

        @Override
        protected void hookOnComplete() {
            try {
                MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, flowRegister.getRequestId());
                if (flowRegister.getAsyncTaskCell().isCancelled()) {
                    LOGGER.info("[{}] Task interrupted. Story task was cancelled! startId: {}",
                            ExceptionEnum.TASK_CANCELLED.getExceptionCode(), flowRegister.getStartFlowElement().getId());
                    return;
                }
                doCompleteHook();
            } finally {
                MDC.clear();
            }
        }

        @Override
        protected void hookOnError(@Nonnull Throwable throwable) {
            try {
                MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, flowRegister.getRequestId());
                doErrorHook(throwable);
            } finally {
                MDC.clear();
            }
        }

        abstract void doNextHook(Object value);

        abstract void doErrorHook(Throwable throwable);

        abstract void doCompleteHook();
    }
}
