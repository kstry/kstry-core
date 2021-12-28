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
package cn.kstry.framework.core.bpmn.enums;

/**
 * BPMN 元素类型
 *
 * @author lykan
 */
public enum BpmnTypeEnum {

    /**
     * BPMN 元素
     */
    ELEMENT("element", null),

    /**
     * 表达式
     */
    EXPRESSION("expression", ELEMENT),

    /**
     * 网关
     */
    GATEWAY("gateway", ELEMENT),

    /**
     * Task
     */
    TASK("task", ELEMENT),

    /**
     * Event
     */
    EVENT("event", ELEMENT),

    /**
     * Sequence Flow
     */
    SEQUENCE_FLOW("sequence_flow", ELEMENT),

    /**
     * Start Event
     */
    START_EVENT("start_event", EVENT),

    /**
     * End Event
     */
    END_EVENT("end_event", EVENT),

    /**
     * Parallel Gateway
     */
    PARALLEL_GATEWAY("parallel_gateway", GATEWAY),

    /**
     * Exclusive Gateway
     */
    EXCLUSIVE_GATEWAY("exclusive_gateway", GATEWAY),

    /**
     * Inclusive Gateway
     */
    INCLUSIVE_GATEWAY("inclusive_gateway", GATEWAY),

    /**
     * Service Task
     */
    SERVICE_TASK("service_task", TASK),

    /**
     * Sub Process
     */
    SUB_PROCESS("sub_process", TASK),
    ;

    private final BpmnTypeEnum parent;

    private final String type;

    BpmnTypeEnum(String type, BpmnTypeEnum parent) {
        this.parent = parent;
        this.type = type;
    }

    /**
     * 获取 parent
     *
     * @return parent
     */
    public BpmnTypeEnum getParent() {
        return parent;
    }

    /**
     * 获取类型
     *
     * @return 类型
     */
    public String getType() {
        return type;
    }
}
