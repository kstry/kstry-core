/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.component.bpmn.builder;

import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.bpmn.extend.ElementIterable;
import cn.kstry.framework.core.bpmn.impl.SequenceFlowExpression;
import cn.kstry.framework.core.bpmn.impl.SubProcessImpl;
import cn.kstry.framework.core.component.bpmn.link.BpmnElementDiagramLink;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.util.GlobalUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * SubProcessBuilder 构建类
 *
 * @author lykan
 */
public class SubProcessBuilder {

    private final ProcessLink processLink;

    private final SubProcessImpl subProcess;

    public SubProcessBuilder(SubProcessImpl subProcess, ProcessLink processLink) {
        this.processLink = processLink;
        this.subProcess = subProcess;
    }

    public SubProcessBuilder notStrictMode() {
        this.subProcess.setStrictMode(false);
        return this;
    }

    public SubProcessBuilder iterable(ElementIterable iterable) {
        this.subProcess.mergeElementIterable(iterable);
        return this;
    }

    public SubProcessBuilder timeout(int timeout) {
        this.subProcess.setTimeout(Math.max(timeout, 0));
        return this;
    }

    public SubProcessBuilder notSkipExp(String exp) {
        if (StringUtils.isBlank(exp)) {
            return this;
        }
        SequenceFlowExpression sequenceFlowExpression = new SequenceFlowExpression(exp);
        sequenceFlowExpression.setId(GlobalUtil.uuid(BpmnTypeEnum.EXPRESSION));
        sequenceFlowExpression.setName(exp);
        this.subProcess.setExpression(sequenceFlowExpression);
        return this;
    }

    public ProcessLink build() {
        return new BpmnElementDiagramLink<SubProcess>(subProcess, processLink);
    }
}
