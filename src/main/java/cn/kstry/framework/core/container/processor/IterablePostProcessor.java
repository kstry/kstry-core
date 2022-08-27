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
package cn.kstry.framework.core.container.processor;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.bpmn.impl.BasicElementIterable;
import cn.kstry.framework.core.bpmn.impl.ServiceTaskImpl;
import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementPropertyUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 选出需要集合迭代的子流程，将子流程下的全部 ServiceTask 都设置迭代属性
 *
 * @author lykan
 */
public class IterablePostProcessor implements StartEventPostProcessor {

    @Override
    public Optional<StartEvent> postStartEvent(StartEvent startEvent) {
        AssertUtil.notNull(startEvent);
        syncIterableProperty(startEvent, null);
        return Optional.of(startEvent);
    }

    private void syncIterableProperty(StartEvent startEvent, SubProcess subProcess) {
        Map<FlowElement, Integer> comingCountMap = Maps.newHashMap();
        InStack<FlowElement> basicInStack = new BasicInStack<>();
        basicInStack.push(startEvent);
        while (!basicInStack.isEmpty()) {
            FlowElement node = basicInStack.pop().orElseThrow(() -> ExceptionUtil.buildException(null, ExceptionEnum.SYSTEM_ERROR, null));
            if (node instanceof SubProcess) {
                SubProcess sp = GlobalUtil.transferNotEmpty(node, SubProcess.class);
                syncIterableProperty(sp.getStartEvent(), sp);
            }
            if (ElementPropertyUtil.isSupportAggregation(node)) {
                comingCountMap.merge(node, 1, Integer::sum);
                if (!Objects.equals(comingCountMap.get(node), node.comingList().size())) {
                    continue;
                }
            }
            if (subProcess != null && subProcess.iterable() && node instanceof ServiceTaskImpl) {
                ServiceTaskImpl serviceTask = GlobalUtil.transferNotEmpty(node, ServiceTaskImpl.class);
                BasicElementIterable beIterable = serviceTask.buildElementIterable();
                if (StringUtils.isBlank(beIterable.getIteSource())) {
                    beIterable.setIteSource(subProcess.getIteSource());
                }
                if (beIterable.openAsync() == null) {
                    beIterable.setOpenAsync(BooleanUtils.isTrue(subProcess.openAsync()));
                }
                if (beIterable.getIteStrategy() == null) {
                    beIterable.setIteStrategy(subProcess.getIteStrategy());
                }
            }
            basicInStack.pushList(node.outingList());
        }
    }

    @Override
    public int getOrder() {
        return 40;
    }
}
