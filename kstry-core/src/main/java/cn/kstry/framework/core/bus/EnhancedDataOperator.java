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
package cn.kstry.framework.core.bus;

import java.util.Optional;

/**
 * 增强型 ScopeDataOperator
 *
 * @author lykan
 */
public interface EnhancedDataOperator extends ScopeDataOperator {

    /**
     * 获取 Result 对象
     *
     * @return 数据结果
     */
    <T> Optional<T> getResult(String typeConverter);

    /**
     * 从 Req 域获取数据
     *
     * @param name 数据名称
     * @return 数据结果
     */
    <T> Optional<T> getReqData(String name, String typeConverter);

    /**
     * 从 Sta 域获取数据
     *
     * @param name 数据名称
     * @return 数据结果
     */
    <T> Optional<T> getStaData(String name, String typeConverter);

    /**
     * 从 Var 域获取数据
     *
     * @param name 数据名称
     * @return 数据结果
     */
    <T> Optional<T> getVarData(String name, String typeConverter);

    /**
     * 使用取值表达式获取数据
     *
     * @param expression 取值表达式
     * @return 数据结果
     */
    <T> Optional<T> getData(String expression, String typeConverter);

    /**
     * 获取服务节点属性，服务节点属性在节点定义时指定
     *
     * @return 服务节点属性
     */
    <T> Optional<T> getTaskProperty(String typeConverter);

    /**
     * 遍历执行迭代器的每一项数据时，获取当前项数据
     *
     * @return 数据结果
     */
    <T> Optional<T> iterDataItem(String typeConverter);

    /**
     * 获取 Result 对象
     *
     * @return 数据结果
     */
    <T> Optional<T> getResult(Class<T> targetClass);

    /**
     * 从 Req 域获取数据
     *
     * @param name 数据名称
     * @return 数据结果
     */
    <T> Optional<T> getReqData(String name, Class<T> targetClass);

    /**
     * 从 Sta 域获取数据
     *
     * @param name 数据名称
     * @return 数据结果
     */
    <T> Optional<T> getStaData(String name, Class<T> targetClass);

    /**
     * 从 Var 域获取数据
     *
     * @param name 数据名称
     * @return 数据结果
     */
    <T> Optional<T> getVarData(String name, Class<T> targetClass);

    /**
     * 使用取值表达式获取数据
     *
     * @param expression 取值表达式
     * @return 数据结果
     */
    <T> Optional<T> getData(String expression, Class<T> targetClass);

    /**
     * 获取服务节点属性，服务节点属性在节点定义时指定
     *
     * @return 服务节点属性
     */
    <T> Optional<T> getTaskProperty(Class<T> targetClass);

    /**
     * 遍历执行迭代器的每一项数据时，获取当前项数据
     *
     * @return 数据结果
     */
    <T> Optional<T> iterDataItem(Class<T> targetClass);
}
