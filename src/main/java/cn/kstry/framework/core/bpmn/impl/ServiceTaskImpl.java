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
package cn.kstry.framework.core.bpmn.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.engine.facade.CustomRoleInfo;

/**
 * ServiceTaskImpl
 */
public class ServiceTaskImpl extends TaskImpl implements ServiceTask {

    /**
     * 读取配置文件，获取 taskComponent
     */
    private String taskComponent;

    /**
     * 读取配置文件，获取 taskService
     */
    private String taskService;

    /**
     * 读取配置文件，获取 customRole
     *
     * 格式：component@service 指定变更 Role 权限的 Service
     */
    private CustomRoleInfo customRoleInfo;

    /**
     * 未匹配到 TaskService 时，是否可以忽略
     * 默认：false 忽略未匹配到的 TaskService 时，抛出异常
     */
    private Boolean allowAbsent;

    /**
     * 严格模式，控制任务节点执行失败后是否要抛出异常，默认是严格模式，节点抛出异常后结束整个 Story 流程
     * 关闭严格模式后，节点抛出异常时忽略该节点继续向下执行
     */
    private Boolean strictMode;

    @Override
    public String getTaskComponent() {
        return taskComponent;
    }

    /**
     * 设置 taskComponent
     *
     * @param taskComponent taskComponent
     */
    public void setTaskComponent(String taskComponent) {
        this.taskComponent = taskComponent;
    }

    @Override
    public String getTaskService() {
        return taskService;
    }

    @Override
    public boolean allowAbsent() {
        return BooleanUtils.isTrue(allowAbsent);
    }

    /**
     * 设置 allowAbsent
     *
     * @param allowAbsent allowAbsent
     */
    public void setAllowAbsent(String allowAbsent) {
        if (StringUtils.isBlank(allowAbsent)) {
            return;
        }
        this.allowAbsent = BooleanUtils.toBooleanObject(allowAbsent.trim());
    }

    @Override
    public boolean strictMode() {
        return BooleanUtils.isNotFalse(strictMode);
    }

    public void setStrictMode(String strictMode) {
        if (StringUtils.isBlank(strictMode)) {
            return;
        }
        this.strictMode = BooleanUtils.toBooleanObject(strictMode.trim());
    }

    @Override
    public CustomRoleInfo getCustomRoleInfo() {
        return customRoleInfo;
    }

    @Override
    public boolean validTask() {
        return !StringUtils.isAnyBlank(taskComponent, taskService);
    }

    /**
     * 设置角色自定义组件
     *
     * @param customRoleInfo 角色自定义组件
     */
    public void setCustomRoleInfo(CustomRoleInfo customRoleInfo) {
        this.customRoleInfo = customRoleInfo;
    }

    /**
     * 设置 taskService
     *
     * @param taskService taskService
     */
    public void setTaskService(String taskService) {
        this.taskService = taskService;
    }

    @Override
    public BpmnTypeEnum getElementType() {
        return BpmnTypeEnum.SERVICE_TASK;
    }
}
