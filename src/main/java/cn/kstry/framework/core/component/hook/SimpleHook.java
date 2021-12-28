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
public class SimpleHook<T> implements Hook<T> {

    private Consumer<T> hook;

    private final T target;

    public SimpleHook(T target) {
        AssertUtil.notNull(target);
        this.target = target;
    }

    @Override
    public void hook(Consumer<T> hook) {
        AssertUtil.notNull(hook);
        if (this.hook == null) {
            this.hook = hook;
        } else {
            this.hook = hook.andThen(this.hook);
        }
    }

    @Override
    public Consumer<T> getHook() {
        return this.hook;
    }

    @Override
    public T getTarget() {
        return this.target;
    }
}
