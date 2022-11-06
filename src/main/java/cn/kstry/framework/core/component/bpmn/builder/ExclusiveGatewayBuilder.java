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
package cn.kstry.framework.core.component.bpmn.builder;

import cn.kstry.framework.core.bpmn.ExclusiveGateway;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.impl.ExclusiveGatewayImpl;
import cn.kstry.framework.core.component.bpmn.link.BpmnElementDiagramLink;
import cn.kstry.framework.core.component.bpmn.link.BpmnLink;

/**
 * ExclusiveGateway 构建类
 *
 * @author lykan
 */
public class ExclusiveGatewayBuilder {

    private final BpmnLink bpmnLink;

    private final ExclusiveGatewayImpl exclusiveGateway;

    public ExclusiveGatewayBuilder(ExclusiveGatewayImpl exclusiveGateway, BpmnLink bpmnLink) {
        this.bpmnLink = bpmnLink;
        this.exclusiveGateway = exclusiveGateway;
    }

    public ExclusiveGatewayBuilder serviceTask(ServiceTask serviceTask) {
        exclusiveGateway.setServiceTask(serviceTask);
        return this;
    }

    public BpmnLink build() {
        return new BpmnElementDiagramLink<ExclusiveGateway>(exclusiveGateway, bpmnLink);
    }
}
