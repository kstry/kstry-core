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
import cn.kstry.framework.core.component.bpmn.BpmnModelTransfer;
import cn.kstry.framework.core.component.bpmn.CamundaBpmnModelTransfer;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author lykan
 */
public class BasicBpmnConfig extends BasicConfig implements BpmnConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicBpmnConfig.class);

    private static final BpmnModelTransfer modelTransfer = new CamundaBpmnModelTransfer();

    private final List<StartEvent> startEventList = Lists.newArrayList();

    public BasicBpmnConfig(Resource resource) {
        try {
            AssertUtil.notNull(resource, ExceptionEnum.CONFIGURATION_RESOURCE_ERROR);
            setConfigName(resource.getFilename());
            setConfigUri(resource.getURI());

            parserResource(resource.getInputStream());
            LOGGER.info("load bpmn resource. path: {}", getConfigUri());
        } catch (Exception e) {
            KstryException.throwException(e, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
        }
    }

    private void parserResource(InputStream inputStream) {
        if (inputStream == null) {
            return;
        }

        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
        Collection<org.camunda.bpm.model.bpmn.instance.StartEvent> startEventList = modelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.StartEvent.class);
        if (CollectionUtils.isEmpty(startEventList)) {
            return ;
        }

        List<StartEvent> collect = startEventList.stream()
                .filter(startEvent -> StringUtils.isNotBlank(startEvent.getId()) && startEvent.getId().startsWith(GlobalProperties.START_EVENT_ID_PREFIX))
                .map(org.camunda.bpm.model.bpmn.instance.StartEvent::getId)
                .map(id -> modelTransfer.getKstryModel(this, modelInstance, id))
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
        this.startEventList.addAll(collect);
    }

    @Override
    public List<StartEvent> getStartEventList() {
        return ImmutableList.copyOf(startEventList);
    }
}
