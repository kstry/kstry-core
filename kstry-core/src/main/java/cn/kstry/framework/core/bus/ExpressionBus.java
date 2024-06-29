/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.bus;

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.component.expression.ConditionExpression;
import cn.kstry.framework.core.engine.thread.InvokeMethodThreadLocal;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;

import java.util.Map;

/**
 * @author lykan
 */
public class ExpressionBus {

    private final StoryBus storyBus;

    public ExpressionBus(StoryBus storyBus) {
        this.storyBus = storyBus;
    }

    /**
     * 获取 Request 域 数据
     *
     * @return data
     */
    public Object getReq() {
        return storyBus.getReq();
    }

    /**
     * 获取 执行结果
     *
     * @return ReturnResult
     */
    public Object getRes() {
        return storyBus.getResult().orElse(null);
    }

    /**
     * 获取 Var 域变量
     *
     * @return var scope data
     */
    public Object getVar() {
        return storyBus.getVar();
    }

    /**
     * 获取 Sta 域变量
     *
     * @return sta scope data
     */
    public Object getSta() {
        return storyBus.getSta();
    }

    /**
     * 获取业务 ID
     *
     * @return 业务 ID
     */
    public String getBusinessId() {
        return storyBus.getBusinessId();
    }

    /**
     * 获取开始事件ID
     *
     * @return 开始事件ID
     */
    public String getStartId() {
        return storyBus.getStartId();
    }

    /**
     * 获取 ability
     *
     * @return ability
     */
    public String getAbility() {
        return storyBus.getScopeDataOperator().getServiceNodeResource().map(ServiceNodeResource::getAbilityName).orElse(null);
    }

    /**
     * 获取 instruct
     *
     * @return instruct
     */
    public String getInstruct() {
        return InvokeMethodThreadLocal.getServiceTask().map(ServiceTask::getTaskInstruct).orElse(null);
    }

    /**
     * 获取 instructContent
     *
     * @return instructContent
     */
    public String getInstructContent() {
        return InvokeMethodThreadLocal.getServiceTask().map(ServiceTask::getTaskInstructContent).orElse(null);
    }

    /**
     * 获取 property
     *
     * @return property
     */
    public String getProperty() {
        return InvokeMethodThreadLocal.getServiceTask().map(ServiceTask::getTaskProperty).orElse(null);
    }

    /**
     * 获取 taskParams
     *
     * @return taskParams
     */
    public Map<String, Object> getTaskParams() {
        return InvokeMethodThreadLocal.getServiceTask().map(ServiceTask::getTaskParams).orElse(null);
    }

    /**
     * 获取 taskService
     *
     * @return taskService
     */
    public String getTaskService() {
        return InvokeMethodThreadLocal.getServiceTask().map(ServiceTask::getTaskService).orElse(null);
    }

    /**
     * 获取 taskComponent
     *
     * @return taskComponent
     */
    public String getTaskComponent() {
        return InvokeMethodThreadLocal.getServiceTask().map(ServiceTask::getTaskComponent).orElse(null);
    }

    /**
     * 获取 taskDemotion
     *
     * @return taskDemotion
     */
    public String getTaskDemotion() {
        return InvokeMethodThreadLocal.getServiceTask().map(ServiceTask::getTaskDemotion).map(ServiceNodeResource::getIdentityId).orElse(null);
    }

    /**
     * 获取 expression
     *
     * @return expression
     */
    public String getExpression() {
        return InvokeMethodThreadLocal.getServiceTask().flatMap(ServiceTask::getConditionExpression).map(ConditionExpression::getPlainExpression).orElse(null);
    }
}
