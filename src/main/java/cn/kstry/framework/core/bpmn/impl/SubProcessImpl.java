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

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.util.AssertUtil;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;

/**
 * SubProcessImpl
 */
public class SubProcessImpl extends TaskImpl implements SubProcess {

    /**
     * 开始节点
     */
    private StartEvent startEvent;

    /**
     * 开始节点延迟生成器
     */
    private final Function<Map<String, SubProcess>, StartEvent> startEventBuilder;

    public SubProcessImpl(Function<Map<String, SubProcess>, StartEvent> startEventBuilder) {
        AssertUtil.notNull(startEventBuilder);
        this.startEventBuilder = startEventBuilder;
    }

    @Override
    public StartEvent getStartEvent() {
        return startEvent;
    }

    @Override
    public BpmnTypeEnum getElementType() {
        return BpmnTypeEnum.SUB_PROCESS;
    }

    public SubProcess cloneSubProcess(Map<String, SubProcess> allSubProcess) {
        SubProcessImpl subProcess = new SubProcessImpl(this.startEventBuilder);
        subProcess.setId(getId());
        subProcess.setName(getName());
        subProcess.strictMode = this.strictMode;
        subProcess.timeout = this.timeout;
        subProcess.startEvent = startEventBuilder.apply(allSubProcess);
        return subProcess;
    }

    public void startEventSupplier(@Nonnull Map<String, SubProcess> allSubProcess) {
        if (startEvent != null) {
            return;
        }
        this.startEvent = startEventBuilder.apply(allSubProcess);
    }
}
