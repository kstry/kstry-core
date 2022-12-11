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
package cn.kstry.framework.core.component.dynamic;

import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.enums.DynamicComponentType;
import org.springframework.context.ApplicationContext;

/**
 * 动态子流程组件
 */
public class SubProcessDynamicComponent extends SpringDynamicComponent<SubProcessLink> {

    public SubProcessDynamicComponent(ApplicationContext applicationContext) {
        super(0L, applicationContext);
    }

    @Override
    public DynamicComponentType getComponentType() {
        return DynamicComponentType.SUB_PROCESS;
    }
}
