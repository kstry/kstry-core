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
package cn.kstry.framework.core.component.bpmn.builder;

import cn.kstry.framework.core.bpmn.impl.BasicElementIterable;
import cn.kstry.framework.core.bpmn.impl.SubProcessImpl;
import cn.kstry.framework.core.component.bpmn.link.SubBpmnLink;
import cn.kstry.framework.core.component.bpmn.link.SubDiagramBpmnLink;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.util.AssertUtil;

import java.util.function.Consumer;

public abstract class SubProcessLink {

    private final String subProcessId;

    private final String subProcessName;

    private final String startEventId;

    private final String startEventName;

    private Integer timeout;

    private Boolean strictMode;

    private BasicElementIterable elementIterable;

    private SubProcessImpl subProcess;

    public SubProcessLink(String subProcessId, String subProcessName, String startEventId, String startEventName) {
        AssertUtil.notBlank(subProcessId, ExceptionEnum.NOT_ALLOW_EMPTY);
        this.subProcessId = subProcessId;
        this.subProcessName = subProcessName;
        this.startEventId = startEventId;
        this.startEventName = startEventName;
    }

    public SubDiagramBpmnLink buildSubDiagramBpmnLink(ConfigResource config) {
        AssertUtil.notNull(config, ExceptionEnum.NOT_ALLOW_EMPTY);
        SubDiagramBpmnLink subBpmnLink = new SubDiagramBpmnLink(this, config, subProcessId, subProcessName, startEventId, startEventName);
        SubProcessImpl subProcess = subBpmnLink.getElement();
        subProcess.setConfig(config);
        subProcess.setStrictMode(strictMode);
        subProcess.setTimeout(timeout);
        subProcess.mergeElementIterable(elementIterable);
        doBuild(subBpmnLink);
        this.subProcess = subProcess;
        return subBpmnLink;
    }

    abstract void doBuild(SubBpmnLink subBpmnLink);

    public String getSubProcessId() {
        return subProcessId;
    }

    public SubProcessImpl getSubProcess() {
        return subProcess;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public void setStrictMode(Boolean strictMode) {
        this.strictMode = strictMode;
    }

    public void setElementIterable(BasicElementIterable elementIterable) {
        this.elementIterable = elementIterable;
    }

    public static SubProcessLink build(String id, Consumer<SubBpmnLink> builder) {
        return build(id, null, builder);
    }

    public static SubProcessLink build(String id, String name, Consumer<SubBpmnLink> builder) {
        return build(id, name, null, null, builder);
    }

    public static SubProcessLink build(String id, String name, String startEventId, String startEventName, Consumer<SubBpmnLink> builder) {
        return new SubProcessLink(id, name, startEventId, startEventName) {

            @Override
            public void doBuild(SubBpmnLink subBpmnLink) {
                builder.accept(subBpmnLink);
            }
        };
    }
}
