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
package cn.kstry.framework.core.engine.thread;

import java.util.Optional;

/**
 *
 * @author lykan
 */
public class IteratorThreadLocal {

    private static final ThreadLocal<Object> ITERATOR_THREAD_LOCAL = new ThreadLocal<>();

    public static void setDataItem(Object data) {
        if (data != null) {
            ITERATOR_THREAD_LOCAL.set(data);
        }
    }

    public static Optional<Object> getDataItem() {
        return Optional.ofNullable(ITERATOR_THREAD_LOCAL.get());
    }

    public static void clear() {
        ITERATOR_THREAD_LOCAL.remove();
    }
}
