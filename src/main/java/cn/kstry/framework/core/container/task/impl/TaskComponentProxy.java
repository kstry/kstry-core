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
package cn.kstry.framework.core.container.task.impl;

import cn.kstry.framework.core.container.task.TaskComponentRegister;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.ExceptionUtil;

/**
 *
 * @author lykan
 */
public class TaskComponentProxy implements TaskComponentRegister {

    private final Object target;

    private final String name;

    public TaskComponentProxy(Object target) {
        AssertUtil.notNull(target);
        this.target = target;
        this.name = ElementParserUtil.getTaskComponentName(target).orElseThrow(() -> ExceptionUtil.buildException(null, ExceptionEnum.COMPONENT_ATTRIBUTES_EMPTY, null));
    }

    public Object getTarget() {
        return target;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
