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
package cn.kstry.framework.core.bpmn;

import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;

/**
 * BaseElement
 */
public interface BaseElement {

    /**
     * 获取 ID
     * @return id
     */
    String getId();

    /**
     * 设置 ID
     * @param id id
     */
    void setId(String id);

    /**
     * 获取名称
     * @return 名称
     */
    String getName();

    /**
     * 设置名称
     *
     * @param name 名称
     */
    void setName(String name);

    /**
     * 获取索引
     * @return 索引
     */
    Integer getIndex();

    /**
     * 设置索引
     *
     * @param index 索引
     */
    void setIndex(Integer index);

    /**
     * 获取 元素类型
     *
     * @return 元素类型
     */
    BpmnTypeEnum getElementType();
}
