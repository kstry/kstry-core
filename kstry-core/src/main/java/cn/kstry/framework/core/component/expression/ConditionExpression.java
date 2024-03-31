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
package cn.kstry.framework.core.component.expression;

import cn.kstry.framework.core.bus.StoryBus;
import org.springframework.core.Ordered;

/**
 * @author lykan
 */
public interface ConditionExpression extends Ordered {

    /**
     * 计算表达式获取结果
     *
     * @param storyBus scopeData
     * @return 表达式结果
     */
    boolean condition(StoryBus storyBus);

    /**
     * 根据表达式判断是否需要使用该类型表达式
     *
     * @return 匹配结果
     */
    boolean match(String expression);

    /**
     * 构建真实参与计算的条件表达式
     *
     * @param aliasParser 别名解析器
     */
    void parseConditionExpression(ExpressionAliasParser aliasParser);

    /**
     * 获取未加工的，最初的条件表达式
     */
    String getPlainExpression();
}
