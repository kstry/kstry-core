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
package cn.kstry.framework.core.component.bpmn;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.bpmn.impl.SubProcessImpl;
import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementPropertyUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * 遍历流程图对象的模版。传入 StartEvent 可遍历整个流程图
 *
 * @author lykan
 */
public class DiagramTraverseSupport<T> {

    protected final boolean recursiveSubProcess;

    /**
     * 遍历时产生中间变量，不传中间变量为空
     */
    private final Function<StartEvent, T> courseFunction;

    public DiagramTraverseSupport() {
        this(null, true);
    }

    public DiagramTraverseSupport(Function<StartEvent, T> courseFunction, boolean recursiveSubProcess) {
        this.courseFunction = courseFunction;
        this.recursiveSubProcess = recursiveSubProcess;
    }

    public void traverse(StartEvent startEvent) {
        doTraverse(startEvent, null);
    }

    private void doTraverse(StartEvent startEvent, SubProcess subProcess) {
        AssertUtil.notNull(startEvent);
        T course = Optional.ofNullable(courseFunction).map(cf -> courseFunction.apply(startEvent)).orElse(null);
        Map<FlowElement, Integer> comingCountMap = Maps.newHashMap();
        InStack<FlowElement> basicInStack = new BasicInStack<>();
        basicInStack.push(startEvent);
        while (!basicInStack.isEmpty()) {
            FlowElement node = basicInStack.pop().orElseThrow(() -> ExceptionUtil.buildException(null, ExceptionEnum.SYSTEM_ERROR, null));
            if (node instanceof SubProcess) {
                SubProcessImpl sp = GlobalUtil.transferNotEmpty(node, SubProcessImpl.class);
                doSubProcess(sp);
                if (recursiveSubProcess) {
                    doTraverse(sp.getStartEvent(), sp);
                }
            }
            if (ElementPropertyUtil.isSupportAggregation(node)) {
                comingCountMap.merge(node, 1, Integer::sum);
                if (!Objects.equals(comingCountMap.get(node), node.comingList().size())) {
                    doAggregationBack(basicInStack, comingCountMap, startEvent, node);
                    continue;
                }
            }
            doPlainElement(course, node, subProcess);
            basicInStack.pushList(node.outingList());
        }
        doLast(course, startEvent);
    }

    public void doSubProcess(SubProcessImpl subProcess) {
        // DO NOTHING
    }

    public void doAggregationBack(InStack<FlowElement> inStack, Map<FlowElement, Integer> comingCountMap, StartEvent startEvent, FlowElement node) {
        // DO NOTHING
    }

    public void doPlainElement(T course, FlowElement node, SubProcess subProcess) {
        // DO NOTHING
    }

    public void doLast(T course, StartEvent startEvent) {
        // DO NOTHING
    }
}
