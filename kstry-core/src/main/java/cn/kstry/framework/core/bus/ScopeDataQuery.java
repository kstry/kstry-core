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

import cn.kstry.framework.core.kv.KvScope;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * 用于暴露给使用方，作为查询 StoryBus 中数据的入口
 *
 * @author lykan
 */
public interface ScopeDataQuery {

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
     * 请求 ID
     *
     * @return 请求 ID
     */
    String getRequestId();

    /**
     * 获取开始事件 ID
     *
     * @return 开始事件 ID
     */
    String getStartId();

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
     * 获取服务节点属性，服务节点属性在节点定义时指定
     *
     * @return 服务节点属性
     */
    Optional<String> getTaskProperty();

    /**
     * 遍历执行迭代器的每一项数据时，获取当前项数据
     *
     * @return 数据结果
     */
    <T> Optional<T> iterDataItem();

    /**
     * 获取服务节点资源标识
     *
     * @return 服务节点属性
     */
    Optional<ServiceNodeResource> getServiceNodeResource();

    /**
     * 获取KvScope
     *
     * @return 服务节点属性
     */
    Optional<KvScope> getKvScope();

    /**
     * 拿到 StoryBus 中的读锁
     *
     * @return 读锁
     */
    ReentrantReadWriteLock.ReadLock readLock();

    /**
     * 串行化读
     */
    <T> Optional<T> serialRead(Function<ScopeDataQuery, T> readFun);
}
