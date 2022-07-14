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
package cn.kstry.framework.core.resource.factory;

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.bpmn.impl.SubProcessImpl;
import cn.kstry.framework.core.enums.ResourceTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.config.BpmnConfigResource;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StartEvent 资源创建工厂
 *
 * @author lykan
 */
public class StartEventFactory extends BasicResourceFactory<StartEvent> {

    public StartEventFactory(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public List<StartEvent> getResourceList() {
        List<ConfigResource> configResourceList = getConfigResource(ResourceTypeEnum.BPMN);
        if (CollectionUtils.isEmpty(configResourceList)) {
            return Lists.newArrayList();
        }

        Map<String, SubProcess> allSubProcess = Maps.newHashMap();
        List<BpmnConfigResource> bpmnResourceList =
                configResourceList.stream().map(c -> GlobalUtil.transferNotEmpty(c, BpmnConfigResource.class)).collect(Collectors.toList());
        bpmnResourceList.forEach(bpmnResource -> {
            Map<String, SubProcess> sbMap = bpmnResource.getSubProcessMap();
            if (MapUtils.isEmpty(sbMap)) {
                return;
            }
            sbMap.forEach((k, v) -> {
                AssertUtil.notTrue(allSubProcess.containsKey(k),
                        ExceptionEnum.ELEMENT_DUPLICATION_ERROR, "There are duplicate SubProcess ids defined! id: {}", k);
                allSubProcess.put(k, v);
            });
        });

        allSubProcess.values().forEach(subProcess -> {
            SubProcessImpl impl = GlobalUtil.transferNotEmpty(subProcess, SubProcessImpl.class);
            impl.startEventSupplier(allSubProcess);
        });

        List<StartEvent> list = bpmnResourceList.stream()
                .flatMap(bpmnResource -> bpmnResource.getStartEventList(allSubProcess).stream()).collect(Collectors.toList());
        List<StartEvent> duplicateList = list.stream().collect(Collectors.toMap(e -> e, e -> 1, Integer::sum))
                .entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(duplicateList)) {
            StartEvent es = duplicateList.get(0);
            String fileName = es.getConfig().map(ConfigResource::getConfigName).orElse(null);
            throw ExceptionUtil.buildException(null, ExceptionEnum.ELEMENT_DUPLICATION_ERROR,
                    GlobalUtil.format("There are duplicate start event ids defined! id: {}, fileName: {}", es.getId(), fileName));
        }
        return list;
    }
}
