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
package cn.kstry.framework.core.bus;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * 用于暴露给使用方，作为操作 StoryBus 中数据的入口
 *
 * @author lykan
 */
public interface ScopeDataOperator {

    /**
     * 获取 Req 对象
     *
     * @return 数据结果
     */
    <T> T getReqScope();

    /**
     * 获取 Sta 对象
     *
     * @return 数据结果
     */
    <T extends ScopeData> T getStaScope();

    /**
     * 获取 Var 对象
     *
     * @return 数据结果
     */
    <T extends ScopeData> T getVarScope();

    /**
     * 获取 Result 对象
     *
     * @return 数据结果
     */
    <T> Optional<T> getResult();

    /**
     * 获取业务 ID
     *
     * @return 业务 ID
     */
    Optional<String> getBusinessId();

    /**
     * 从 Req 域获取数据
     *
     * @param name 数据名称
     * @return 数据结果
     */
    <T> Optional<T> getReqData(String name);

    /**
     * 从 Sta 域获取数据
     *
     * @param name 数据名称
     * @return 数据结果
     */
    <T> Optional<T> getStaData(String name);

    /**
     * 从 Var 域获取数据
     *
     * @param name 数据名称
     * @return 数据结果
     */
    <T> Optional<T> getVarData(String name);

    /**
     * 使用取值表达式获取数据
     *
     * @param expression 取值表达式
     * @return 数据结果
     */
    <T> Optional<T> getData(String expression);

    /**
     * 获取任务节点属性，任务节点属性在节点定义时指定
     *
     * @return 任务节点属性
     */
    Optional<String> getTaskProperty();

    /**
     * 使用取值表达式获取数据, 如果不存在会创建并赋值到表达式指定位置，创建失败或指定位置失败返回空值
     *
     * @param expression 取值表达式
     * @param supplier 没有获取到目标值时调用获取默认值
     *
     * @return 数据结果
     */
    <T> Optional<T> computeIfAbsent(String expression, Supplier<T> supplier);

    /**
     * 遍历执行迭代器的每一项数据时，获取当前项数据
     *
     * @return 数据结果
     */
    <T> Optional<T> iterDataItem();

    /**
     * 设置 Sta 域变量
     *
     * @param name 变量名
     * @param target 变量值
     *
     * @return 是否设置成功，如果已经存在将会设置失败
     */
    boolean setStaData(String name, Object target);

    /**
     * 设置 Var 域变量
     *
     * @param name 变量名
     * @param target 变量值
     *
     * @return 是否设置成功
     */
    boolean setVarData(String name, Object target);

    /**
     * 设置 Result
     *
     * @param target Result对象
     * @return 是否设置成功，如果已经存在将会设置失败
     */
    boolean setResult(Object target);

    /**
     * 拿到 StoryBus 中的读锁
     *
     * @return 读锁
     */
    ReentrantReadWriteLock.ReadLock readLock();

    /**
     * 拿到 StoryBus 中的写锁
     *
     * @return 写锁
     */
    ReentrantReadWriteLock.WriteLock writeLock();
}
