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

import cn.kstry.framework.core.bpmn.EndEvent;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.resource.config.Config;

import java.util.Optional;

/**
 * StartEventImpl
 */
public class StartEventImpl extends EventImpl implements StartEvent {

    /**
     * 资源配置信息
     */
    private Config config;

    /**
     * End 节点
     */
    private EndEvent endEvent;

    @Override
    public BpmnTypeEnum getElementType() {
        return BpmnTypeEnum.START_EVENT;
    }

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public Optional<Config> getConfig() {
        return Optional.ofNullable(config);
    }

    @Override
    public EndEvent getEndEvent() {
        return endEvent;
    }

    @Override
    public void setEndEvent(EndEvent endEvent) {
        this.endEvent = endEvent;
    }
}
