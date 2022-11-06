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
package cn.kstry.framework.core.component.bpmn.link;

import cn.kstry.framework.core.bpmn.FlowElement;

/**
 * 提供在元素之间的 BPMN 元素代码方式连接能力
 *
 * @author lykan
 */
public class BpmnElementDiagramLink<T extends FlowElement> extends BpmnDiagramLink implements BpmnLink {

    /**
     * 被连接的元素
     */
    private final T element;

    private final BpmnLink bpmnLink;

    public BpmnElementDiagramLink(T element, BpmnLink bpmnLink) {
        this.element = element;
        this.bpmnLink = bpmnLink;
    }

    @Override
    public BpmnLink getBpmnLink() {
        return bpmnLink;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getElement() {
        return element;
    }
}
