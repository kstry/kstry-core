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
package cn.kstry.framework.core.component.bpmn;

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.resource.config.Config;

import java.util.Optional;

/**
 *
 * @author lykan
 */
public interface BpmnModelTransfer {

    /**
     * 从 其他 Bpmn Model 转化为 Kstry Model
     *
     * @param config  config
     * @param instance  Bpmn Model
     * @param startId startId
     * @return Bpmn Model
     */
    Optional<StartEvent> getKstryModel(Config config, Object instance, String startId);
}
