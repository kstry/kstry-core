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
package cn.kstry.framework.core.component.demotion;

import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.container.task.TaskComponentRegister;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalServiceDemotion implements TaskComponentRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalServiceDemotion.class);

    @TaskService(name = "demotion", desc = "默认降级")
    public void demotion(ScopeDataOperator dataOperator) {
        ServiceNodeResource serviceNodeResource = dataOperator.getServiceNodeResource().orElse(null);
        String serviceName = StringUtils.EMPTY;
        String abilityName = StringUtils.EMPTY;
        String componentName = StringUtils.EMPTY;
        if (serviceNodeResource != null) {
            componentName = serviceNodeResource.getComponentName();
            abilityName = serviceNodeResource.getAbilityName();
            serviceName = serviceNodeResource.getServiceName();
        }
        LOGGER.info("Service node execution fails, performs default demotion method. component: {}, service: {}, ability: {}, businessId: {}",
                componentName, serviceName, abilityName, dataOperator.getBusinessId().orElse(StringUtils.EMPTY));
    }

    @Override
    public String getName() {
        return "global-service-demotion";
    }
}
