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
package cn.kstry.framework.core.container.component;

import cn.kstry.framework.core.annotation.TaskInstruct;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lykan
 */
public class TaskInstructWrapper {

    private final String name;

    public TaskInstructWrapper(TaskInstruct taskInstruct, String taskService) {
        this.name = StringUtils.isBlank(taskInstruct.name()) ? taskService : taskInstruct.name();
    }

    public String getName() {
        return name;
    }
}
