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
package cn.kstry.framework.core.bpmn.extend;

import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.component.bpmn.builder.IterablePropertyBuilder;

/**
 * BPMN元素迭代器
 * <p>
 * 在 StoryBus 中选定集合，遍历集合中的每一项，使得每一项数据都执行一次当前元素节点
 */
public interface ElementIterable extends AsyncFlowElement {

    /**
     * 指定需要从 StoryBus 中的什么地方来获取需要迭代的集合
     *
     * @return StoryBus中的目标
     */
    String getIteSource();

    /**
     * 集合迭代是否开启异步，默认：false
     *
     * @return openAsync
     */
    Boolean openAsync();

    /**
     * 迭代策略，默认：ALL_SUCCESS
     *
     * @return IterateStrategyEnum
     */
    IterateStrategyEnum getIteStrategy();

    /**
     * 是否可以被遍历执行
     *
     * @return boolean
     */
    boolean iterable();

    /**
     * 迭代器构建器
     *
     * @param iteSource 迭代资源地址
     * @return 构建器
     */
    static IterablePropertyBuilder builder(String iteSource) {
        return new IterablePropertyBuilder(iteSource);
    }
}
