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

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bus.BasicStoryBus;
import cn.kstry.framework.core.bus.ScopeData;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.engine.facade.TaskResponseBox;
import cn.kstry.framework.core.engine.future.AdminFuture;
import cn.kstry.framework.core.engine.future.FlowFuture;
import cn.kstry.framework.core.engine.future.FlowTaskSubscriber;
import cn.kstry.framework.core.engine.future.MonoFlowFuture;
import cn.kstry.framework.core.engine.thread.FlowTask;
import cn.kstry.framework.core.engine.thread.MonoFlowTask;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.monitor.RecallStory;
import cn.kstry.framework.core.role.BusinessRoleRepository;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.role.ServiceTaskRole;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 执行引擎
 *
 * @author lykan
 */
public class StoryEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoryEngine.class);

    /**
     * StoryEngine 组成模块
     */
    private final StoryEngineModule storyEngineModule;

    /**
     * 角色资源库，可以根据业务ID决策使用哪个角色
     */
    private final BusinessRoleRepository businessRoleRepository;

    public StoryEngine(StoryEngineModule storyEngineModule, BusinessRoleRepository businessRoleRepository) {
        AssertUtil.anyNotNull(businessRoleRepository, storyEngineModule);
        this.businessRoleRepository = businessRoleRepository;
        this.storyEngineModule = storyEngineModule;
    }

    public <T> TaskResponse<T> fire(StoryRequest<T> storyRequest) {
        try {
            MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, GlobalUtil.getOrSetRequestId(storyRequest));
            preProcessing(storyRequest);
            return doFire(storyRequest);
        } catch (KstryException exception) {
            exception.log(e -> LOGGER.warn(e.getMessage(), e));
            TaskResponse<T> errorResponse = TaskResponseBox.buildError(exception.getErrorCode(), exception.getMessage());
            GlobalUtil.transferNotEmpty(errorResponse, TaskResponseBox.class).setResultException(exception);
            return errorResponse;
        } catch (Throwable exception) {
            TaskResponse<T> errorResponse = TaskResponseBox.buildError(ExceptionEnum.SYSTEM_ERROR.getExceptionCode(), ExceptionEnum.SYSTEM_ERROR.getDesc());
            GlobalUtil.transferNotEmpty(errorResponse, TaskResponseBox.class).setResultException(exception);
            LOGGER.warn(exception.getMessage(), exception);
            return errorResponse;
        } finally {
            MDC.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> TaskResponse<T> doFire(StoryRequest<T> storyRequest) throws InterruptedException, TimeoutException {
        Role role = storyRequest.getRole();
        FlowRegister flowRegister = getFlowRegister(storyRequest);
        BasicStoryBus storyBus = getStoryBus(storyRequest, flowRegister, role);
        FlowTask flowTask = new FlowTask(storyEngineModule, flowRegister, role, storyBus);
        AdminFuture adminFuture = storyEngineModule.getTaskThreadPool().submitAdminTask(flowTask);
        try {
            int timeout = storyRequest.getTimeout();
            FlowFuture flowFuture = GlobalUtil.transferNotEmpty(adminFuture.getMainTaskFuture(), FlowFuture.class);
            boolean await = flowFuture.await(timeout, TimeUnit.MILLISECONDS);
            if (!await) {
                throw new TimeoutException(GlobalUtil.format(
                        "[{}] Target story execution timeout! maximum time limit: {}ms", ExceptionEnum.ASYNC_TASK_TIMEOUT.getExceptionCode(), timeout));
            }
            Optional<KstryException> exceptionOptional = adminFuture.getException();
            if (exceptionOptional.isPresent()) {
                throw exceptionOptional.get();
            }
            Object result = null;
            Class<?> returnType = storyRequest.getReturnType();
            if (returnType != null && storyBus.getResult() != null) {
                result = storyBus.getResult();
                AssertUtil.isTrue(ElementParserUtil.isAssignable(returnType, result.getClass()), ExceptionEnum.TYPE_TRANSFER_ERROR,
                        "Engine fire. result type conversion error! expect: {}, actual: {}", returnType.getName(), result.getClass().getName());
            }
            Optional.ofNullable(storyRequest.getRecallStoryHook()).ifPresent(c -> c.accept(new RecallStory(storyBus)));
            return TaskResponseBox.buildSuccess((T) result);
        } catch (Throwable exception) {
            adminFuture.cancel(flowRegister.getStartEventId());
            Optional.ofNullable(storyRequest.getRecallStoryHook()).ifPresent(c -> c.accept(new RecallStory(exception, storyBus)));
            throw exception;
        } finally {
            flowRegister.getMonitorTracking().trackingLog();
        }
    }

    public <T> Mono<T> fireAsync(StoryRequest<T> storyRequest) {
        try {
            MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, GlobalUtil.getOrSetRequestId(storyRequest));
            preProcessing(storyRequest);
            return doFireAsync(storyRequest);
        } finally {
            MDC.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Mono<T> doFireAsync(StoryRequest<T> storyRequest) {
        Role role = storyRequest.getRole();
        FlowRegister flowRegister = getFlowRegister(storyRequest);
        BasicStoryBus storyBus = getStoryBus(storyRequest, flowRegister, role);
        FlowTaskSubscriber flowTaskSubscriber = new FlowTaskSubscriber(
                true, storyRequest.getTimeout(), flowRegister, GlobalUtil.getTaskName(flowRegister.getStartElement(), flowRegister.getRequestId())) {
            @Override
            protected void doErrorHook(Throwable throwable) {
                flowRegister.getMonitorTracking().trackingLog();
                Optional.ofNullable(storyRequest.getRecallStoryHook()).ifPresent(c -> c.accept(new RecallStory(throwable, storyBus)));
            }
        };
        MonoFlowTask monoFlowTask = new MonoFlowTask(storyEngineModule, flowRegister, role, storyBus, flowTaskSubscriber);
        AdminFuture adminFuture = storyEngineModule.getTaskThreadPool().submitAdminTask(monoFlowTask);
        MonoFlowFuture monoFlowFuture = GlobalUtil.transferNotEmpty(adminFuture.getMainTaskFuture(), MonoFlowFuture.class);
        return monoFlowFuture.getMonoFuture().mapNotNull(t -> {
            try {
                if (!Objects.equals(t, AsyncTaskState.SUCCESS)) {
                    return null;
                }
                MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, flowRegister.getRequestId());
                Class<?> returnType = storyRequest.getReturnType();
                Object result = null;
                if (returnType != null && storyBus.getResult() != null) {
                    result = storyBus.getResult();
                    AssertUtil.isTrue(ElementParserUtil.isAssignable(returnType, result.getClass()), ExceptionEnum.TYPE_TRANSFER_ERROR,
                            "Engine async fire. result type conversion error! expect: {}, actual: {}", returnType.getName(), result.getClass().getName());
                }
                Optional.ofNullable(storyRequest.getRecallStoryHook()).ifPresent(c -> c.accept(new RecallStory(storyBus)));
                return result == null ? null : (T) result;
            } catch (Throwable exception) {
                LOGGER.warn(exception.getMessage(), exception);
                Optional.ofNullable(storyRequest.getRecallStoryHook()).ifPresent(c -> c.accept(new RecallStory(exception, storyBus)));
                return null;
            } finally {
                flowRegister.getMonitorTracking().trackingLog();
                MDC.clear();
            }
        });
    }

    private <T> FlowRegister getFlowRegister(StoryRequest<T> storyRequest) {
        String startId = storyRequest.getStartId();
        AssertUtil.notBlank(startId, ExceptionEnum.PARAMS_ERROR, "StartId is not allowed to be empty!");
        Optional<StartEvent> startEventOptional = storyEngineModule.getStartEventContainer().getStartEventById(startId);
        StartEvent startEvent = startEventOptional.orElseThrow(() -> ExceptionUtil
                .buildException(null, ExceptionEnum.PARAMS_ERROR, GlobalUtil.format("StartId did not match a valid StartEvent! startId: {}", startId)));
        return new FlowRegister(startEvent, storyRequest);
    }

    private <T> void preProcessing(StoryRequest<T> storyRequest) {
        String startId = storyRequest.getStartId();
        Role role = storyRequest.getRole();
        if (StringUtils.isNotBlank(startId) && role == null) {
            String businessId = storyRequest.getBusinessId();
            storyRequest.setRole(businessRoleRepository.getRole(businessId, startId).orElse(new ServiceTaskRole()));
        }
    }

    private <T> BasicStoryBus getStoryBus(StoryRequest<T> storyRequest, FlowRegister flowRegister, Role role) {
        String businessId = storyRequest.getBusinessId();
        ScopeData varScopeData = storyRequest.getVarScopeData();
        ScopeData staScopeData = storyRequest.getStaScopeData();
        MonitorTracking monitorTracking = flowRegister.getMonitorTracking();
        return new BasicStoryBus(businessId, role, monitorTracking, storyRequest.getRequest(), varScopeData, staScopeData);
    }
}
