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
package cn.kstry.framework.test.subprocess.interceptor;

import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.engine.interceptor.SubProcessIdentity;
import cn.kstry.framework.core.engine.interceptor.SubProcessInterceptor;
import cn.kstry.framework.core.role.Role;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author lykan
 */
@Slf4j
@Component
public class SayHello02Interceptor implements SubProcessInterceptor {

    @Override
    public Set<SubProcessIdentity> getSubProcessIdentity() {
        return Sets.newHashSet(new SubProcessIdentity("Event_0fecx97"), new SubProcessIdentity("Event_06ro2iz"));
    }

    @Override
    public boolean beforeProcessor(ScopeDataOperator dataOperator, Role role) {
        Optional<AtomicInteger> staDataOptional = dataOperator.getStaData("tbo.f");
        AtomicInteger integer = staDataOptional.orElseThrow(RuntimeException::new);
        integer.getAndIncrement();
        return true;
    }

    @Override
    public void afterProcessor(ScopeDataOperator dataOperator, Role role) {
        Optional<AtomicInteger> staDataOptional = dataOperator.getStaData("tbo.f");
        AtomicInteger integer = staDataOptional.orElseThrow(RuntimeException::new);
        integer.getAndIncrement();
    }

    @Override
    public void errorProcessor(Throwable exception, ScopeDataOperator dataOperator, Role role) {
        Optional<AtomicInteger> staDataOptional = dataOperator.getStaData("tbo.f");
        AtomicInteger integer = staDataOptional.orElseThrow(RuntimeException::new);
        integer.getAndIncrement();
    }

    @Override
    public void finallyProcessor(ScopeDataOperator dataOperator, Role role) {
        Optional<AtomicInteger> staDataOptional = dataOperator.getStaData("tbo.f");
        AtomicInteger integer = staDataOptional.orElseThrow(RuntimeException::new);
        integer.getAndIncrement();
    }
}
