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
package cn.kstry.framework.core.component.launcher;

import cn.kstry.framework.core.component.bpmn.BpmnDiagramRegister;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.link.BpmnLink;
import org.apache.commons.collections.MapUtils;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

public class SpringBpmnDiagramRegister implements BpmnDiagramRegister {

    private final ApplicationContext applicationContext;

    public SpringBpmnDiagramRegister(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void registerSubDiagram(List<SubProcessLink> subLinkBuilderList) {
        Map<String, SubProcessLink> builderMap = applicationContext.getBeansOfType(SubProcessLink.class);
        if (MapUtils.isEmpty(builderMap)) {
            return;
        }
        subLinkBuilderList.addAll(builderMap.values());
    }

    @Override
    public void registerDiagram(List<BpmnLink> bpmnLinkList) {
        Map<String, BpmnLink> bpmnLinkMap = applicationContext.getBeansOfType(BpmnLink.class);
        if (MapUtils.isEmpty(bpmnLinkMap)) {
            return;
        }
        bpmnLinkList.addAll(bpmnLinkMap.values());
    }
}
