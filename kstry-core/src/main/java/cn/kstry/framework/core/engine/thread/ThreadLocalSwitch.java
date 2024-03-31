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
package cn.kstry.framework.core.engine.thread;

/**
 * 实现该接口的ThreadLocal，在Kstry框架内部发生线程切换时，ThreadLocal中的数据不会丢失
 *
 * 注意：服务节点方法或拦截器等组件中，自定义的线程切换不会生效
 */
public interface ThreadLocalSwitch<T> {

    /**
     * 获取当前ThreadLocal的值
     */
    T get();

    /**
     * 设置当前ThreadLocal的值
     */
    void set(T value);

    /**
     * 移除当前ThreadLocal的值
     */
    void remove();
}
