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
package cn.kstry.framework.core.engine.interceptor;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Iter extends IterData {

    private final Supplier<Object> supplier;

    private final List<TaskInterceptor> taskInterceptors;

    private int index;

    public Iter(Supplier<Object> supplier, IterData iterData, List<TaskInterceptor> taskInterceptors) {
        super(iterData.getDataOperator(), iterData.getServiceNode(), iterData.getRole());
        this.supplier = supplier;
        this.taskInterceptors = taskInterceptors.stream().filter(t -> t.match(iterData)).collect(Collectors.toList());
        this.index = 0;
    }

    public Object next() {
        if (index < taskInterceptors.size()) {
            return taskInterceptors.get(index++).invoke(this);
        }
        return supplier.get();
    }
}
