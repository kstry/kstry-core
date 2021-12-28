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
package cn.kstry.framework.core.constant;

/**
 * BpmnConstant
 *
 * @author lykan
 */
public interface BpmnConstant {

    /**
     * ServiceTask: task-component
     */
    String SERVICE_TASK_TASK_COMPONENT = "task-component";

    /**
     * ServiceTask: task-service
     */
    String SERVICE_TASK_TASK_SERVICE = "task-service";

    /**
     * ServiceTask: allow-absent
     */
    String SERVICE_TASK_ALLOW_ABSENT = "allow-absent";

    /**
     * ServiceTask: custom-role
     */
    String SERVICE_TASK_CUSTOM_ROLE = "custom-role";

    /**
     * AsyncElement: open-async
     */
    String ASYNC_ELEMENT_OPEN_ASYNC = "open-async";

    /**
     * element: strict-mode
     */
    String ELEMENT_STRICT_MODE = "strict-mode";
}
