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

import cn.kstry.framework.core.engine.thread.InvokeMethodThreadLocal;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.kv.KvScope;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.PropertyUtil;
import org.apache.commons.lang3.StringUtils;

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
     * 逐级解析对象字段，可以获取JSON字符串中的数据
     *
     * @param expression 取值表达式
     * @return 数据结果
     */
    @SuppressWarnings("unchecked")
    default <T> Optional<T> getRawData(String expression) {
        if (StringUtils.isBlank(expression)) {
            return Optional.empty();
        }
        if (!ElementParserUtil.isValidDataExpression(expression)) {
            return Optional.empty();
        }
        expression = StringUtils.replaceOnce(expression, "$.", StringUtils.EMPTY);
        String[] expArr = expression.split("\\.", 2);
        String key = (expArr.length == 2) ? expArr[1] : null;
        ScopeTypeEnum scopeTypeEnum = ScopeTypeEnum.of(expArr[0]).orElse(null);
        if (scopeTypeEnum == null) {
            return Optional.empty();
        }
        return (Optional<T>) serialRead(query -> {
            if (scopeTypeEnum == ScopeTypeEnum.RESULT) {
                return PropertyUtil.getRawProperty(getResult().orElse(null), key).map(r -> (T) r);
            }
            if (scopeTypeEnum == ScopeTypeEnum.STABLE) {
                return PropertyUtil.getRawProperty(getStaScope(), key).map(r -> (T) r);
            }
            if (scopeTypeEnum == ScopeTypeEnum.VARIABLE) {
                return PropertyUtil.getRawProperty(getVarScope(), key).map(r -> (T) r);
            }
            if (scopeTypeEnum == ScopeTypeEnum.REQUEST) {
                return PropertyUtil.getRawProperty(getReqScope(), key).map(r -> (T) r);
            }
            throw ExceptionUtil.buildException(null, ExceptionEnum.STORY_ERROR, null);
        });
    }

    /**
     * 获取服务节点属性，服务节点属性在节点定义时指定
     *
     * @return 服务节点属性
     */
    Optional<String> getTaskProperty();

    /**
     * 获取当前循环次数，如果主流程循环3次，子流程循环2次，该方法在主流程的服务节点返回3，在子流程服务节点返回2
     *
     * @return 服务节点属性
     */
    default long getCycleTimes() {
        return InvokeMethodThreadLocal.getCycleTimes().filter(t -> t > 0).orElse(1L);
    }

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
    default <T> Optional<T> serialRead(Function<ScopeDataQuery, T> readFun) {
        if (readFun == null) {
            return Optional.empty();
        }
        ReentrantReadWriteLock.ReadLock readLock = readLock();
        readLock.lock();
        try {
            return GlobalUtil.resOptional(readFun.apply(this));
        } finally {
            readLock.unlock();
        }
    }
}
