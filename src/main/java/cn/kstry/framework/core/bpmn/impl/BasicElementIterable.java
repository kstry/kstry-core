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
package cn.kstry.framework.core.bpmn.impl;

import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.bpmn.extend.ElementIterable;
import cn.kstry.framework.core.util.ElementParserUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * BPMN元素迭代器
 */
public class BasicElementIterable extends BasicAsyncFlowElement implements ElementIterable {

    /**
     * 指定需要从 StoryBus 中的什么地方来获取需要迭代的集合
     */
    private String source;

    /**
     * 迭代策略，默认：ALL_SUCCESS
     */
    private IterateStrategyEnum strategy;

    @Override
    public String getIteSource() {
        return this.source;
    }

    @Override
    public IterateStrategyEnum getIteStrategy() {
        return this.strategy;
    }

    @Override
    public boolean iterable() {
        return StringUtils.isNotBlank(this.source);
    }

    public void setIteSource(String source) {
        if (!ElementParserUtil.isValidDataExpression(source)) {
            return;
        }
        this.source = source;
    }

    public void setIteStrategy(IterateStrategyEnum strategy) {
        this.strategy = strategy;
    }
}
