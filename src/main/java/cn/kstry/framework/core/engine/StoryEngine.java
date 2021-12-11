/*
 *
 *  *  Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  *  <p>
 *  *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *  <p>
 *  * https://www.gnu.org/licenses/lgpl.html
 *  *  <p>
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
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.container.MethodWrapper;
import cn.kstry.framework.core.container.StartEventContainer;
import cn.kstry.framework.core.container.TaskContainer;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.engine.facade.TaskResponseBox;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.role.BusinessRoleRepository;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author lykan
 */
public class StoryEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoryEngine.class);

    private final StartEventContainer startEventContainer;

    private final ThreadPoolExecutor asyncThreadPool;

    private final TaskContainer taskContainer;

    private final BusinessRoleRepository businessRoleRepository;

    private final Function<MethodWrapper.ParamInjectDef, Object> paramInitStrategy;

    public StoryEngine(BusinessRoleRepository businessRoleRepository, StartEventContainer startEventContainer,
                       ThreadPoolExecutor asyncThreadPool, TaskContainer taskContainer, Function<MethodWrapper.ParamInjectDef, Object> paramInitStrategy) {
        AssertUtil.notNull(businessRoleRepository);
        AssertUtil.notNull(taskContainer);
        AssertUtil.notNull(asyncThreadPool);
        AssertUtil.notNull(paramInitStrategy);
        AssertUtil.notNull(startEventContainer);
        this.businessRoleRepository = businessRoleRepository;
        this.asyncThreadPool = asyncThreadPool;
        this.taskContainer = taskContainer;
        this.paramInitStrategy = paramInitStrategy;
        this.startEventContainer = startEventContainer;
    }

    public <T> TaskResponse<T> fire(StoryRequest<T> storyRequest) {
        try {
            MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, GlobalUtil.getOrSetRequestId(storyRequest));
            preProcessing(storyRequest);
            return doFire(storyRequest);
        } catch (KstryException exception) {
            TaskResponse<T> errorResponse = TaskResponseBox.buildError(exception.getErrorCode(), exception.getMessage());
            GlobalUtil.transferNotEmpty(errorResponse, TaskResponseBox.class).setResultException(exception);
            LOGGER.warn(exception.getMessage(), exception);
            return errorResponse;
        } catch (Exception exception) {
            TaskResponse<T> errorResponse = TaskResponseBox.buildError(ExceptionEnum.SYSTEM_ERROR.getExceptionCode(), ExceptionEnum.SYSTEM_ERROR.getDesc());
            GlobalUtil.transferNotEmpty(errorResponse, TaskResponseBox.class).setResultException(exception);
            LOGGER.warn(exception.getMessage(), exception);
            return errorResponse;
        } finally {
            MDC.clear();
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
    public <T> Mono<T> doFireAsync(StoryRequest<T> storyRequest) {
        FlowRegister flowRegister = getFlowRegister(storyRequest);
        AsyncTaskCell asyncTaskCell = flowRegister.getAsyncTaskCell();
        CompletableFuture<AsyncTaskState> future = asyncTaskCell.initResultFuture();

        StoryBus storyBus = submitTaskGetBus(storyRequest, flowRegister);
        int timeout = Optional.ofNullable(storyRequest.getTimeout()).orElse(GlobalProperties.ASYNC_TASK_DEFAULT_TIMEOUT);
        return Mono.fromFuture(future.whenComplete((result, exp) -> {
            try {
                MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, flowRegister.getRequestId());
                if (exp != null && !asyncTaskCell.isCancelled()) {
                    asyncTaskCell.cancel();
                    LOGGER.warn(exp.getMessage(), exp);
                    flowRegister.getMonitorTracking().trackingLog();
                    Optional.ofNullable(storyRequest.getStoryBusHook()).ifPresent(c -> c.accept(storyBus));
                }
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            } finally {
                MDC.clear();
            }
        }).thenCompose(t -> {
            try {
                MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, flowRegister.getRequestId());
                flowRegister.getMonitorTracking().trackingLog();
                Class<?> returnType = storyRequest.getReturnType();
                if (returnType == null || storyBus.getResult() == null) {
                    Optional.ofNullable(storyRequest.getStoryBusHook()).ifPresent(c -> c.accept(storyBus));
                    return CompletableFuture.completedFuture(null);
                }
                Object result = storyBus.getResult();
                AssertUtil.isTrue(ElementParserUtil.isAssignable(returnType, result.getClass()), ExceptionEnum.TYPE_TRANSFER_ERROR,
                        "Type conversion error! expect: {}, actual: {}", returnType.getName(), result.getClass().getName());
                Optional.ofNullable(storyRequest.getStoryBusHook()).ifPresent(c -> c.accept(storyBus));
                return CompletableFuture.completedFuture((T) result);
            } catch (Exception exception) {
                LOGGER.warn(exception.getMessage(), exception);
                CompletableFuture<T> completableFuture = new CompletableFuture<>();
                completableFuture.completeExceptionally(exception);
                return completableFuture;
            } finally {
                MDC.clear();
            }
        })).timeout(Duration.ofMillis(timeout), Mono.fromSupplier(() -> {
            try {
                MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, flowRegister.getRequestId());
                asyncTaskCell.cancel();
                flowRegister.getMonitorTracking().trackingLog();
                T t = Optional.ofNullable(storyRequest.getMonoTimeoutFallback()).map(Supplier::get).orElse(null);
                Optional.ofNullable(storyRequest.getStoryBusHook()).ifPresent(c -> c.accept(storyBus));
                return t;
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
                throw e;
            } finally {
                MDC.clear();
            }
        }));
    }

    @SuppressWarnings("unchecked")
    private <T> TaskResponse<T> doFire(StoryRequest<T> storyRequest) {
        FlowRegister flowRegister = getFlowRegister(storyRequest);
        StoryBus storyBus = submitTaskGetBus(storyRequest, flowRegister);
        try {
            AsyncTaskCell asyncTaskCell1 = flowRegister.getAsyncTaskCell();
            long timeout = Optional.ofNullable(storyRequest.getTimeout()).orElse(GlobalProperties.ASYNC_TASK_DEFAULT_TIMEOUT);
            Optional<KstryException> expOptional = asyncTaskCell1.get(timeout, TimeUnit.MILLISECONDS);
            if (expOptional.isPresent()) {
                throw expOptional.get();
            }

            Class<?> returnType = storyRequest.getReturnType();
            if (returnType != null && storyBus.getResult() != null) {
                Object result = storyBus.getResult();
                AssertUtil.isTrue(ElementParserUtil.isAssignable(returnType, result.getClass()), ExceptionEnum.TYPE_TRANSFER_ERROR);
                return TaskResponseBox.buildSuccess((T) result);
            }
            return TaskResponseBox.buildSuccess(null);
        } finally {
            flowRegister.getMonitorTracking().trackingLog();
            Optional.ofNullable(storyRequest.getStoryBusHook()).ifPresent(c -> c.accept(storyBus));
        }
    }

    /**
     * 注销
     */
    public void destroy() {
        if (asyncThreadPool != null) {
            LOGGER.info("begin shutdown time slot thread pool! active count: {}", asyncThreadPool.getActiveCount());
            asyncThreadPool.shutdown();
            try {
                TimeUnit.MILLISECONDS.sleep(GlobalProperties.ENGINE_SHUTDOWN_SLEEP_SECONDS);
            } catch (Exception e) {
                LOGGER.warn("time slot thread pool close task are interrupted on shutdown!", e);
            }
            if (asyncThreadPool.isShutdown() && asyncThreadPool.getActiveCount() == 0) {
                LOGGER.info("[shutdown] interrupting tasks in the thread pool success! thread pool close success!");
                return;
            } else {
                LOGGER.info("interrupting tasks in the thread pool that have not yet finished! begin shutdownNow! active count: {}",
                        asyncThreadPool.getActiveCount());
                asyncThreadPool.shutdownNow();
            }
            try {
                TimeUnit.MILLISECONDS.sleep(GlobalProperties.ENGINE_SHUTDOWN_NOW_SLEEP_SECONDS);
            } catch (Exception e) {
                LOGGER.warn("time slot thread pool close task are interrupted on shutdown!", e);
            }
            if (asyncThreadPool.isShutdown() && asyncThreadPool.getActiveCount() == 0) {
                LOGGER.info("[shutdownNow] interrupting tasks in the thread pool success! thread pool close success!");
            } else {
                LOGGER.error("[shutdownNow] interrupting tasks in the thread pool error! thread pool close error!");
            }
        }
    }

    private <T> StoryBus submitTaskGetBus(StoryRequest<T> storyRequest, FlowRegister flowRegister) {
        // build story bus
        Role role = storyRequest.getRole();
        String businessId = storyRequest.getBusinessId();
        ScopeData varScopeData = storyRequest.getVarScopeData();
        ScopeData staScopeData = storyRequest.getStaScopeData();
        MonitorTracking monitorTracking = flowRegister.getMonitorTracking();
        BasicStoryBus storyBus = new BasicStoryBus(businessId, role, monitorTracking, storyRequest.getRequest(), varScopeData, staScopeData);

        // submit task
        AsyncTaskForkJoin asyncForkJoin = new AsyncTaskForkJoin();
        asyncForkJoin.setRole(role);
        asyncForkJoin.setStoryBus(storyBus);
        asyncForkJoin.setTaskContainer(taskContainer);
        asyncForkJoin.setParamInitStrategy(paramInitStrategy);
        asyncForkJoin.setAsyncThreadPool(asyncThreadPool);
        asyncForkJoin.submitTask(flowRegister);
        return storyBus;
    }

    private <T> FlowRegister getFlowRegister(StoryRequest<T> storyRequest) {
        String startId = storyRequest.getStartId();
        AssertUtil.notBlank(startId, ExceptionEnum.PARAMS_ERROR, "StartId is not allowed to be empty!");
        Optional<StartEvent> startEventOptional = startEventContainer.getStartEventById(startId);
        StartEvent startEvent = startEventOptional.orElseThrow(() -> KstryException
                .buildException(null, ExceptionEnum.PARAMS_ERROR, GlobalUtil.format("StartId did not match a valid StartEvent! startId: {}", startId)));
        return new FlowRegister(startEvent, storyRequest);
    }

    private <T> void preProcessing(StoryRequest<T> storyRequest) {
        String startId = storyRequest.getStartId();
        Role role = storyRequest.getRole();
        if (StringUtils.isNotBlank(startId) && role == null) {
            String businessId = storyRequest.getBusinessId();
            businessRoleRepository.getRole(businessId, startId).ifPresent(storyRequest::setRole);
        }
    }
}
