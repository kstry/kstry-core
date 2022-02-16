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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.Maps;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementPropertyUtil;

/**
 * 将元素中的出入度变为不可变对象，防止后面程序修改了出入度导致链路出错
 *
 * @author lykan
 */
public class ImmutablePostProcessor implements StartEventPostProcessor{

    @Override
    public Optional<StartEvent> postStartEvent(StartEvent startEvent) {
        AssertUtil.notNull(startEvent);
        Map<FlowElement, Integer> comingCountMap = Maps.newHashMap();
        InStack<FlowElement> basicInStack = new BasicInStack<>();
        basicInStack.push(startEvent);
        while (!basicInStack.isEmpty()) {
            FlowElement node = basicInStack.pop().orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
            if (ElementPropertyUtil.isSupportAggregation(node)) {
                comingCountMap.merge(node, 1, Integer::sum);
                if (!Objects.equals(comingCountMap.get(node), node.comingList().size())) {
                    continue;
                }
            }
            AssertUtil.notTrue(node instanceof SubProcess);
            node.immutable();
            basicInStack.pushList(node.outingList());
        }
        return Optional.of(startEvent);
    }
}
