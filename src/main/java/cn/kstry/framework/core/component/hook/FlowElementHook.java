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

import cn.kstry.framework.core.bpmn.AsyncFlowElement;
import cn.kstry.framework.core.bpmn.impl.FlowElementImpl;

import java.util.function.Consumer;

/**
 *
 * @author lykan
 */
public class FlowElementHook<T> extends FlowElementImpl implements Hook<T>, AsyncFlowElement {

    private final SimpleHook<T> simpleHook;

    public FlowElementHook(T target) {
        this.simpleHook = new SimpleHook<>(target);
    }

    @Override
    public void hook(Consumer<T> hook) {
        simpleHook.hook(hook);
    }

    @Override
    public Consumer<T> getHook() {
        return simpleHook.getHook();
    }

    @Override
    public T getTarget() {
        return simpleHook.getTarget();
    }

    @Override
    public boolean openAsync() {
        return true;
    }
}
