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
package cn.kstry.framework.core.component.bpmn;

import cn.kstry.framework.core.bpmn.StartEvent;

import java.util.Optional;

/**
 * 序列化流程
 *
 * @author lykan
 */
public interface SerializeProcessParser<T> {

    /**
     * 将流程序列化成字符串
     *
     * @param startEvent 开始事件
     * @return 序列化结果
     */
    Optional<T> serialize(StartEvent startEvent);

    /**
     * BPMN配置文件转JSON
     *
     * @param bpmn bpmn文件
     * @return 序列化结果
     */
    Optional<T> bpmnSerialize(String bpmn);
}
