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
package cn.kstry.framework.core.component.bpmn.link;

import cn.kstry.framework.core.component.bpmn.builder.InclusiveJoinPointBuilder;
import cn.kstry.framework.core.component.bpmn.builder.ParallelJoinPointBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * 在 BpmnLink 的基础上增加了开始节点特有的功能
 */
public interface StartProcessLink extends ProcessLink {

    /**
     * 创建包含网关汇聚节点
     *
     * @return 包含网关汇聚节点构建类
     */
    InclusiveJoinPointBuilder inclusive();

    /**
     * 创建并行网关汇聚节点
     *
     * @return 包含网关汇聚节点构建类
     */
    ParallelJoinPointBuilder parallel();

    /**
     * 构建
     *
     * @param id 非空
     * @return BpmnLink
     */
    static StartProcessLink build(String id) {
        return build(id, StringUtils.EMPTY);
    }

    /**
     * 构建
     *
     * @param id   非空
     * @param name 允许为空
     * @return BpmnLink
     */
    static StartProcessLink build(String id, String name) {
        return new StartDiagramProcessLink(id, name);
    }
}
