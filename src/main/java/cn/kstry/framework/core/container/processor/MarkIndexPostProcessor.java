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

import cn.kstry.framework.core.bpmn.*;
import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementPropertyUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lykan
 */
public class MarkIndexPostProcessor implements StartEventPostProcessor {

    @Override
    public Optional<StartEvent> postStartEvent(StartEvent startEvent) {
        AtomicInteger index = new AtomicInteger(1);
        return doPostStartEvent(index, startEvent);
    }

    private Optional<StartEvent> doPostStartEvent(AtomicInteger index, StartEvent startEvent) {
        AssertUtil.notNull(startEvent);

        // 顺序标记
        // 1、标记 index
        // 2、标记 endEvent
        // 3、标记 flowTrack
        comingMark(index, startEvent);

        // 逆序标记
        // 1、标记 SequenceFlow 可达的聚合节点
        outingMark(startEvent);
        return Optional.of(startEvent);
    }

    private void comingMark(AtomicInteger index, StartEvent startEvent) {
        Map<FlowElement, Integer> comingCountMap = Maps.newHashMap();

        InStack<FlowElement> basicInStack = new BasicInStack<>();
        basicInStack.push(startEvent);
        while (!basicInStack.isEmpty()) {
            FlowElement node = basicInStack.pop().orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
            if (node instanceof SubProcess) {
                doPostStartEvent(index, ((SubProcess) node).getStartEvent());
            }
            if (ElementPropertyUtil.isSupportAggregation(node)) {
                comingCountMap.merge(node, 1, Integer::sum);
                if (!Objects.equals(comingCountMap.get(node), node.comingList().size())) {
                    continue;
                }
            }
            if (node instanceof EndEvent) {
                startEvent.setEndEvent(GlobalUtil.transferNotEmpty(node, EndEvent.class));
            }
            node.setIndex(index.getAndIncrement());

            List<Integer> flowTrack = Lists.newArrayList(node.getFlowTrack());
            flowTrack.add(node.getIndex());
            node.outingList().forEach(flowElement -> flowElement.addFlowTrack(flowTrack));
            basicInStack.pushList(node.outingList());
        }
    }

    private void outingMark(StartEvent startEvent) {
        Set<SequenceFlow> sequenceFlowSet = Sets.newHashSet();
        List<FlowElement> flowElements = startEvent.getEndEvent().comingList();
        AssertUtil.notEmpty(flowElements);
        flowElements.forEach(flowElement ->
                GlobalUtil.transferNotEmpty(flowElement, SequenceFlow.class).addEndElementList(Lists.newArrayList(startEvent.getEndEvent()))
        );

        InStack<FlowElement> outStack = new BasicInStack<>();
        outStack.pushList(flowElements);
        Map<FlowElement, Integer> outingCountMap = Maps.newHashMap();
        while (!outStack.isEmpty()) {
            FlowElement node = outStack.pop().orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
            if (ElementPropertyUtil.isSupportAggregation(node)) {
                outingCountMap.merge(node, 1, Integer::sum);
                if (!Objects.equals(outingCountMap.get(node), node.outingList().size())) {
                    continue;
                }
                List<FlowElement> comingList = node.comingList();
                AssertUtil.notEmpty(comingList);
                comingList.forEach(flowElement -> GlobalUtil.transferNotEmpty(flowElement, SequenceFlow.class).addEndElementList(Lists.newArrayList(node)));
                outStack.pushList(node.comingList());
                continue;
            }
            if (!(node instanceof SequenceFlow)) {
                outStack.pushList(node.comingList());
                continue;
            }
            SequenceFlow sequenceFlow = GlobalUtil.transferNotEmpty(node, SequenceFlow.class);
            sequenceFlowSet.add(sequenceFlow);
            if (ElementPropertyUtil.isSupportAggregation(sequenceFlow.comingList().get(0))) {
                outStack.pushList(node.comingList());
                continue;
            }
            List<FlowElement> oneSizeComingList = sequenceFlow.comingList().get(0).comingList();
            if (CollectionUtils.isEmpty(oneSizeComingList)) {
                AssertUtil.isTrue(sequenceFlow.comingList().get(0) instanceof StartEvent, ExceptionEnum.CONFIGURATION_RESOURCE_ERROR);
                continue;
            }
            AssertUtil.oneSize(oneSizeComingList, ExceptionEnum.CONFIGURATION_RESOURCE_ERROR);
            GlobalUtil.transferNotEmpty(oneSizeComingList.get(0), SequenceFlow.class).addEndElementList((sequenceFlow.getEndElementList()));
            outStack.pushList(node.comingList());
        }
        sequenceFlowSet.forEach(SequenceFlow::immutableEndElement);
    }
}
