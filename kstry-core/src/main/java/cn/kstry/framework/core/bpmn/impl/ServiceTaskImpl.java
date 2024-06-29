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
package cn.kstry.framework.core.bpmn.impl;

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.component.limiter.RateLimiterConfig;
import cn.kstry.framework.core.constant.BpmnElementProperties;
import cn.kstry.framework.core.enums.PermissionType;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.PermissionUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * ServiceTaskImpl
 */
public class ServiceTaskImpl extends TaskImpl implements ServiceTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTaskImpl.class);

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

    /**
     * 任务参数
     */
    private String taskParams;

    /**
     * 任务参数包装类
     */
    private TaskParamWrapper taskParamWrapper;

    /**
     * 服务节点重试次数
     */
    private Integer retryTimes;

    /**
     * 限速器相关配置
     */
    private RateLimiterConfig rateLimiterConfig;

    /**
     * 指定服务节点执行失败（包含重试）或超时后的降级处理表达式。异常发生后会根据表达式从容器中找到对应的服务节点执行
     * 降级表达式举例：
     *  1> pr:risk-control@check-img  服务组件名是 risk-control 且 服务名是 init-base-info 的服务节点
     *  2> pr:risk-control@check-img@triple  服务组件名是 risk-control 且 服务名是 init-base-info 且 能力点名是 triple 的服务节点
     */
    private ServiceNodeResource taskDemotion;

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
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
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
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        this.customRoleInfo = customRoleInfo;
    }

    /**
     * 设置 taskService
     *
     * @param taskService taskService
     */
    public void setTaskService(String taskService) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        this.taskService = taskService;
    }

    public void setTaskProperty(String taskProperty) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
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

    public String getTaskInstruct() {
        return taskInstruct;
    }

    public String getTaskInstructContent() {
        return taskInstructContent;
    }

    public void setTaskInstructContent(String taskInstructContent) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        this.taskInstructContent = taskInstructContent;
    }

    public void setTaskInstruct(String taskInstruct) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        AssertUtil.notBlank(taskInstruct, ExceptionEnum.CONFIGURATION_ATTRIBUTES_REQUIRED, "TaskInstruct cannot be blank!");
        this.taskInstruct = StringUtils.replaceOnceIgnoreCase(taskInstruct, BpmnElementProperties.SERVICE_TASK_TASK_INSTRUCT, StringUtils.EMPTY);
    }

    @Override
    public Map<String, Object> getTaskParams() {
        return StringUtils.isBlank(taskParams) ? null : JSON.parseObject(taskParams);
    }

    public void setTaskParams(String taskParams) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        if (StringUtils.isBlank(taskParams)) {
            return;
        }
        this.taskParamWrapper = new TaskParamWrapper(taskParams);
        this.taskParams = taskParams;
    }

    @Override
    public TaskParamWrapper getTaskParamWrapper() {
        return taskParamWrapper;
    }

    /**
     * 设置 allowAbsent
     *
     * @param allowAbsent allowAbsent
     */
    public void setAllowAbsent(Boolean allowAbsent) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        this.allowAbsent = allowAbsent;
    }

    @Override
    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        this.retryTimes = retryTimes;
    }

    @Override
    public Optional<RateLimiterConfig> getRateLimiterConfig() {
        return Optional.ofNullable(rateLimiterConfig);
    }

    public void setRateLimiterConfig(RateLimiterConfig rateLimiterConfig) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        this.rateLimiterConfig = rateLimiterConfig;
    }

    @Override
    public ServiceNodeResource getTaskDemotion() {
        return taskDemotion;
    }

    public void setTaskDemotion(String taskDemotion) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        this.taskDemotion = PermissionUtil.parseResource(taskDemotion)
                .filter(p -> p.getPermissionType() == PermissionType.COMPONENT_SERVICE || p.getPermissionType() == PermissionType.COMPONENT_SERVICE_ABILITY).orElse(null);
        if (StringUtils.isNotBlank(taskDemotion) && this.taskDemotion == null) {
            LOGGER.warn("[{}] Not effective when setting the taskDemotion attribute to ServiceTask! identity: {}, demotion: {}", ExceptionEnum.DEMOTION_DEFINITION_ERROR.getExceptionCode(), identity(), taskDemotion);
        }
    }

    @Override
    public String identity() {
        return GlobalUtil.format("{}:[id: {}, name: {}, component: {}, service: {}]", getElementType(), getId(), getName(), taskComponent, taskService);
    }

    @Override
    public BpmnTypeEnum getElementType() {
        return BpmnTypeEnum.SERVICE_TASK;
    }
}
