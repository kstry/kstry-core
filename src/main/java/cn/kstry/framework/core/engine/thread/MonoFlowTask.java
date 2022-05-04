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

import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.engine.FlowRegister;
import cn.kstry.framework.core.engine.StoryEngineModule;
import cn.kstry.framework.core.engine.future.FlowTaskSubscriber;
import cn.kstry.framework.core.engine.future.MonoFlowFuture;
import cn.kstry.framework.core.engine.future.MonoFlowTaskFuture;
import cn.kstry.framework.core.enums.AsyncTaskState;
import cn.kstry.framework.core.role.Role;

import java.util.concurrent.Future;

/**
 * 异步化的主流程任务
 *
 * @author lykan
 */
public class MonoFlowTask extends MainFlowTask {

    private final FlowTaskSubscriber flowTaskSubscriber;

    public MonoFlowTask(StoryEngineModule engineModule, FlowRegister flowRegister, Role role, StoryBus storyBus, FlowTaskSubscriber flowTaskSubscriber) {
        super(engineModule, flowRegister, role, storyBus);
        this.flowTaskSubscriber = flowTaskSubscriber;
    }

    @Override
    public MonoFlowFuture buildTaskFuture(Future<AsyncTaskState> future) {
        return new MonoFlowTaskFuture(endTaskPedometer, future, getTaskName(), flowTaskSubscriber);
    }
}
