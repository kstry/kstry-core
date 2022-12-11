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

import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;

import java.util.List;
import java.util.function.Supplier;

public class Iter {

    private final Supplier<Object> supplier;

    private final ScopeDataOperator scopeDataOperator;

    private final List<TaskInterceptor> taskInterceptors;

    private final ServiceNodeResource serviceNodeResource;

    private int index;

    public Iter(Supplier<Object> supplier, ScopeDataOperator scopeDataOperator, List<TaskInterceptor> taskInterceptors, ServiceNodeResource serviceNodeResource) {
        this.supplier = supplier;
        this.scopeDataOperator = scopeDataOperator;
        this.taskInterceptors = taskInterceptors;
        this.serviceNodeResource = serviceNodeResource;
        this.index = 0;
    }

    public Object next() {
        if (index < taskInterceptors.size()) {
            return taskInterceptors.get(index++).invoke(this);
        }
        return supplier.get();
    }

    public ServiceNodeResource getServiceNode() {
        return serviceNodeResource;
    }

    public ScopeDataOperator getDataOperator() {
        return scopeDataOperator;
    }
}
