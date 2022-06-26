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
package cn.kstry.framework.core.container.component;

import cn.kstry.framework.core.container.task.impl.TaskComponentProxy;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
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
    private final TaskComponentProxy taskComponentTarget;

    /**
     * 获取服务节点资源
     */
    private final ServiceNodeResource getServiceNodeResource;

    /**
     * 降级节点
     */
    private boolean demotionNode;

    public TaskServiceDef(@Nonnull ServiceNodeResource getServiceNodeResource, @Nonnull String name,
                          @Nonnull MethodWrapper methodWrapper, @Nonnull TaskComponentProxy taskComponentTarget) {
        AssertUtil.notBlank(name);
        AssertUtil.anyNotNull(getServiceNodeResource, methodWrapper, taskComponentTarget);
        this.name = name;
        this.methodWrapper = methodWrapper;
        this.taskComponentTarget = taskComponentTarget;
        this.getServiceNodeResource = getServiceNodeResource;
        this.demotionNode = false;
    }

    public String getName() {
        return name;
    }

    public MethodWrapper getMethodWrapper() {
        return methodWrapper;
    }

    public TaskComponentProxy getTaskComponentTarget() {
        return taskComponentTarget;
    }

    public ServiceNodeResource getGetServiceNodeResource() {
        return getServiceNodeResource;
    }

    public boolean isDemotionNode() {
        return demotionNode;
    }

    public void setDemotionNode(boolean demotionNode) {
        this.demotionNode = demotionNode;
    }
}
