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

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.container.component.MethodWrapper;
import cn.kstry.framework.core.container.component.ParamInjectDef;
import cn.kstry.framework.core.container.component.TaskServiceDef;
import cn.kstry.framework.core.container.task.impl.TaskComponentProxy;
import cn.kstry.framework.core.engine.thread.Task;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ProxyUtil;
import cn.kstry.framework.core.util.TaskServiceUtil;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * 方法调用核心
 *
 * @author lykan
 */
public abstract class BasicTaskCore<T> implements Task<T> {

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

    public BasicTaskCore(StoryEngineModule engineModule, FlowRegister flowRegister, StoryBus storyBus, Role role, String taskName) {
        AssertUtil.notBlank(taskName);
        AssertUtil.anyNotNull(engineModule, flowRegister, storyBus, role);
        this.engineModule = engineModule;
        this.flowRegister = flowRegister;
        this.storyBus = storyBus;
        this.role = role;
        this.taskName = taskName;
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

    protected Object doInvokeMethod(ServiceTask serviceTask, TaskServiceDef taskServiceDef, StoryBus storyBus, Role role) {
        MethodWrapper methodWrapper = taskServiceDef.getMethodWrapper();
        TaskComponentProxy targetProxy = taskServiceDef.getTaskComponentTarget();
        List<ParamInjectDef> paramInjectDefs = methodWrapper.getParamInjectDefs();
        if (CollectionUtils.isEmpty(paramInjectDefs)) {
            return ProxyUtil.invokeMethod(storyBus, methodWrapper, targetProxy.getTarget());
        }
        Function<ParamInjectDef, Object> paramInitStrategy = engineModule.getParamInitStrategy();
        return ProxyUtil.invokeMethod(storyBus, methodWrapper, targetProxy.getTarget(),
                () -> TaskServiceUtil.getTaskParams(serviceTask, storyBus, role, targetProxy, paramInjectDefs, paramInitStrategy));
    }
}
