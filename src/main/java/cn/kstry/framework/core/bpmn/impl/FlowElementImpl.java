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
package cn.kstry.framework.core.bpmn.impl;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.SequenceFlow;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementPropertyUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

/**
 * FlowElementImpl
 */
public class FlowElementImpl extends BpmnElementImpl implements FlowElement {

    /**
     * 出度集合
     */
    private List<FlowElement> outingFlowElementList = Lists.newArrayList();

    /**
     * 入度集合
     */
    private List<FlowElement> comingFlowElementList = Lists.newArrayList();

    /**
     * 不可变标识
     */
    private boolean immutable = false;

    /**
     * 从 一个 AggregationFlowElement 到另一个 AggregationFlowElement 之间会经过一些普通节点
     * FlowTrack 保存所有的这些节点索引
     */
    private List<Integer> flowTrack = ImmutableList.of();

    @Override
    public void outing(FlowElement flowElement) {
        AssertUtil.notNull(flowElement);
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR);
        if (outingFlowElementList.contains(flowElement)) {
            return;
        }

        outingFlowElementList.forEach(flow -> AssertUtil.notTrue(Objects.equals(flowElement, flow), ExceptionEnum.COMPONENT_DUPLICATION_ERROR));
        boolean flow2Ele = (this instanceof SequenceFlow) && !(flowElement instanceof SequenceFlow);
        boolean ele2Flow = !(this instanceof SequenceFlow) && (flowElement instanceof SequenceFlow);
        AssertUtil.isTrue(flow2Ele || ele2Flow, ExceptionEnum.CONFIGURATION_FLOW_ERROR);

        outingFlowElementList.add(flowElement);
        if (flowElement instanceof FlowElementImpl) {
            ((FlowElementImpl) flowElement).coming(this);
        } else {
            throw ExceptionUtil.buildException(null, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, null);
        }
    }

    @Override
    public List<FlowElement> outingList() {
        return outingFlowElementList;
    }

    @Override
    public List<FlowElement> comingList() {
        return comingFlowElementList;
    }

    @Override
    public List<Integer> getFlowTrack() {
        return flowTrack;
    }

    @Override
    public void addFlowTrack(List<Integer> flowTrack) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        if (!ElementPropertyUtil.isSupportAggregation(this)) {
            this.flowTrack = ImmutableList.copyOf(flowTrack);
        }
    }

    @Override
    public void clearOutingChain() {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        outingFlowElementList.forEach(outNode -> GlobalUtil.transferNotEmpty(outNode, FlowElementImpl.class).comingFlowElementList.remove(this));
        outingFlowElementList.clear();
    }

    @Override
    public void immutable() {
        if (isImmutable()) {
            return;
        }
        outingFlowElementList = ImmutableList.copyOf(outingFlowElementList);
        comingFlowElementList = ImmutableList.copyOf(comingFlowElementList);
        immutable = true;
    }

    @Override
    public boolean isImmutable() {
        return immutable;
    }

    @Override
    public String identity() {
        return GlobalUtil.format("{}:[id:{}, name:{}]", getElementType(), getId(), getName());
    }

    @Override
    public void setId(String id) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        super.setId(id);
    }

    @Override
    public void setIndex(Integer index) {
        AssertUtil.notTrue(immutable, ExceptionEnum.COMPONENT_IMMUTABLE_ERROR, "FlowElement is not modifiable.");
        super.setIndex(index);
    }

    private void coming(FlowElement flowElement) {
        AssertUtil.notNull(flowElement);
        AssertUtil.isTrue(flowElement.outingList().contains(this), ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
        if (!this.comingFlowElementList.contains(flowElement)) {
            this.comingFlowElementList.add(flowElement);
        }
    }
}
