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
package cn.kstry.framework.core.component.bpmn.builder;

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.bpmn.extend.ElementIterable;
import cn.kstry.framework.core.bpmn.impl.SequenceFlowExpression;
import cn.kstry.framework.core.bpmn.impl.ServiceTaskImpl;
import cn.kstry.framework.core.component.bpmn.link.BpmnElementDiagramLink;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.limiter.RateLimiterConfig;
import cn.kstry.framework.core.util.CustomRoleInfo;
import cn.kstry.framework.core.util.GlobalUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * ServiceTask 构建类
 *
 * @author lykan
 */
public class ServiceTaskBuilder {

    private final ProcessLink processLink;

    private final ServiceTaskImpl serviceTask;

    public ServiceTaskBuilder() {
        this.processLink = null;
        this.serviceTask = new ServiceTaskImpl();
    }

    public ServiceTaskBuilder(ServiceTaskImpl serviceTask, ProcessLink processLink) {
        this.processLink = processLink;
        this.serviceTask = serviceTask;
    }

    public ServiceTaskBuilder id(String id) {
        serviceTask.setId(id);
        return this;
    }

    public ServiceTaskBuilder name(String name) {
        serviceTask.setName(name);
        return this;
    }

    public ServiceTaskBuilder service(String service) {
        serviceTask.setTaskService(service);
        return this;
    }

    public ServiceTaskBuilder property(String property) {
        serviceTask.setTaskProperty(property);
        return this;
    }

    public ServiceTaskBuilder component(String component) {
        serviceTask.setTaskComponent(component);
        return this;
    }

    public ServiceTaskBuilder instruct(String instruct) {
        serviceTask.setTaskInstruct(instruct);
        return this;
    }

    public ServiceTaskBuilder instructContent(String instructContent) {
        serviceTask.setTaskInstructContent(instructContent);
        return this;
    }

    public ServiceTaskBuilder params(String taskParams) {
        serviceTask.setTaskParams(taskParams);
        return this;
    }

    public ServiceTaskBuilder customRole(String customRole) {
        this.serviceTask.setCustomRoleInfo(CustomRoleInfo.buildCustomRole(customRole).orElse(null));
        return this;
    }

    public ServiceTaskBuilder allowAbsent() {
        this.serviceTask.setAllowAbsent(true);
        return this;
    }

    public ServiceTaskBuilder retryTimes(Integer retryTimes) {
        if (retryTimes == null || retryTimes <= 0) {
            return this;
        }
        this.serviceTask.setRetryTimes(retryTimes);
        return this;
    }

    public ServiceTaskBuilder notStrictMode() {
        this.serviceTask.setStrictMode(false);
        return this;
    }

    public ServiceTaskBuilder iterable(ElementIterable iterable) {
        this.serviceTask.mergeElementIterable(iterable);
        return this;
    }

    public ServiceTaskBuilder timeout(int timeout) {
        this.serviceTask.setTimeout(Math.max(timeout, 0));
        return this;
    }

    public ServiceTaskBuilder notSkipExp(String exp) {
        if (StringUtils.isBlank(exp)) {
            return this;
        }
        SequenceFlowExpression sequenceFlowExpression = new SequenceFlowExpression(exp);
        sequenceFlowExpression.setId(GlobalUtil.uuid(BpmnTypeEnum.EXPRESSION));
        sequenceFlowExpression.setName(exp);
        this.serviceTask.setExpression(sequenceFlowExpression);
        return this;
    }

    public ServiceTaskBuilder rateLimiter(RateLimiterConfig rateLimiterConfig) {
        this.serviceTask.setRateLimiterConfig(rateLimiterConfig);
        return this;
    }

    public ServiceTaskBuilder taskDemotion(String taskDemotion) {
        if (StringUtils.isBlank(taskDemotion)) {
            return this;
        }
        this.serviceTask.setTaskDemotion(taskDemotion);
        return this;
    }

    public ProcessLink build() {
        return new BpmnElementDiagramLink<ServiceTask>(serviceTask, processLink);
    }

    public ServiceTask ins() {
        return this.serviceTask;
    }
}
