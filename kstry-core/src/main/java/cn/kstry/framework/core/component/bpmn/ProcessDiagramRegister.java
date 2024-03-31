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
package cn.kstry.framework.core.component.bpmn;

import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.enums.ResourceTypeEnum;
import cn.kstry.framework.core.resource.config.ConfigResource;

import java.util.List;

/**
 * 代码方式注册简单BPMN流程图
 *
 * @author lykan
 */
public interface ProcessDiagramRegister extends ConfigResource {

    /**
     * 获取配置文件名称
     *
     * @return 配置文件名称
     */
    default String getConfigName() {
        return this.getClass().getName();
    }

    /**
     * 注册子流程
     *
     * @param subLinkBuilderList 子流程配置
     */
    default void registerSubDiagram(List<SubProcessLink> subLinkBuilderList) {
        // DO NOTHING
    }

    /**
     * 注册流程
     *
     * @param processLinkList 流程配置
     */
    void registerDiagram(List<ProcessLink> processLinkList);

    @Override
    default ResourceTypeEnum getResourceType() {
        return ResourceTypeEnum.CODE_PROCESS;
    }
}
