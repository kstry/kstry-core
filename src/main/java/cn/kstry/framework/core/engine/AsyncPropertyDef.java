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

import cn.kstry.framework.core.bus.BasicStoryBus;
import cn.kstry.framework.core.container.MethodWrapper;
import cn.kstry.framework.core.container.TaskContainer;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

/**
 *
 * @author lykan
 */
public class AsyncPropertyDef {

    private BasicStoryBus storyBus;

    private Role role;

    private TaskContainer taskContainer;

    private Function<MethodWrapper.ParamInjectDef, Object> paramInitStrategy;

    private ThreadPoolExecutor asyncThreadPool;

    public BasicStoryBus getStoryBus() {
        return storyBus;
    }

    public Role getRole() {
        return role;
    }

    public void setStoryBus(BasicStoryBus storyBus) {
        AssertUtil.notNull(storyBus);
        this.storyBus = storyBus;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public TaskContainer getTaskContainer() {
        return taskContainer;
    }

    public void setTaskContainer(TaskContainer taskContainer) {
        this.taskContainer = taskContainer;
    }

    public Function<MethodWrapper.ParamInjectDef, Object> getParamInitStrategy() {
        return paramInitStrategy;
    }

    public void setParamInitStrategy(Function<MethodWrapper.ParamInjectDef, Object> paramInitStrategy) {
        this.paramInitStrategy = paramInitStrategy;
    }

    public ThreadPoolExecutor getAsyncThreadPool() {
        return asyncThreadPool;
    }

    public void setAsyncThreadPool(ThreadPoolExecutor asyncThreadPool) {
        this.asyncThreadPool = asyncThreadPool;
    }
}
