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
package cn.kstry.framework.core.engine.thread;

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.container.component.TaskServiceDef;
import cn.kstry.framework.core.engine.BasicTaskCore;
import cn.kstry.framework.core.engine.FlowRegister;
import cn.kstry.framework.core.engine.StoryEngineModule;
import cn.kstry.framework.core.engine.future.AdminFuture;
import cn.kstry.framework.core.engine.future.InvokeFuture;
import cn.kstry.framework.core.engine.future.MethodInvokeFuture;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * 方法调用任务
 *
 * @author lykan
 */
public class MethodInvokeTask extends BasicTaskCore<Object> implements Task<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodInvokeTask.class);

    /**
     * ServiceTask
     */
    private final ServiceTask serviceTask;

    /**
     * 服务节点定义
     */
    private final TaskServiceDef taskServiceDef;

    /**
     * 程序执行计步器
     */
    private final MethodInvokePedometer methodInvokePedometer;

    public MethodInvokeTask(MethodInvokePedometer methodInvokePedometer, FlowRegister flowRegister,
                            StoryEngineModule engineModule, ServiceTask serviceTask, TaskServiceDef taskServiceDef, StoryBus storyBus, Role role) {
        super(engineModule, flowRegister, storyBus, role, GlobalUtil.getTaskName(serviceTask, flowRegister.getRequestId()));
        AssertUtil.anyNotNull(flowRegister.getAdminFuture(), serviceTask, taskServiceDef, storyBus, role);
        this.serviceTask = serviceTask;
        this.taskServiceDef = taskServiceDef;
        this.methodInvokePedometer = methodInvokePedometer;
    }

    @Override
    public InvokeFuture buildTaskFuture(Future<Object> future) {
        return new MethodInvokeFuture(future, getTaskName());
    }

    @Override
    public Object call() throws Exception {
        AdminFuture adminFuture = flowRegister.getAdminFuture();
        try {
            MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, flowRegister.getRequestId());
            asyncTaskSwitch.await();
            AssertUtil.notTrue(adminFuture.isCancelled(flowRegister.getStartEventId()), ExceptionEnum.ASYNC_TASK_INTERRUPTED,
                    "Task interrupted. Method invoke task was interrupted! taskName: {}", getTaskName());
            return doInvokeMethod(serviceTask, taskServiceDef, storyBus, role);
        } catch (Throwable e) {
            if (adminFuture.isCancelled(flowRegister.getStartEventId())) {
                adminFuture.errorNotice(e, flowRegister.getStartEventId());
                throw e;
            }
            // 可重试
            if (methodInvokePedometer.remainRetry > 0) {
                throw e;
            }
            // 可降级
            if (methodInvokePedometer.needDemotionSupplier.get().isPresent() && !methodInvokePedometer.isDemotion) {
                throw e;
            }
            // 非严格模式
            if (!(serviceTask.strictMode() && methodInvokePedometer.strictMode)) {
                LOGGER.warn("[{}] Target method execution failure, error is ignored in non-strict mode. exception: {}",
                        ExceptionEnum.SERVICE_INVOKE_ERROR.getExceptionCode(), e.getMessage(), e);
                throw e;
            }
            adminFuture.errorNotice(e, flowRegister.getStartEventId());
            throw e;
        } finally {
            MDC.clear();
        }
    }

    public static class MethodInvokePedometer {

        private final int remainRetry;

        private final Supplier<Optional<TaskServiceDef>> needDemotionSupplier;

        private final boolean isDemotion;

        private final boolean strictMode;

        public MethodInvokePedometer(int remainRetry, Supplier<Optional<TaskServiceDef>> needDemotionSupplier,
                                     boolean isDemotion, boolean strictMode) {
            this.remainRetry = remainRetry;
            this.needDemotionSupplier = needDemotionSupplier;
            this.isDemotion = isDemotion;
            this.strictMode = strictMode;
        }

        public boolean isDemotion() {
            return isDemotion;
        }
    }
}
