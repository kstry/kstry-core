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
package cn.kstry.framework.core.container.processor;

import cn.kstry.framework.core.bpmn.EndEvent;
import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.resource.config.Config;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementPropertyUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author lykan
 */
public class VerifyFlowPostProcessor implements StartEventPostProcessor {

    @Override
    public Optional<StartEvent> postStartEvent(StartEvent startEvent) {

        Set<EndEvent> endEventSet = Sets.newHashSet();
        Map<FlowElement, Integer> comingCountMap = Maps.newHashMap();
        InStack<FlowElement> basicInStack = new BasicInStack<>();
        basicInStack.push(startEvent);
        while (!basicInStack.isEmpty()) {
            FlowElement node = basicInStack.pop().orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
            if (node instanceof SubProcess) {
                postStartEvent(((SubProcess) node).getStartEvent());
            }
            if (ElementPropertyUtil.isSupportAggregation(node)) {
                comingCountMap.merge(node, 1, Integer::sum);
                if (!Objects.equals(comingCountMap.get(node), node.comingList().size())) {
                    AssertUtil.isTrue(comingCountMap.get(node) < node.comingList().size() && basicInStack.peek().isPresent(),
                            ExceptionEnum.CONFIGURATION_FLOW_ERROR, "Wrong branch in the path of an element! bpmnId: {}, fileName: {}",
                            node.getId(), startEvent.getConfig().map(Config::getConfigName).orElse(null));
                    continue;
                }
            }
            if (node instanceof EndEvent) {
                endEventSet.add((EndEvent) node);
            }
            basicInStack.pushList(node.outingList());
        }
        AssertUtil.oneSize(endEventSet, ExceptionEnum.CONFIGURATION_UNSUPPORTED_ELEMENT,
                "EndEvent must appear and can only appear once! fileName: {}", startEvent.getConfig().map(Config::getConfigName).orElse(null));
        return Optional.of(startEvent);
    }
}
