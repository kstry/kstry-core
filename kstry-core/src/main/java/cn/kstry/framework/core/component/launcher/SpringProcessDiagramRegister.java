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
package cn.kstry.framework.core.component.launcher;

import cn.kstry.framework.core.component.bpmn.ProcessDiagramRegister;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.preheat.PreheatProcessDef;
import org.apache.commons.collections.MapUtils;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

public class SpringProcessDiagramRegister implements ProcessDiagramRegister {

    private final ApplicationContext applicationContext;

    public SpringProcessDiagramRegister(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void registerSubDiagram(List<SubProcessLink> subLinkBuilderList) {
        subLinkBuilderList.add(PreheatProcessDef.buildPreheatSubProcess());
        Map<String, SubProcessLink> builderMap = applicationContext.getBeansOfType(SubProcessLink.class);
        if (MapUtils.isEmpty(builderMap)) {
            return;
        }
        subLinkBuilderList.addAll(builderMap.values());
    }

    @Override
    public void registerDiagram(List<ProcessLink> processLinkList) {
        processLinkList.add(PreheatProcessDef.buildPreheatProcess());
        Map<String, ProcessLink> processLinkMap = applicationContext.getBeansOfType(ProcessLink.class);
        if (MapUtils.isEmpty(processLinkMap)) {
            return;
        }
        processLinkList.addAll(processLinkMap.values());
    }
}
