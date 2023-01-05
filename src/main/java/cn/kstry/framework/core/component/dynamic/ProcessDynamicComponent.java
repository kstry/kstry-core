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
package cn.kstry.framework.core.component.dynamic;

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.container.processor.StartEventProcessor;
import cn.kstry.framework.core.enums.DynamicComponentType;
import cn.kstry.framework.core.exception.BusinessException;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.util.ElementParserUtil;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 动态流程组件
 */
public class ProcessDynamicComponent extends SpringDynamicComponent<ProcessLink> implements ConfigResource {

    private final StartEventProcessor startEventProcessor;

    private final SubProcessDynamicComponent subProcessDynamicComponent;

    public ProcessDynamicComponent(ApplicationContext applicationContext, StartEventProcessor startEventProcessor, SubProcessDynamicComponent subProcessDynamicComponent) {
        super(50_000L, applicationContext);
        this.startEventProcessor = startEventProcessor;
        this.subProcessDynamicComponent = subProcessDynamicComponent;
    }

    public Optional<StartEvent> getStartEvent(Map<String, SubProcess> allSubProcess, ScopeDataQuery scopeDataQuery) {
        return dynamicGetComponent(null, new HashMap<String, SubProcess>() {

            @Override
            public SubProcess get(Object key) {

                SubProcess subProcess = allSubProcess.get(key);
                if (subProcess != null) {
                    return subProcess;
                }
                return subProcessDynamicComponent.dynamicGetComponent(String.valueOf(key), null, scopeDataQuery)
                        .<SubProcess>map(subProcessLink -> subProcessLink.buildSubDiagramBpmnLink(ProcessDynamicComponent.this).getElement()).orElse(null);
            }

            @Override
            public SubProcess put(String key, SubProcess value) {
                throw new BusinessException(ExceptionEnum.SYSTEM_ERROR.getExceptionCode(), "Method is not allowed to be called!");
            }

        }, scopeDataQuery).map(ProcessLink::getElement);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<ProcessLink> checkDecorateComponent(ProcessLink component, Object param) {
        if (component == null) {
            return Optional.empty();
        }

        StartEvent startEvent = component.getElement();
        startEvent.setConfig(this);
        Map<String, SubProcess> allSubProcess = (Map<String, SubProcess>) param;
        ElementParserUtil.fillSubProcess(allSubProcess, startEvent);
        if (!startEventProcessor.postStartEvent(startEvent).isPresent()) {
            return Optional.empty();
        }
        return Optional.of(component);
    }

    @Override
    public DynamicComponentType getComponentType() {
        return DynamicComponentType.PROCESS;
    }

    @Override
    public String getConfigName() {
        return ProcessDynamicComponent.class.getName();
    }
}
