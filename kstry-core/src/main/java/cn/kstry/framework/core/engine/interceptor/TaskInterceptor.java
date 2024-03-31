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
package cn.kstry.framework.core.engine.interceptor;

import org.springframework.core.Ordered;

/**
 * 任务拦截器，每一个被服务节点被调用时都将先通过拦截器
 */
public interface TaskInterceptor extends Ordered {

    /**
     * 判断当前拦截器是否生效
     *
     * @param iterData 数据集
     * @return 是否匹配成功，默认：true
     */
    default boolean match(IterData iterData) {
        return true;
    }

    /**
     * 调用目标任务
     *
     * @param iter 拦截器链
     * @return 执行结果
     */
    Object invoke(Iter iter);

    @Override
    default int getOrder() {
        return 0;
    }
}
