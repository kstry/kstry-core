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
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.ElementParserUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BPMN元素迭代器
 */
public class BasicElementIterable extends BasicAsyncFlowElement implements ElementIterable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicElementIterable.class);

    /**
     * 指定需要从 StoryBus 中的什么地方来获取需要迭代的集合
     */
    private String source;

    /**
     * 迭代策略，默认：ALL_SUCCESS
     */
    private IterateStrategyEnum strategy;

    /**
     * 迭代步长，即每批处理多少元素。默认为空，代表每批处理1个元素
     */
    private Integer stride;

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

    @Override
    public Integer getStride() {
        return stride;
    }

    public void setIteSource(String source) {
        if (!ElementParserUtil.isValidDataExpression(source)) {
            LOGGER.warn("[{}] The set ite-source being iterated over is invalid. source: {}", ExceptionEnum.BPMN_ATTRIBUTE_INVALID.getExceptionCode(), source);
            return;
        }
        this.source = source;
    }

    public void setIteStrategy(IterateStrategyEnum strategy) {
        this.strategy = strategy;
    }

    public void setStride(Integer stride) {
        if (stride != null && stride <= 0) {
            return;
        }
        this.stride = stride;
    }

    public void mergeProperty(ElementIterable elementIterable) {
        if (elementIterable == null) {
            return;
        }

        if (this.getIteStrategy() == null) {
            this.setIteStrategy(elementIterable.getIteStrategy());
        }
        if (this.getIteSource() == null) {
            this.setIteSource(elementIterable.getIteSource());
        }
        if (this.openAsync() == null) {
            this.setOpenAsync(elementIterable.openAsync());
        }
        if (this.getStride() == null) {
            this.setStride(elementIterable.getStride());
        }
    }
}
