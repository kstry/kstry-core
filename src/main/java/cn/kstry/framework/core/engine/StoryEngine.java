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
import cn.kstry.framework.core.bus.ScopeDataQuery;
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
import cn.kstry.framework.core.engine.thread.hook.ThreadSwitchHook;
import cn.kstry.framework.core.engine.thread.hook.ThreadSwitchHookProcessor;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.exception.BusinessException;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.monitor.RecallStory;
import cn.kstry.framework.core.role.BusinessRoleRepository;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.role.ServiceTaskRole;
import cn.kstry.framework.core.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
        String requestLogIdKey = GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME;
        String oldRequestId = MDC.get(requestLogIdKey);
        try {
            MDC.put(requestLogIdKey, GlobalUtil.getOrSetRequestId(storyRequest));
            ScopeDataQuery scopeDataQuery = getScopeDataQuery(storyRequest);
            initRole(storyRequest, scopeDataQuery);
            return doFire(storyRequest, scopeDataQuery);
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
            GlobalUtil.traceIdClear(oldRequestId, requestLogIdKey);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> TaskResponse<T> doFire(StoryRequest<T> storyRequest, ScopeDataQuery scopeDataQuery) throws InterruptedException, TimeoutException {
        Role role = storyRequest.getRole();
        FlowRegister flowRegister = getFlowRegister(storyRequest, scopeDataQuery);
        BasicStoryBus storyBus = getStoryBus(storyRequest, flowRegister, role);
        FlowTask flowTask = new FlowTask(storyEngineModule, flowRegister, role, storyBus);
        AdminFuture adminFuture = storyEngineModule.getTaskThreadPool().submitAdminTask(flowTask);
        try {
            int timeout = storyRequest.getTimeout();
            FlowFuture flowFuture = GlobalUtil.transferNotEmpty(adminFuture.getMainTaskFuture(), FlowFuture.class);
            boolean await = flowFuture.await(timeout, TimeUnit.MILLISECONDS);
            if (!await) {
                throw new TimeoutException(GlobalUtil.format("[{}] Target story execution timeout! maximum time limit: {}ms", ExceptionEnum.ASYNC_TASK_TIMEOUT.getExceptionCode(), timeout));
            }
            Optional<KstryException> exceptionOptional = adminFuture.getException();
            if (exceptionOptional.isPresent()) {
                throw exceptionOptional.get();
            }
            Object result = null;
            Class<?> returnType = storyRequest.getReturnType();
            if (returnType != null && storyBus.getResult().isPresent()) {
                result = storyBus.getResult().get();
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
        String requestLogIdKey = GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME;
        String oldRequestId = MDC.get(requestLogIdKey);
        try {
            MDC.put(requestLogIdKey, GlobalUtil.getOrSetRequestId(storyRequest));
            ScopeDataQuery scopeDataQuery = getScopeDataQuery(storyRequest);
            initRole(storyRequest, scopeDataQuery);
            return doFireAsync(storyRequest, scopeDataQuery);
        } finally {
            GlobalUtil.traceIdClear(oldRequestId, requestLogIdKey);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Mono<T> doFireAsync(StoryRequest<T> storyRequest, ScopeDataQuery scopeDataQuery) {
        Role role = storyRequest.getRole();
        FlowRegister flowRegister = getFlowRegister(storyRequest, scopeDataQuery);
        BasicStoryBus storyBus = getStoryBus(storyRequest, flowRegister, role);
        ThreadSwitchHookProcessor threadSwitchHookProcessor = storyEngineModule.getThreadSwitchHookProcessor();
        Map<ThreadSwitchHook<Object>, Object> hookData = threadSwitchHookProcessor.getPreviousData(scopeDataQuery);
        FlowTaskSubscriber flowTaskSubscriber = new FlowTaskSubscriber(() -> threadSwitchHookProcessor.usePreviousData(hookData, storyBus.getScopeDataOperator()),
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
        return monoFlowFuture.getMonoFuture().handle((t, sink) -> {
            String requestLogIdKey = GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME;
            String oldRequestId = MDC.get(requestLogIdKey);
            try {
                MDC.put(requestLogIdKey, flowRegister.getRequestId());
                AssertUtil.isTrue(t == AsyncTaskState.SUCCESS, ExceptionEnum.SYSTEM_ERROR);
                Class<?> returnType = storyRequest.getReturnType();
                Object result = null;
                if (returnType != null && storyBus.getResult().isPresent()) {
                    result = storyBus.getResult().get();
                    AssertUtil.isTrue(ElementParserUtil.isAssignable(returnType, result.getClass()), ExceptionEnum.TYPE_TRANSFER_ERROR,
                            "Engine async fire. result type conversion error! expect: {}, actual: {}", returnType.getName(), result.getClass().getName());
                }
                Optional.ofNullable(storyRequest.getRecallStoryHook()).ifPresent(c -> c.accept(new RecallStory(storyBus)));
                if (result == null) {
                    return;
                }
                sink.next((T) result);
            } catch (Throwable exception) {
                LOGGER.warn(exception.getMessage(), exception);
                Optional.ofNullable(storyRequest.getRecallStoryHook()).ifPresent(c -> c.accept(new RecallStory(exception, storyBus)));
                sink.error(exception);
            } finally {
                flowRegister.getMonitorTracking().trackingLog();
                GlobalUtil.traceIdClear(oldRequestId, requestLogIdKey);
            }
        });
    }

    private <T> FlowRegister getFlowRegister(StoryRequest<T> storyRequest, ScopeDataQuery scopeDataQuery) {
        String startId = storyRequest.getStartId();
        AssertUtil.notBlank(startId, ExceptionEnum.PARAMS_ERROR, "StartId is not allowed to be empty!");
        Optional<StartEvent> startEventOptional = storyEngineModule.getStartEventContainer().getStartEventById(scopeDataQuery);
        StartEvent startEvent = startEventOptional.orElseThrow(() -> ExceptionUtil
                .buildException(null, ExceptionEnum.PARAMS_ERROR, GlobalUtil.format("StartId did not match a valid StartEvent! startId: {}", startId)));
        return new FlowRegister(startEvent, storyRequest);
    }

    private <T> void initRole(StoryRequest<T> storyRequest, ScopeDataQuery scopeDataQuery) {
        if (StringUtils.isBlank(storyRequest.getStartId()) || storyRequest.getRole() != null) {
            return;
        }
        storyRequest.setRole(businessRoleRepository.getRole(storyRequest, scopeDataQuery).orElse(new ServiceTaskRole()));
    }

    private <T> BasicStoryBus getStoryBus(StoryRequest<T> storyRequest, FlowRegister flowRegister, Role role) {
        String businessId = storyRequest.getBusinessId();
        ScopeData varScopeData = storyRequest.getVarScopeData();
        ScopeData staScopeData = storyRequest.getStaScopeData();
        MonitorTracking monitorTracking = flowRegister.getMonitorTracking();
        return new BasicStoryBus(storyRequest.getRequestId(), storyRequest.getStartId(), businessId, role, monitorTracking, storyRequest.getRequest(), varScopeData, staScopeData);
    }

    @SuppressWarnings("unchecked")
    private ScopeDataQuery getScopeDataQuery(StoryRequest<?> storyRequest) {

        return new ScopeDataQuery() {

            @Override
            public <T> T getReqScope() {
                return (T) storyRequest.getRequest();
            }

            @Override
            public <T extends ScopeData> T getStaScope() {
                return (T) storyRequest.getStaScopeData();
            }

            @Override
            public <T extends ScopeData> T getVarScope() {
                return (T) storyRequest.getVarScopeData();
            }

            @Override
            public <T> Optional<T> getResult() {
                throw new BusinessException(ExceptionEnum.BUSINESS_INVOKE_ERROR.getExceptionCode(), "Method is not allowed to be called!");
            }

            @Override
            public String getRequestId() {
                return storyRequest.getRequestId();
            }

            @Override
            public String getStartId() {
                return storyRequest.getStartId();
            }

            @Override
            public Optional<String> getBusinessId() {
                return Optional.ofNullable(storyRequest.getBusinessId()).filter(StringUtils::isNotBlank);
            }

            @Override
            public <T> Optional<T> getReqData(String name) {
                T reqScope = getReqScope();
                if (reqScope == null) {
                    return Optional.empty();
                }
                return PropertyUtil.getProperty(reqScope, name).map(obj -> (T) obj);
            }

            @Override
            public <T> Optional<T> getStaData(String name) {
                T staScope = getStaScope();
                if (staScope == null) {
                    return Optional.empty();
                }
                return PropertyUtil.getProperty(staScope, name).map(obj -> (T) obj);
            }

            @Override
            public <T> Optional<T> getVarData(String name) {
                T varScope = getVarScope();
                if (varScope == null) {
                    return Optional.empty();
                }
                return PropertyUtil.getProperty(varScope, name).map(obj -> (T) obj);
            }

            @Override
            public <T> Optional<T> getData(String expression) {
                if (!ElementParserUtil.isValidDataExpression(expression)) {
                    return Optional.empty();
                }

                String[] expArr = expression.split("\\.", 2);
                Optional<ScopeTypeEnum> ScopeTypeOptional = ScopeTypeEnum.of(expArr[0]);
                if (ScopeTypeOptional.orElse(null) == ScopeTypeEnum.RESULT) {
                    return getResult();
                }

                String key = (expArr.length == 2) ? expArr[1] : null;
                if (StringUtils.isBlank(key)) {
                    return Optional.empty();
                }
                return ScopeTypeOptional.flatMap(scope -> {
                    if (scope == ScopeTypeEnum.REQUEST) {
                        return getReqData(key);
                    } else if (scope == ScopeTypeEnum.STABLE) {
                        return getStaData(key);
                    } else if (scope == ScopeTypeEnum.VARIABLE) {
                        return getVarData(key);
                    }
                    return Optional.empty();
                });
            }

            @Override
            public Optional<String> getTaskProperty() {
                throw new BusinessException(ExceptionEnum.BUSINESS_INVOKE_ERROR.getExceptionCode(), "Method is not allowed to be called!");
            }

            @Override
            public <T> Optional<T> iterDataItem() {
                throw new BusinessException(ExceptionEnum.BUSINESS_INVOKE_ERROR.getExceptionCode(), "Method is not allowed to be called!");
            }

            @Override
            public ReentrantReadWriteLock.ReadLock readLock() {
                throw new BusinessException(ExceptionEnum.BUSINESS_INVOKE_ERROR.getExceptionCode(), "Method is not allowed to be called!");
            }
        };
    }
}
