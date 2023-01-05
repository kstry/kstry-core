/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
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
import cn.kstry.framework.core.bpmn.impl.InclusiveGatewayImpl;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.bpmn.joinpoint.InclusiveJoinPoint;

public class InclusiveJoinPointBuilder {

    private final ProcessLink processLink;

    private final InclusiveGatewayImpl inclusiveGateway;

    public InclusiveJoinPointBuilder(InclusiveGatewayImpl inclusiveGateway, ProcessLink processLink) {
        this.processLink = processLink;
        this.inclusiveGateway = inclusiveGateway;
    }

    public InclusiveJoinPointBuilder openAsync() {
        inclusiveGateway.setOpenAsync(true);
        return this;
    }

    public InclusiveJoinPointBuilder serviceTask(ServiceTask serviceTask) {
        inclusiveGateway.setServiceTask(serviceTask);
        return this;
    }

    public InclusiveJoinPoint build() {
        return new InclusiveJoinPoint(inclusiveGateway, processLink);
    }
}
