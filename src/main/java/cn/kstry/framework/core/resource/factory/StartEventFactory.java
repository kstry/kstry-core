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
package cn.kstry.framework.core.resource.factory;

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.bpmn.impl.SubProcessImpl;
import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.component.bpmn.BpmnDiagramRegister;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.dynamic.ProcessDynamicComponent;
import cn.kstry.framework.core.enums.ResourceTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.config.BpmnConfigResource;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * StartEvent 资源创建工厂
 *
 * @author lykan
 */
public class StartEventFactory extends BasicResourceFactory<StartEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartEventFactory.class);

    private Map<String, SubProcess> allSubProcessMap;

    private List<StartEvent> resourceList;
    private final ProcessDynamicComponent processDynamicComponent;

    public StartEventFactory(ApplicationContext applicationContext, ProcessDynamicComponent processDynamicComponent) {
        super(applicationContext);
        this.processDynamicComponent = processDynamicComponent;
    }

    @PostConstruct
    public void initResourceList() {
        List<ConfigResource> configResourceList = getConfigResource(ResourceTypeEnum.BPMN);
        List<ConfigResource> diagramConfigResourceList = getConfigResource(ResourceTypeEnum.BPMN_DIAGRAM);
        if (CollectionUtils.isEmpty(configResourceList) && CollectionUtils.isEmpty(diagramConfigResourceList)) {
            this.resourceList = Lists.newArrayList();
            return;
        }

        List<BpmnConfigResource> bpmnResourceList =
                configResourceList.stream().map(c -> GlobalUtil.transferNotEmpty(c, BpmnConfigResource.class)).collect(Collectors.toList());
        List<BpmnDiagramRegister> bpmnDiagramResourceList =
                diagramConfigResourceList.stream().map(r -> GlobalUtil.transferNotEmpty(r, BpmnDiagramRegister.class)).collect(Collectors.toList());
        Map<String, SubProcess> aspMap = getAllSubProcessMap(bpmnResourceList, bpmnDiagramResourceList);
        List<StartEvent> list = getStartEvents(aspMap, bpmnResourceList, bpmnDiagramResourceList);
        notDuplicateCheck(list);
        this.resourceList = Collections.unmodifiableList(list);
        this.allSubProcessMap = ImmutableMap.copyOf(aspMap);
    }

    @Override
    public List<StartEvent> getResourceList() {
        return resourceList;
    }

    public Optional<StartEvent> getDynamicStartEvent(ScopeDataQuery scopeDataQuery) {
        return processDynamicComponent.getStartEvent(allSubProcessMap, scopeDataQuery);
    }

    private static void notDuplicateCheck(List<StartEvent> list) {
        List<StartEvent> duplicateList = list.stream().collect(Collectors.toMap(e -> e, e -> 1, Integer::sum))
                .entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(duplicateList)) {
            return;
        }
        StartEvent es = duplicateList.get(0);
        String fileName = es.getConfig().map(ConfigResource::getConfigName).orElse(null);
        throw ExceptionUtil.buildException(null, ExceptionEnum.ELEMENT_DUPLICATION_ERROR,
                GlobalUtil.format("There are duplicate start event ids defined! identity: {}, fileName: {}", es.identity(), fileName));
    }

    private List<StartEvent> getStartEvents(Map<String, SubProcess> allSubProcess, List<BpmnConfigResource> bpmnResourceList, List<BpmnDiagramRegister> bpmnDiagramResourceList) {
        List<StartEvent> startEventList = bpmnResourceList.stream().flatMap(bpmnResource ->
                bpmnResource.getStartEventList().stream()).peek(startEvent -> ElementParserUtil.fillSubProcess(allSubProcess, startEvent)).collect(Collectors.toList());

        bpmnDiagramResourceList.forEach(bpmnDiagramRegister -> {
            List<ProcessLink> processLinkList = Lists.newArrayList();
            bpmnDiagramRegister.registerDiagram(processLinkList);
            if (CollectionUtils.isEmpty(processLinkList)) {
                return;
            }
            LOGGER.info("Load bpmn code register. name: {}", bpmnDiagramRegister.getConfigName());
            List<StartEvent> sList = Lists.newArrayList();
            for (ProcessLink processLink : processLinkList) {
                if (processLink == null) {
                    continue;
                }
                StartEvent startEvent = processLink.getElement();
                startEvent.setConfig(bpmnDiagramRegister);
                ElementParserUtil.fillSubProcess(allSubProcess, startEvent);
                sList.add(startEvent);
            }
            startEventList.addAll(sList);
        });
        return startEventList;
    }

    private Map<String, SubProcess> getAllSubProcessMap(List<BpmnConfigResource> bpmnResourceList, List<BpmnDiagramRegister> bpmnDiagramResourceList) {
        Map<String, SubProcess> allSubProcess = Maps.newHashMap();
        bpmnResourceList.forEach(bpmnResource -> {
            Map<String, SubProcessLink> sbMap = bpmnResource.getSubProcessMap();
            if (MapUtils.isEmpty(sbMap)) {
                return;
            }
            sbMap.forEach((k, v) -> {
                AssertUtil.notTrue(allSubProcess.containsKey(k),
                        ExceptionEnum.ELEMENT_DUPLICATION_ERROR, "There are duplicate SubProcess ids defined! id: {}", k);
                allSubProcess.put(k, v.getSubProcess());
            });
        });

        bpmnDiagramResourceList.forEach(diagramConfig -> {
            List<SubProcessLink> subLinkBuilderList = Lists.newArrayList();
            diagramConfig.registerSubDiagram(subLinkBuilderList);
            if (CollectionUtils.isEmpty(subLinkBuilderList)) {
                return;
            }
            subLinkBuilderList.forEach(linkBuilder -> {
                SubProcessImpl subProcess = linkBuilder.buildSubDiagramBpmnLink(diagramConfig).getElement();
                AssertUtil.notTrue(allSubProcess.containsKey(subProcess.getId()),
                        ExceptionEnum.ELEMENT_DUPLICATION_ERROR, "There are duplicate SubProcess ids defined! identity: {}", subProcess.identity());
                allSubProcess.put(subProcess.getId(), subProcess);
            });
        });
        return allSubProcess;
    }
}
