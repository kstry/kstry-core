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

import cn.kstry.framework.core.component.expression.Expression;

import java.util.List;

/**
 * SequenceFlow
 */
public interface SequenceFlow extends FlowElement, Expression {

    /**
     * 添加通过该 SequenceFlow 所能到达的 EndElement 节点
     *
     * @param endElementList 所能到达的 EndElement 节点
     */
    void addEndElementList(List<FlowElement> endElementList);

    /**
     * 获取通过该 SequenceFlow 所能到达的 EndElement 节点
     *
     * @return 通过该 SequenceFlow 所能到达的 EndElement 节点
     */
    List<FlowElement> getEndElementList();

    /**
     * 将 EndElementList 变为不可变对象
     */
    void immutableEndElement();
}
