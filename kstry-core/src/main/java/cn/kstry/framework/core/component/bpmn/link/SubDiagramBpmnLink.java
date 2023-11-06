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
package cn.kstry.framework.core.component.bpmn.link;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.impl.SubProcessImpl;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.util.ElementParserUtil;
import org.apache.commons.lang3.StringUtils;

public class SubDiagramBpmnLink extends StartDiagramProcessLink implements SubBpmnLink {

    private final SubProcessImpl subProcess;

    public SubDiagramBpmnLink(SubProcessLink builder, ConfigResource configResource, String id, String name, String startId, String startName) {
        super(StringUtils.isBlank(startId) ? SubBpmnLink.relatedStartId(id) : startId, StringUtils.isBlank(startName) ? name : startName, id, name);
        SubProcessImpl subProcess = new SubProcessImpl(allSub -> {
            SubDiagramBpmnLink processLink = builder.buildSubDiagramBpmnLink(configResource);
            SubProcessImpl sp = processLink.getElement();
            StartEvent startEvent = processLink.getStartEvent();
            startEvent.setConfig(sp.getConfig());
            ElementParserUtil.fillSubProcess(allSub, startEvent);
            return startEvent;
        });
        subProcess.setId(id);
        subProcess.setName(name);
        this.subProcess = subProcess;
    }

    @Override
    public <T extends FlowElement> T beforeElement() {
        return super.getElement();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends FlowElement> T getElement() {
        return (T) subProcess;
    }

    @Override
    public StartEvent getStartEvent() {
        return super.getElement();
    }
}
