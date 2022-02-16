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

import java.util.Optional;

import cn.kstry.framework.core.bpmn.ExclusiveGateway;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.ServiceTaskSupport;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;

/**
 * ExclusiveGatewayImpl
 */
public class ExclusiveGatewayImpl extends GatewayImpl implements ExclusiveGateway, ServiceTaskSupport {

    /**
     * 支持定义 ServiceTask
     */
    private final ServiceTask serviceTask;

    public ExclusiveGatewayImpl(ServiceTask serviceTask) {
        if (serviceTask != null && serviceTask.validTask()) {
            this.serviceTask = serviceTask;
        } else {
            this.serviceTask = null;
        }
    }

    @Override
    public BpmnTypeEnum getElementType() {
        return BpmnTypeEnum.EXCLUSIVE_GATEWAY;
    }

    @Override
    public Optional<ServiceTask> getServiceTask() {
        return Optional.ofNullable(serviceTask);
    }
}
