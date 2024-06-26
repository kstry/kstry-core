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
package cn.kstry.framework.core.bpmn;

import cn.kstry.framework.core.bpmn.extend.ElementIterable;
import cn.kstry.framework.core.component.expression.Expression;

import java.util.Optional;

/**
 * Task
 */
public interface Task extends FlowElement, Expression {

    /**
     * 获取超时时间
     *
     * @return 超时时间，单位ms
     */
    Integer getTimeout();

    /**
     * 是否为严格模式
     *
     * @return 默认 true
     */
    boolean strictMode();

    /**
     * 获取元素迭代器
     *
     * @return 元素迭代器
     */
    Optional<ElementIterable> getElementIterable();
}
