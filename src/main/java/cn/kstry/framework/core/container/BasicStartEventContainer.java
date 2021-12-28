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
package cn.kstry.framework.core.container;

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.container.processor.StartEventPostProcessor;
import cn.kstry.framework.core.enums.ConfigTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.resource.config.BpmnConfig;
import cn.kstry.framework.core.resource.config.Config;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * StartEvent 容器
 *
 * @author lykan
 */
public class BasicStartEventContainer implements StartEventContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicStartEventContainer.class);

    private final List<StartEventPostProcessor> startEventPostProcessorList = Lists.newArrayList();

    private final Map<String, StartEvent> startEventMap = Maps.newHashMap();

    private final List<ConfigResource> configResourceList;

    public BasicStartEventContainer(Collection<ConfigResource> configResourceList) {
        this.configResourceList = configResourceList.stream().filter(r -> r.getConfigType() == ConfigTypeEnum.BPMN).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(this.configResourceList)) {
            LOGGER.warn("No bpmn configuration information available!");
        }
    }

    public void initStartEvent() {
        if (CollectionUtils.isEmpty(configResourceList)) {
            return;
        }
        configResourceList.forEach(resource -> {
            List<Config> list = resource.getConfigList();
            if (CollectionUtils.isEmpty(list)) {
                return;
            }

            list.stream().map(cfg -> GlobalUtil.transferNotEmpty(cfg, BpmnConfig.class)).forEach(cfg -> {
                List<StartEvent> startEventList = cfg.getStartEventList();
                startEventList.stream().filter(event -> {
                    for (StartEventPostProcessor startEventPostProcessor : startEventPostProcessorList) {
                        if (!startEventPostProcessor.postStartEvent(event).isPresent()) {
                            return false;
                        }
                    }
                    return true;
                }).forEach(event -> {
                    StartEvent existStartEvent = startEventMap.get(event.getId());
                    if (existStartEvent != null) {
                        KstryException.throwException(ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                GlobalUtil.format("There are duplicate start event ids defined! id:{}, fileName1: {}, fileName2: {}",
                                        event.getId(), cfg.getConfigName(), existStartEvent.getConfig().map(Config::getConfigName).orElse(null)));
                    }
                    startEventMap.put(event.getId(), event);
                });
            });
        });
    }

    @Override
    public Optional<StartEvent> getStartEventById(String id) {
        if (StringUtils.isBlank(id)) {
            return Optional.empty();
        }
        return Optional.ofNullable(startEventMap.get(id));
    }

    @Override
    public void registerStartEventPostProcessor(StartEventPostProcessor processor) {
        if (processor == null) {
            return;
        }
        startEventPostProcessorList.add(processor);
    }
}
