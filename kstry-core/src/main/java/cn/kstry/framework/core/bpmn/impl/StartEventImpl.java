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

import cn.kstry.framework.core.bpmn.EndEvent;
import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * StartEventImpl
 */
public class StartEventImpl extends EventImpl implements StartEvent {

    /**
     * 资源配置信息
     */
    private ConfigResource config;

    /**
     * End 节点
     */
    private EndEvent endEvent;

    private String processId;

    private String processName;

    private Map<String, FlowElement> midwayStartIdMapping;

    @Override
    public BpmnTypeEnum getElementType() {
        return BpmnTypeEnum.START_EVENT;
    }

    @Override
    public void setConfig(ConfigResource config) {
        this.config = config;
    }

    @Override
    public Optional<ConfigResource> getConfig() {
        return Optional.ofNullable(config);
    }

    @Override
    public EndEvent getEndEvent() {
        return endEvent;
    }

    @Override
    public void setEndEvent(EndEvent endEvent) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        this.endEvent = endEvent;
    }

    @Override
    public String getProcessId() {
        return processId;
    }

    @Override
    public void setProcessId(String processId) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        this.processId = processId;
    }

    @Override
    public String getProcessName() {
        return processName;
    }

    @Override
    public void setProcessName(String processName) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        this.processName = processName;
    }

    @Override
    public Optional<FlowElement> getMidwayStartElement(String midwayStartId) {
        if (StringUtils.isBlank(midwayStartId)) {
            return Optional.empty();
        }
        AssertUtil.notNull(midwayStartIdMapping);
        return Optional.ofNullable(midwayStartIdMapping.get(midwayStartId));
    }

    public void setMidwayStartIdMapping(Map<String, FlowElement> midwayStartIdMapping) {
        if (this.midwayStartIdMapping != null) {
            return;
        }
        if (MapUtils.isEmpty(midwayStartIdMapping)) {
            this.midwayStartIdMapping = ImmutableMap.of();
            return;
        }
        this.midwayStartIdMapping = ImmutableMap.copyOf(midwayStartIdMapping);
    }
}
