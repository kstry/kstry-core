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
import cn.kstry.framework.core.role.Role;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.OrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class TaskInterceptorRepository {

    /**
     * 任务拦截器集合
     */
    private final List<TaskInterceptor> taskInterceptors;

    public TaskInterceptorRepository(Collection<TaskInterceptor> taskInterceptors) {
        List<TaskInterceptor> taskInterceptorList = new ArrayList<>(taskInterceptors);
        OrderComparator.sort(taskInterceptorList);
        this.taskInterceptors = ImmutableList.copyOf(taskInterceptorList);
    }

    public Object process(Supplier<Object> supplier, ServiceNodeResource serviceNodeResource, ScopeDataOperator scopeDataOperator, Role role) {
        if (CollectionUtils.isEmpty(taskInterceptors)) {
            return supplier.get();
        }
        return new Iter(supplier, new IterData(scopeDataOperator, serviceNodeResource, role), taskInterceptors).next();
    }
}
