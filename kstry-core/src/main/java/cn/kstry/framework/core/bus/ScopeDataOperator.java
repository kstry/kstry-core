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

import cn.kstry.framework.core.util.GlobalUtil;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 用于暴露给使用方，作为操作 StoryBus 中数据的入口
 *
 * @author lykan
 */
public interface ScopeDataOperator extends ScopeDataQuery {

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
     * 设置数据
     *
     * @param expression 取值表达式
     * @param target 变量值
     *
     * @return 是否设置成功，如果已经存在将会设置失败
     */
    boolean setData(String expression, Object target);

    /**
     * 删除数据
     *
     * @param expression 取值表达式
     *
     * @return 是否删除成功
     */
    boolean removeData(String expression);

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
     * 拿到 StoryBus 中的写锁
     *
     * @return 写锁
     */
    ReentrantReadWriteLock.WriteLock writeLock();

    /**
     * 串行化写
     */
    default <T> Optional<T> serialWrite(Function<ScopeDataQuery, T> writeFun) {
        if (writeFun == null) {
            return Optional.empty();
        }
        ReentrantReadWriteLock.WriteLock writeLock = writeLock();
        writeLock.lock();
        try {
            return GlobalUtil.resOptional(writeFun.apply(this));
        } finally {
            writeLock.unlock();
        }
    }
}
