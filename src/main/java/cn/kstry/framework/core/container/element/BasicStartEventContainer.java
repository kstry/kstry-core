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
package cn.kstry.framework.core.container.element;

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.container.processor.StartEventProcessor;
import cn.kstry.framework.core.resource.factory.StartEventFactory;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * StartEvent 容器
 *
 * @author lykan
 */
public class BasicStartEventContainer implements StartEventContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicStartEventContainer.class);

    private final StartEventProcessor startEventProcessor;

    /**
     * 保存 StartEvent
     */
    private Map<String, StartEvent> globalStartEventMap;

    /**
     * StartEvent 资源创建工厂
     */
    private final StartEventFactory startEventFactory;

    public BasicStartEventContainer(StartEventFactory startEventFactory, StartEventProcessor startEventProcessor) {
        AssertUtil.anyNotNull(startEventFactory, startEventProcessor);
        this.startEventFactory = startEventFactory;
        this.startEventProcessor = startEventProcessor;
    }

    @PostConstruct
    public void refreshStartEvent() {
        List<StartEvent> resourceList = this.startEventFactory.getResourceList();
        Map<String, StartEvent> startEventMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(resourceList)) {
            LOGGER.warn("No bpmn configuration information available!");
            this.globalStartEventMap = ImmutableMap.copyOf(startEventMap);
            return;
        }
        resourceList.stream().map(event -> startEventProcessor.postStartEvent(event).orElse(null)).filter(Objects::nonNull).forEach(event -> startEventMap.put(event.getId(), event));
        this.globalStartEventMap = ImmutableMap.copyOf(startEventMap);
    }

    @Override
    public Optional<StartEvent> getStartEventById(ScopeDataQuery scopeDataQuery) {
        if (StringUtils.isBlank(scopeDataQuery.getStartId())) {
            return Optional.empty();
        }
        StartEvent startEvent = globalStartEventMap.get(scopeDataQuery.getStartId());
        if (startEvent != null) {
            return Optional.of(startEvent);
        }
        return startEventFactory.getDynamicStartEvent(scopeDataQuery);
    }
}
