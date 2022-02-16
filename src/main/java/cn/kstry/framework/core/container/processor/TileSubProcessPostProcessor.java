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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.kstry.framework.core.bpmn.EndEvent;
import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementPropertyUtil;
import cn.kstry.framework.core.util.GlobalUtil;

/**
 * 将子分支铺开，整合到父流程，最终组成一个大流程
 *
 * @author lykan
 */
public class TileSubProcessPostProcessor implements StartEventPostProcessor{

    @Override
    public Optional<StartEvent> postStartEvent(StartEvent startEvent) {
        tileSubProcess(startEvent);
        return Optional.of(startEvent);
    }

    private void tileSubProcess(StartEvent startEvent) {
        AssertUtil.notNull(startEvent);
        Map<FlowElement, Integer> comingCountMap = Maps.newHashMap();
        InStack<FlowElement> basicInStack = new BasicInStack<>();
        basicInStack.push(startEvent);
        while (!basicInStack.isEmpty()) {
            FlowElement node = basicInStack.pop().orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
            List<FlowElement> flowElements = Lists.newArrayList(node.outingList());
            if (node instanceof SubProcess) {
                StartEvent subStartEvent = GlobalUtil.transferNotEmpty(node, SubProcess.class).getStartEvent();
                AssertUtil.notNull(subStartEvent);
                EndEvent endEvent = subStartEvent.getEndEvent();
                AssertUtil.notNull(endEvent);

                // 递归平铺 SubProcess
                tileSubProcess(subStartEvent);

                FlowElement comingFlow = node.comingList().get(0);
                comingFlow.clearOutingChain();
                comingFlow.outing(subStartEvent);
                node.clearOutingChain();
                flowElements.forEach(endEvent::outing);
            }
            if (ElementPropertyUtil.isSupportAggregation(node)) {
                comingCountMap.merge(node, 1, Integer::sum);
                if (!Objects.equals(comingCountMap.get(node), node.comingList().size())) {
                    continue;
                }
            }
            basicInStack.pushList(flowElements);
        }
    }
}
