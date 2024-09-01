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

import org.springframework.core.NamedThreadLocal;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Kstry中支持的ThreadLocal，Kstry框架内部发生线程切换时，KstryThreadLocal中的数据不会丢失
 * <p>
 * 注意：服务节点方法或拦截器等组件中，自定义的线程切换不会生效
 */
public class KstryThreadLocal<T> extends NamedThreadLocal<T> implements ThreadLocalSwitch<T> {

    private static final AtomicLong createCounter = new AtomicLong(0);

    public KstryThreadLocal() {
        this("KstryThreadLocal-" + createCounter.getAndIncrement());
    }

    public KstryThreadLocal(String name) {
        super(name);
    }

    @Override
    public String getName() {
        return super.toString();
    }

    public static <S> KstryThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
        return new KstrySuppliedThreadLocal<>(supplier);
    }

    static final class KstrySuppliedThreadLocal<T> extends KstryThreadLocal<T> {

        private final Supplier<? extends T> supplier;

        KstrySuppliedThreadLocal(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        protected T initialValue() {
            return supplier.get();
        }
    }
}
