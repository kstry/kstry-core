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
package cn.kstry.framework.core.container.processor;

import cn.kstry.framework.core.bpmn.StartEvent;
import com.google.common.collect.ImmutableList;
import org.springframework.core.OrderComparator;

import java.util.List;
import java.util.Optional;

/**
 * 开始节点加工器
 *
 * @author lykan
 */
public class StartEventProcessor {

    /**
     * StartEvent 创建流程拦截器链
     */
    private final List<StartEventPostProcessor> startEventPostProcessors;

    public StartEventProcessor(List<StartEventPostProcessor> startEventPostProcessors) {
        OrderComparator.sort(startEventPostProcessors);
        this.startEventPostProcessors = ImmutableList.copyOf(startEventPostProcessors);
    }

    public Optional<StartEvent> postStartEvent(StartEvent startEvent) {
        if (startEvent == null) {
            return Optional.empty();
        }
        for (StartEventPostProcessor startEventPostProcessor : startEventPostProcessors) {
            if (!startEventPostProcessor.postStartEvent(startEvent).isPresent()) {
                return Optional.empty();
            }
        }
        return Optional.of(startEvent);
    }
}
