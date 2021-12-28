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
package cn.kstry.framework.core.task.facade;

import cn.kstry.framework.core.container.MethodWrapper;
import cn.kstry.framework.core.task.impl.TaskComponentRegisterProxy;
import cn.kstry.framework.core.util.AssertUtil;

import javax.annotation.Nonnull;

/**
 * @author lykan
 */
public class TaskServiceDef {

    /**
     * 服务名称
     */
    private final String name;

    /**
     * 执行方法
     */
    private final MethodWrapper methodWrapper;

    /**
     * 目标对象
     */
    private final TaskComponentRegisterProxy taskComponentTarget;

    public TaskServiceDef(@Nonnull String name, @Nonnull MethodWrapper methodWrapper, @Nonnull TaskComponentRegisterProxy taskComponentTarget) {
        AssertUtil.notBlank(name);
        AssertUtil.notNull(methodWrapper);
        AssertUtil.notNull(taskComponentTarget);
        this.name = name;
        this.methodWrapper = methodWrapper;
        this.taskComponentTarget = taskComponentTarget;
    }

    public String getName() {
        return name;
    }

    public MethodWrapper getMethodWrapper() {
        return methodWrapper;
    }

    public TaskComponentRegisterProxy getTaskComponentTarget() {
        return taskComponentTarget;
    }
}
