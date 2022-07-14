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

import cn.kstry.framework.core.bpmn.*;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.bus.ContextStoryBus;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.component.hook.AsyncFlowHook;
import cn.kstry.framework.core.component.hook.Hook;
import cn.kstry.framework.core.container.component.InvokeProperties;
import cn.kstry.framework.core.container.component.MethodWrapper;
import cn.kstry.framework.core.container.component.TaskServiceDef;
import cn.kstry.framework.core.engine.future.FlowTaskSubscriber;
import cn.kstry.framework.core.engine.future.InvokeFuture;
import cn.kstry.framework.core.engine.interceptor.SubProcessInterceptorRepository;
import cn.kstry.framework.core.engine.thread.FragmentTask;
import cn.kstry.framework.core.engine.thread.MethodInvokeTask;
import cn.kstry.framework.core.engine.thread.MonoFlowTask;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.monitor.DemotionInfo;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 流程任务执行核心
 *
 * @author lykan
 */
public abstract class FlowTaskCore<T> extends BasicTaskCore<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowTaskCore.class);

    public FlowTaskCore(StoryEngineModule engineModule, FlowRegister flowRegister, Role role, StoryBus storyBus) {
        super(engineModule, flowRegister, storyBus, role, GlobalUtil.getTaskName(flowRegister.getStartElement(), flowRegister.getRequestId()));
    }

    protected void doExe(Role role, StoryBus storyBus, FlowRegister flowRegister) {
        ContextStoryBus csd;
        Optional<FlowElement> next;
        for (csd = new ContextStoryBus(storyBus),
                     next = flowRegister.nextElement(csd); next.isPresent(); csd = new ContextStoryBus(storyBus), next = flowRegister.nextElement(csd)) {
            FlowElement flowElement = next.get();
            if (!doInvoke(role, storyBus, flowRegister, flowElement)) {
                break;
            }
            Optional<AsyncFlowHook<List<FlowElement>>> asyncFlowHook = flowRegister.predictNextElement(csd, flowElement);
            if (asyncFlowHook.isPresent() && asyncFlowHook.get().openAsync()) {
                submitAsyncTask(role, storyBus, flowRegister, asyncFlowHook.get());
            }
        }
    }

    private boolean doInvoke(Role role, StoryBus storyBus, FlowRegister flowRegister, FlowElement flowElement) {
        if (flowElement.getElementType() == BpmnTypeEnum.SUB_PROCESS) {
            SubProcess subProcess = GlobalUtil.transferNotEmpty(flowElement, SubProcess.class);
            SubProcessInterceptorRepository subInterceptorRepository = engineModule.getSubInterceptorRepository();
            if (!subInterceptorRepository.postBeforeProcessor(storyBus, subProcess.getStartEvent().getId(), flowRegister.getStoryId())) {
                return true;
            }
            subProcessTaskHandler(role, storyBus, flowRegister, subProcess);
            return false;
        }

        if (flowElement.getElementType() != BpmnTypeEnum.SERVICE_TASK) {
            return true;
        }

        ServiceTask serviceTask = (ServiceTask) flowElement;
        Optional<TaskServiceDef> taskServiceDefOptional =
                engineModule.getTaskContainer().getTaskServiceDef(serviceTask.getTaskComponent(), serviceTask.getTaskService(), role);
        if (!taskServiceDefOptional.isPresent() && serviceTask.allowAbsent()) {
            return true;
        }
        TaskServiceDef taskServiceDef = taskServiceDefOptional.orElseThrow(() ->
                ExceptionUtil.buildException(null, ExceptionEnum.TASK_SERVICE_MATCH_ERROR, ExceptionEnum.TASK_SERVICE_MATCH_ERROR.getDesc()
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
            InvokeProperties invokeProperties = taskServiceDef.getMethodWrapper().getInvokeProperties();
            if (!(serviceTask.strictMode() && invokeProperties.isStrictMode())) {
                LOGGER.info("[{}] Target method execution failure, error is ignored in non-strict mode. exception: {}",
                        ExceptionEnum.SERVICE_INVOKE_ERROR.getExceptionCode(), exception.getMessage(), exception);
                return true;
            }
            throw exception;
        }
        if (result instanceof Mono) {
            monoResultHandler(role, storyBus, flowRegister, serviceTask, taskServiceDef, result);
            return false;
        }
        storyBus.noticeResult(serviceTask, result, taskServiceDef);
        customRoleInfo(role, storyBus, serviceTask);
        flowRegister.getMonitorTracking().finishTaskTracking(flowElement, null);
        return true;
    }

    @SuppressWarnings("unchecked")
    private void monoResultHandler(Role role, StoryBus storyBus,
                                   FlowRegister flowRegister, ServiceTask serviceTask, TaskServiceDef taskServiceDef, Object result) {
        InvokeProperties invokeProperties = taskServiceDef.getMethodWrapper().getInvokeProperties();
        boolean strictMode = serviceTask.strictMode() && invokeProperties.isStrictMode();
        Integer timeout = Optional.ofNullable(serviceTask.getTimeout()).filter(t -> t >= 0).orElse(invokeProperties.getTimeout());
        FlowTaskSubscriber flowTaskSubscriber = new FlowTaskSubscriber(strictMode, timeout, flowRegister, getTaskName()) {

            @Override
            protected void doNextHook(Object value) {
                resultHandler(flowRegister, serviceTask, storyBus, role, value);
            }

            @Override
            protected void doErrorHook(Throwable throwable) {
                flowRegister.getMonitorTracking().finishTaskTracking(serviceTask, throwable);
                Supplier<Optional<TaskServiceDef>> needDemotionSupplier = getNeedDemotionSupplier(role, invokeProperties);
                Optional<TaskServiceDef> demotionServiceDefOptional = needDemotionSupplier.get();
                KstryException kstryException = ExceptionUtil.buildException(throwable, ExceptionEnum.SERVICE_INVOKE_ERROR, null);
                if (!flowRegister.getAdminFuture().isCancelled(flowRegister.getStartEventId()) && demotionServiceDefOptional.isPresent()) {
                    kstryException.log(e -> LOGGER.warn("[{}] Target method execution failed. taskName: {}, exception: {}",
                            e.getErrorCode(), getTaskName(), throwable.getMessage(), e));
                    TaskServiceDef demotionTaskServiceDef = demotionServiceDefOptional.get();
                    DemotionInfo demotionInfo = new DemotionInfo();
                    demotionInfo.setRetryTimes(0);
                    demotionInfo.setDemotionNodeId(demotionTaskServiceDef.getGetServiceNodeResource().getIdentityId());
                    demotionInfo.setDemotionSuccess(true);
                    try {
                        Object o = doInvokeMethod(serviceTask, demotionTaskServiceDef, storyBus, role);
                        if (o instanceof Mono) {
                            GlobalUtil.transferNotEmpty(o, Mono.class).subscribe(new BaseSubscriber<Object>() {
                                @Override
                                protected void hookOnNext(@Nonnull Object value) {
                                    doNextHook(value);
                                    dispose();
                                }

                                @Override
                                protected void hookOnError(@Nonnull Throwable e) {
                                    LOGGER.warn("[{}] Target method execution failed, demotion policy execution failed. taskName: {}, exception: {}",
                                            ExceptionEnum.DEMOTION_DEFINITION_ERROR.getExceptionCode(), getTaskName(), e.getMessage(), e);
                                    dispose();
                                }

                                @Override
                                protected void hookOnComplete() {
                                    doNextHook(null);
                                    dispose();
                                }
                            });
                            return;
                        }
                        doNextHook(o);
                        return;
                    } catch (Throwable e) {
                        LOGGER.warn("[{}] Target method execution failed, demotion policy execution failed. taskName: {}, exception: {}",
                                ExceptionEnum.DEMOTION_DEFINITION_ERROR.getExceptionCode(), getTaskName(), e.getMessage(), e);
                    } finally {
                        flowRegister.getMonitorTracking().demotionTaskTracking(serviceTask, demotionInfo);
                    }
                }
                if (!isStrictMode()) {
                    kstryException.log(e -> LOGGER.warn("[{}] Target method execution failure, error is ignored in non-strict mode. exception: {}",
                            e.getErrorCode(), throwable.getMessage(), e));
                    doNextElement(flowRegister, serviceTask, storyBus, role);
                    return;
                }
                flowRegister.getAdminFuture().errorNotice(kstryException, flowRegister.getStartEventId());
            }

            @Override
            protected void doCompleteHook() {
                resultHandler(flowRegister, serviceTask, storyBus, role, null);
            }

            private void resultHandler(FlowRegister flowRegister, ServiceTask serviceTask, StoryBus scopeData, Role role, Object value) {
                scopeData.noticeResult(serviceTask, value, taskServiceDef);
                customRoleInfo(role, scopeData, serviceTask);
                doNextElement(flowRegister, serviceTask, storyBus, role);
            }
        };
        Mono<?> mono = GlobalUtil.transferNotEmpty(result, Mono.class);
        if (timeout != null) {
            mono = mono.timeout(Duration.ofMillis(timeout), Mono.fromRunnable(() -> {
                KstryException e = ExceptionUtil.buildException(null, ExceptionEnum.ASYNC_TASK_TIMEOUT,
                        GlobalUtil.format("Target method execution timeout! maximum time limit: {}ms, taskName: {}",
                                timeout, GlobalUtil.getTaskName(serviceTask, flowRegister.getRequestId())));
                flowTaskSubscriber.onError(e);
                flowTaskSubscriber.dispose();
            }));
        }
        mono.subscribe(flowTaskSubscriber);
    }

    private void subProcessTaskHandler(Role role, StoryBus storyBus, FlowRegister parentFlowRegister, SubProcess subProcess) {
        StartEvent startEvent = subProcess.getStartEvent();

        FlowRegister cloneSubFlowRegister = parentFlowRegister.cloneSubFlowRegister(startEvent);
        String taskName = GlobalUtil.getTaskName(cloneSubFlowRegister.getStartElement(), cloneSubFlowRegister.getRequestId());
        Integer timeout = Optional.of(subProcess).map(SubProcess::getTimeout).orElse(null);
        FlowTaskSubscriber flowTaskSubscriber = new FlowTaskSubscriber(subProcess.strictMode(), timeout, cloneSubFlowRegister, taskName) {

            @Override
            protected void doNextHook(Object value) {
                SubProcessInterceptorRepository subInterceptorRepository = engineModule.getSubInterceptorRepository();
                subInterceptorRepository.postAfterProcessor(storyBus, flowRegister.getStartElement().getId(), flowRegister.getStoryId());
                doNextElement(parentFlowRegister, subProcess, storyBus, role);
            }

            @Override
            protected void doErrorHook(Throwable throwable) {
                SubProcessInterceptorRepository subInterceptorRepository = engineModule.getSubInterceptorRepository();
                subInterceptorRepository.postErrorProcessor(throwable, storyBus, flowRegister.getStartElement().getId(), flowRegister.getStoryId());
                if (!isStrictMode()) {
                    doNextElement(parentFlowRegister, subProcess, storyBus, role);
                }
            }

            @Override
            protected void doCompleteHook() {
                throw ExceptionUtil.buildException(null, ExceptionEnum.STORY_ERROR, null);
            }

            @Override
            protected void doFinallyHook() {
                SubProcessInterceptorRepository subInterceptorRepository = engineModule.getSubInterceptorRepository();
                subInterceptorRepository.postFinallyProcessor(storyBus, flowRegister.getStartElement().getId(), flowRegister.getStoryId());
            }
        };
        MonoFlowTask subFlowTask = new MonoFlowTask(engineModule, cloneSubFlowRegister, role, storyBus, flowTaskSubscriber);
        engineModule.getTaskThreadPool().submitMonoFlowTask(parentFlowRegister.getStartEventId(), subFlowTask);
    }

    private void customRoleInfo(Role role, StoryBus scopeData, ServiceTask serviceTask) {
        Optional<TaskServiceDef> taskServiceDefOptional;
        TaskServiceDef taskServiceDef;
        ServiceNodeResource customRoleInfo = serviceTask.getCustomRoleInfo();
        if (customRoleInfo == null) {
            return;
        }
        taskServiceDefOptional = engineModule.getTaskContainer().getTaskServiceDef(customRoleInfo.getComponentName(), customRoleInfo.getServiceName(), role);
        if (!taskServiceDefOptional.isPresent() && serviceTask.allowAbsent()) {
            return;
        }
        taskServiceDef = taskServiceDefOptional.orElseThrow(() ->
                ExceptionUtil.buildException(null, ExceptionEnum.TASK_SERVICE_MATCH_ERROR, ExceptionEnum.TASK_SERVICE_MATCH_ERROR.getDesc()
                        + GlobalUtil.format(" service task id: {}, name: {}", serviceTask.getId(), serviceTask.getName())));
        doInvokeMethod(serviceTask, taskServiceDef, scopeData, role);
    }

    private void submitAsyncTask(Role role, StoryBus storyBus, FlowRegister flowRegister, Hook<List<FlowElement>> hook) {
        hook.hook(list -> {
            for (FlowElement asyncFlow : list) {
                SequenceFlow sequenceFlow = (SequenceFlow) asyncFlow;
                FlowRegister asyncFlowRegister = flowRegister.asyncFlowRegister(sequenceFlow);
                FragmentTask fragmentTask = new FragmentTask(engineModule, asyncFlowRegister, role, storyBus);
                engineModule.getTaskThreadPool().submitFragmentTask(fragmentTask);
            }
        }).trigger();
    }

    private void doNextElement(FlowRegister flowRegister, FlowElement flowElement, StoryBus storyBus, Role role) {
        flowRegister.getMonitorTracking().finishTaskTracking(flowElement, null);
        Optional<AsyncFlowHook<List<FlowElement>>> asyncFlowHook = flowRegister.predictNextElement(new ContextStoryBus(storyBus), flowElement);
        if (asyncFlowHook.isPresent() && asyncFlowHook.get().openAsync()) {
            submitAsyncTask(role, storyBus, flowRegister, asyncFlowHook.get());
        } else {
            FragmentTask fragmentTask = new FragmentTask(engineModule, flowRegister, role, storyBus);
            engineModule.getTaskThreadPool().submitFragmentTask(fragmentTask);
        }
    }

    @Override
    protected Object doInvokeMethod(ServiceTask serviceTask, TaskServiceDef taskServiceDef, StoryBus storyBus, Role role) {
        if (taskServiceDef.isDemotionNode()) {
            return super.doInvokeMethod(serviceTask, taskServiceDef, storyBus, role);
        }
        MethodWrapper methodWrapper = taskServiceDef.getMethodWrapper();
        InvokeProperties invokeProperties = methodWrapper.getInvokeProperties();
        Supplier<Optional<TaskServiceDef>> needDemotionSupplier = getNeedDemotionSupplier(role, invokeProperties);
        int retry = invokeProperties.getRetry();
        for (int i = 0; i <= retry; i++) {
            try {
                MethodInvokeTask.MethodInvokePedometer pedometer =
                        new MethodInvokeTask.MethodInvokePedometer(retry - i, needDemotionSupplier, false, invokeProperties.isStrictMode());
                return retryInvokeMethod(pedometer, serviceTask, taskServiceDef, storyBus, role);
            } catch (Throwable exception) {
                KstryException ke = ExceptionUtil.buildException(exception, ExceptionEnum.SERVICE_INVOKE_ERROR, null);
                if (flowRegister.getAdminFuture().isCancelled(flowRegister.getStartEventId())) {
                    throw ke;
                }
                String taskName = GlobalUtil.getTaskName(serviceTask, flowRegister.getRequestId());
                int iFinal = i;
                ke.log(e -> LOGGER.warn("[{}] Target method execution failed, retry for the {}th time. taskName: {}, exception: {}",
                        ExceptionEnum.SERVICE_INVOKE_ERROR.getExceptionCode(), iFinal, taskName, exception.getMessage(), e));
                if (i >= retry) {
                    Optional<TaskServiceDef> serviceDefOptional = needDemotionSupplier.get();
                    if (!serviceDefOptional.isPresent()) {
                        throw ke;
                    }
                    MethodInvokeTask.MethodInvokePedometer pedometer = new MethodInvokeTask.MethodInvokePedometer(
                            0, needDemotionSupplier, true, invokeProperties.isStrictMode());
                    DemotionInfo demotionInfo = new DemotionInfo();
                    demotionInfo.setRetryTimes(i);
                    demotionInfo.setDemotionNodeId(serviceDefOptional.get().getGetServiceNodeResource().getIdentityId());
                    demotionInfo.setDemotionSuccess(true);
                    try {
                        flowRegister.getMonitorTracking().demotionTaskTracking(serviceTask, demotionInfo);
                        return retryInvokeMethod(pedometer, serviceTask, serviceDefOptional.get(), storyBus, role);
                    } catch (Throwable ex) {
                        demotionInfo.setDemotionSuccess(false);
                        KstryException kex = ExceptionUtil.buildException(ex, ExceptionEnum.DEMOTION_DEFINITION_ERROR, null);
                        kex.log(e -> LOGGER.warn("[{}] Target method execution failed, demotion policy execution failed. taskName: {}, exception: {}",
                                e.getErrorCode(), taskName, ex.getMessage(), e));
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

    private Supplier<Optional<TaskServiceDef>> getNeedDemotionSupplier(Role role, InvokeProperties invokeProperties) {
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
                LOGGER.warn("[{}] {} demotion: {}", ExceptionEnum.DEMOTION_DEFINITION_ERROR.getExceptionCode(),
                        ExceptionEnum.DEMOTION_DEFINITION_ERROR.getDesc(), demotionResource.getIdentityId());
                return resultOptional;
            } catch (Throwable e) {
                LOGGER.warn(e.getMessage(), e);
                return Optional.empty();
            }
        };
    }

    private Object retryInvokeMethod(MethodInvokeTask.MethodInvokePedometer pedometer,
                                     ServiceTask serviceTask, TaskServiceDef taskServiceDef, StoryBus storyBus, Role role) {
        MethodWrapper methodWrapper = taskServiceDef.getMethodWrapper();
        InvokeProperties invokeProperties = methodWrapper.getInvokeProperties();
        Integer timeout;
        if (pedometer.isDemotion()) {
            timeout = Optional.ofNullable(invokeProperties.getTimeout()).filter(t -> t >= 0).orElse(serviceTask.getTimeout());
        } else {
            timeout = Optional.ofNullable(serviceTask.getTimeout()).filter(t -> t >= 0).orElse(invokeProperties.getTimeout());
        }
        if (timeout == null || methodWrapper.isMonoResult()) {
            return super.doInvokeMethod(serviceTask, taskServiceDef, storyBus, role);
        }
        MethodInvokeTask methodInvokeTask = new MethodInvokeTask(pedometer, flowRegister, engineModule, serviceTask, taskServiceDef, storyBus, role);
        InvokeFuture invokeFuture = engineModule.getMethodThreadPool().submitMethodInvokeTask(methodInvokeTask);
        return invokeFuture.invokeMethod(timeout);
    }
}
