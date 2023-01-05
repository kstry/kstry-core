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
package cn.kstry.framework.core.engine.thread;

import cn.kstry.framework.core.bpmn.EndEvent;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.engine.FlowRegister;
import cn.kstry.framework.core.engine.StoryEngineModule;
import cn.kstry.framework.core.engine.future.AdminFuture;
import cn.kstry.framework.core.engine.future.MainTaskFuture;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;

import java.util.concurrent.Future;

/**
 * 主流程任务
 *
 * @author lykan
 */
public class MainFlowTask extends FragmentTask {

    /**
     * 结束任务计步器
     */
    protected final EndTaskPedometer endTaskPedometer;

    public MainFlowTask(StoryEngineModule engineModule, FlowRegister flowRegister, Role role, StoryBus storyBus) {
        super(engineModule, flowRegister, role, storyBus);
        StartEvent startEvent = GlobalUtil.transferNotEmpty(flowRegister.getStartElement(), StartEvent.class);
        EndEvent endEvent = GlobalUtil.notNull(startEvent.getEndEvent());
        this.endTaskPedometer = new EndTaskPedometer(startEvent.getId(), endEvent.comingList(), getTaskName());
    }

    @Override
    public MainTaskFuture buildTaskFuture(Future<AsyncTaskState> future) {
        throw ExceptionUtil.buildException(null, ExceptionEnum.ASYNC_TASK_ERROR, null);
    }

    /**
     * 设置 TaskFuture 管理类
     *
     * @param adminFuture TaskFuture 管理类
     */
    public void setAdminFuture(AdminFuture adminFuture) {
        AssertUtil.isNull(flowRegister.getAdminFuture());
        flowRegister.setAdminFuture(adminFuture);
    }
}
