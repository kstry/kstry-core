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
package cn.kstry.framework.core.bpmn;

import java.util.List;

/**
 * FlowElement
 */
public interface FlowElement extends BpmnElement {

    /**
     * 下一个连接 元素
     * 被允许：
     *     sequence flow -> element
     *     element -> sequence flow
     * <p>
     * 不被允许：
     *     element x-> element
     *     sequence flow x-> sequence flow
     *
     * @param flowElement flowElement
     */
    void outing(FlowElement flowElement);

    /**
     * 拿到 出度 列表
     *
     * @return list
     */
    List<FlowElement> outingList();

    /**
     * 拿到 入度 列表
     *
     * @return list
     */
    List<FlowElement> comingList();

    /**
     * 从 一个 AggregationFlowElement 到另一个 AggregationFlowElement 之间会经过一些普通节点
     * FlowTrack 保存所有的这些节点索引
     *
     * @return flowTrack 获取 FlowTrack
     */
    List<Integer> getFlowTrack();

    /**
     * 从 一个 AggregationFlowElement 到另一个 AggregationFlowElement 之间会经过一些普通节点
     * FlowTrack 保存所有的这些节点索引
     *
     * @param flowTrack flowTrack 新增
     */
    void addFlowTrack(List<Integer> flowTrack);

    /**
     * 断开该节点与后面节点的关联
     */
    void clearOutingChain();

    /**
     * 将出度、入度 变成不可变对象
     */
    void immutable();
}
