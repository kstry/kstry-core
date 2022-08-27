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
package cn.kstry.framework.core.bpmn;

import cn.kstry.framework.core.bpmn.extend.AggregationFlowElement;
import cn.kstry.framework.core.bpmn.extend.AsyncFlowElement;

/**
 * ParallelGateway
 */
public interface ParallelGateway extends Gateway, AggregationFlowElement, AsyncFlowElement {

    /**
     * 是否为严格模式，严格模式下的并行节点必须所有入度都得执行到，否则报错
     *
     * @return true 默认：true
     */
    boolean isStrictMode();
}
