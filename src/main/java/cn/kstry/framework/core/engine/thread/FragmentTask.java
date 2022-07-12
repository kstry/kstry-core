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
package cn.kstry.framework.core.engine.thread;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.engine.FlowRegister;
import cn.kstry.framework.core.engine.FlowTaskCore;
import cn.kstry.framework.core.engine.StoryEngineModule;
import cn.kstry.framework.core.engine.future.AdminFuture;
import cn.kstry.framework.core.engine.future.FragmentFuture;
import cn.kstry.framework.core.engine.future.FragmentTaskFuture;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;

/**
 * 流程片段执行任务
 *
 * @author lykan
 */
public class FragmentTask extends FlowTaskCore<AsyncTaskState> implements Task<AsyncTaskState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentTask.class);

    public FragmentTask(StoryEngineModule engineModule, FlowRegister flowRegister, Role role, StoryBus storyBus) {
        super(engineModule, flowRegister, role, storyBus);
    }

    @Override
    public FragmentFuture buildTaskFuture(Future<AsyncTaskState> future) {
        return new FragmentTaskFuture<>(future, getTaskName());
    }

    @Override
    public AsyncTaskState call() {
        AdminFuture adminFuture = null;
        try {
            MDC.put(GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME, flowRegister.getRequestId());
            asyncTaskSwitch.await();
            adminFuture = flowRegister.getAdminFuture();
            AssertUtil.notTrue(adminFuture.isCancelled(flowRegister.getStartEventId()),
                    ExceptionEnum.ASYNC_TASK_INTERRUPTED, "Task interrupted. Story task was interrupted! taskName: {}", getTaskName());
            doExe(this.role, this.storyBus, this.flowRegister);
            return AsyncTaskState.SUCCESS;
        } catch (Throwable e) {
            if (adminFuture != null) {
                adminFuture.errorNotice(e, flowRegister.getStartEventId());
            } else {
                String errorCode = ExceptionEnum.STORY_ERROR.getExceptionCode();
                if (e instanceof KstryException) {
                    errorCode = GlobalUtil.transferNotEmpty(e, KstryException.class).getErrorCode();
                }
                LOGGER.warn("[{}] Task execution fails and exits because an exception is thrown! AdminFuture is null! taskName: {}",
                        errorCode, getTaskName(), e);
            }
            return AsyncTaskState.ERROR;
        } finally {
            MDC.clear();
        }
    }
}
