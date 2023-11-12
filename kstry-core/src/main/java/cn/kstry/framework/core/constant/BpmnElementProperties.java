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
package cn.kstry.framework.core.constant;

/**
 * BpmnConstant
 *
 * @author lykan
 */
public interface BpmnElementProperties {

    /**
     * ServiceTask: task-component
     */
    String SERVICE_TASK_TASK_COMPONENT = "task-component";

    /**
     * ServiceTask: task-service
     */
    String SERVICE_TASK_TASK_SERVICE = "task-service";

    /**
     * ServiceTask: 服务节点属性
     */
    String SERVICE_TASK_TASK_PROPERTY = "task-property";

    /**
     * ServiceTask: 任务指令
     */
    String SERVICE_TASK_TASK_INSTRUCT = "c-";

    /**
     * ServiceTask: allow-absent
     */
    String TASK_ALLOW_ABSENT = "allow-absent";

    /**
     * ServiceTask: custom-role
     */
    String SERVICE_TASK_CUSTOM_ROLE = "custom-role";

    /**
     * ServiceTask: 服务节点参数指定
     */
    String SERVICE_TASK_TASK_PARAMS = "task-params";

    /**
     * ServiceTask: 指定服务节点重试次数
     */
    String SERVICE_TASK_RETRY_TIMES = "retry-times";

    /**
     * AsyncFlowElement(InclusiveGateway/ParallelGateway/ElementIterator(SubProcess/ServiceTask)): open-async
     */
    String ASYNC_ELEMENT_OPEN_ASYNC = "open-async";

    /**
     * InclusiveGateway: completed-count
     */
    String INCLUSIVE_GATEWAY_COMPLETED_COUNT = "completed-count";

    /**
     * InclusiveGateway: midway-start-id
     */
    String INCLUSIVE_GATEWAY_MIDWAY_START_ID = "midway-start-id";

    /**
     * SubProcess/ParallelGateway/ServiceTask: strict-mode
     */
    String TASK_STRICT_MODE = "strict-mode";

    /**
     * SubProcess/ServiceTask: timeout
     */
    String TASK_TIMEOUT = "timeout";

    /**
     * ElementIterator(SubProcess/ServiceTask): ite-source
     */
    String ITERATE_SOURCE = "ite-source";

    /**
     * ElementIterator(SubProcess/ServiceTask): ite-async
     */
    String ITERATE_ASYNC = "ite-async";

    /**
     * ElementIterator(SubProcess/ServiceTask): ite-strategy
     */
    String ITERATE_STRATEGY = "ite-strategy";

    /**
     * ElementIterator(SubProcess/ServiceTask): ite-align-index
     */
    String ITERATE_ALIGN_INDEX = "ite-align-index";

    /**
     * ElementIterator(SubProcess/ServiceTask): ite-stride
     */
    String ITERATE_STRIDE = "ite-stride";
}
