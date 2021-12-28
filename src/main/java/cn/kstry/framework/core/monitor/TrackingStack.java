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
package cn.kstry.framework.core.monitor;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.component.utils.BasicInStack;
import cn.kstry.framework.core.component.utils.InStack;
import cn.kstry.framework.core.util.AssertUtil;

import java.util.List;

/**
 *
 * @author lykan
 */
public class TrackingStack extends BasicInStack<FlowElement> implements InStack<FlowElement> {

    /**
     * 链路追踪器
     */
    private final MonitorTracking monitorTracking;

    public TrackingStack(MonitorTracking monitorTracking) {
        AssertUtil.notNull(monitorTracking);
        this.monitorTracking = monitorTracking;
    }

    public void push(FlowElement prevElement, FlowElement flowElement) {
        monitorTracking.buildNodeTracking(prevElement).ifPresent(t -> t.addToNodeId(flowElement.getId()));
        super.push(flowElement);
    }

    public void pushList(FlowElement prevElement, List<FlowElement> list) {
        monitorTracking.buildNodeTracking(prevElement).ifPresent(t -> list.stream().map(FlowElement::getId).forEach(t::addToNodeId));
        super.pushList(list);
    }
}
