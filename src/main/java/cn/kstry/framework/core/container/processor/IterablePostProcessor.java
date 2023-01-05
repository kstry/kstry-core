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
package cn.kstry.framework.core.container.processor;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.bpmn.impl.BasicElementIterable;
import cn.kstry.framework.core.bpmn.impl.ServiceTaskImpl;
import cn.kstry.framework.core.component.bpmn.DiagramTraverseSupport;
import cn.kstry.framework.core.util.GlobalUtil;

import java.util.Optional;

/**
 * 选出需要集合迭代的子流程，将子流程下的全部 ServiceTask 都设置迭代属性
 *
 * @author lykan
 */
public class IterablePostProcessor extends DiagramTraverseSupport<Object> implements StartEventPostProcessor {

    @Override
    public Optional<StartEvent> postStartEvent(StartEvent startEvent) {
        traverse(startEvent);
        return Optional.of(startEvent);
    }

    @Override
    public void doPlainElement(Object course, FlowElement node, SubProcess subProcess) {
        if (subProcess == null || !subProcess.iterable() || !(node instanceof ServiceTaskImpl)) {
            return;
        }
        BasicElementIterable beIterable = new BasicElementIterable();
        beIterable.setIteSource(subProcess.getIteSource());
        beIterable.setOpenAsync(subProcess.openAsync());
        beIterable.setIteStrategy(subProcess.getIteStrategy());
        beIterable.setStride(subProcess.getStride());
        GlobalUtil.transferNotEmpty(node, ServiceTaskImpl.class).mergeElementIterable(beIterable);
    }

    @Override
    public int getOrder() {
        return 40;
    }
}
