/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.component.hook;

import cn.kstry.framework.core.util.AssertUtil;

import java.util.function.Consumer;

/**
 *
 * @author lykan
 */
public interface Hook<T> {

    /**
     * 设置 钩子操作
     *
     * @param hook 钩子操作
     */
    void hook(Consumer<T> hook);

    /**
     * 拿到钩子操作
     *
     * @return 钩子操作
     */
    Consumer<T> getHook();

    /**
     * 拿到目标对象，即钩子的执行者
     *
     * @return 目标对象
     */
    T getTarget();

    /**
     * 触发钩子操作
     */
    default void trigger() {
        if (getHook() == null) {
            return;
        }
        T t = getTarget();
        AssertUtil.notNull(t);
        getHook().accept(t);
    }
}
