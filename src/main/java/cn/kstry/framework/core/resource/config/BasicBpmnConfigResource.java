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
package cn.kstry.framework.core.resource.config;

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.component.bpmn.BpmnModelTransfer;
import cn.kstry.framework.core.component.bpmn.CamundaBpmnModelTransfer;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BPMN 配置信息，一个对象对应一个 BPMN 文件
 *
 * @author lykan
 */
public class BasicBpmnConfigResource extends AbstractConfigResource implements BpmnConfigResource {

    /**
     * BPMN 文件解析器Camunda
     */
    private static final BpmnModelTransfer<BpmnModelInstance> modelTransfer = new CamundaBpmnModelTransfer();

    /**
     * Camunda 解析 BPMN 文件得到的实例对象
     */
    private BpmnModelInstance modelInstance;

    public BasicBpmnConfigResource(Resource resource) {
        super(resource);
    }

    @Override
    public void init(Resource resource, InputStream inputStream) {
        try {
            this.modelInstance = Bpmn.readModelFromStream(inputStream);
            AssertUtil.notNull(this.modelInstance);
        } catch (Exception e) {
            throw KstryException.buildException(e, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, GlobalUtil.format("BPMN configuration file parsing failure! fileName: {}", getConfigName()));
        }
    }

    @Override
    public Map<String, SubProcess> getSubProcessMap() {
        AssertUtil.notNull(modelInstance);
        return modelTransfer.getAllSubProcess(this, modelInstance);
    }

    @Override
    public List<StartEvent> getStartEventList(Map<String, SubProcess> allSubProcess) {
        AssertUtil.anyNotNull(modelInstance, allSubProcess);
        Collection<org.camunda.bpm.model.bpmn.instance.StartEvent> startEventList =
                modelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.StartEvent.class);
        if (CollectionUtils.isEmpty(startEventList)) {
            return Lists.newArrayList();
        }
        return startEventList.stream()
                .filter(startEvent -> StringUtils.isNotBlank(startEvent.getId()) && startEvent.getId().startsWith(GlobalProperties.START_EVENT_ID_PREFIX))
                .map(org.camunda.bpm.model.bpmn.instance.StartEvent::getId)
                .map(id -> modelTransfer.getKstryModel(allSubProcess, this, modelInstance, id))
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
    }
}
