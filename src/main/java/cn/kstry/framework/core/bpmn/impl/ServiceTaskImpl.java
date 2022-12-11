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
package cn.kstry.framework.core.bpmn.impl;

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.constant.BpmnElementProperties;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

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
     */
    private ServiceNodeResource customRoleInfo;

    /**
     * 未匹配到子任务时，是否可以忽略当前节点继续向下执行
     * 默认：false。未匹配到子任务时，抛出异常
     */
    private Boolean allowAbsent;

    /**
     * 任务属性
     */
    private String taskProperty;

    /**
     * 任务指令
     */
    private String taskInstruct;

    /**
     * 任务指令参数
     */
    private String taskInstructContent;

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
    public ServiceNodeResource getCustomRoleInfo() {
        return customRoleInfo;
    }

    @Override
    public boolean validTask() {
        return !StringUtils.isAllBlank(taskService, taskInstruct);
    }

    /**
     * 设置角色自定义组件
     *
     * @param customRoleInfo 角色自定义组件
     */
    public void setCustomRoleInfo(ServiceNodeResource customRoleInfo) {
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

    public void setTaskProperty(String taskProperty) {
        this.taskProperty = taskProperty;
    }

    @Override
    public boolean allowAbsent() {
        return BooleanUtils.isTrue(allowAbsent);
    }

    @Override
    public String getTaskProperty() {
        return taskProperty;
    }

    public Boolean getAllowAbsent() {
        return allowAbsent;
    }

    public String getTaskInstruct() {
        return taskInstruct;
    }

    public String getTaskInstructContent() {
        return taskInstructContent;
    }

    public void setTaskInstructContent(String taskInstructContent) {
        this.taskInstructContent = taskInstructContent;
    }

    public void setTaskInstruct(String taskInstruct) {
        AssertUtil.notBlank(taskInstruct, ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, GlobalUtil.format("TaskInstruct cannot be blank!"));
        this.taskInstruct = taskInstruct.replaceFirst(BpmnElementProperties.SERVICE_TASK_TASK_INSTRUCT, StringUtils.EMPTY);
    }

    /**
     * 设置 allowAbsent
     *
     * @param allowAbsent allowAbsent
     */
    public void setAllowAbsent(Boolean allowAbsent) {
        this.allowAbsent = allowAbsent;
    }

    @Override
    public String identity() {
        return GlobalUtil.format("{}:[id:{}, name:{}, component:{}, service: {}]", getElementType(), getId(), getName(), taskComponent, taskService);
    }

    @Override
    public BpmnTypeEnum getElementType() {
        return BpmnTypeEnum.SERVICE_TASK;
    }
}
