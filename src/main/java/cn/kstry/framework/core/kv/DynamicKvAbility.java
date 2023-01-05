/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.kv;

import cn.kstry.framework.core.component.dynamic.KValueDynamicComponent;

import java.util.Optional;

public class DynamicKvAbility extends BasicKvAbility {

    private final KValueDynamicComponent kValueDynamicComponent;

    public DynamicKvAbility(KvSelector kvSelector, KValueDynamicComponent kValueDynamicComponent) {
        super(kvSelector);
        this.kValueDynamicComponent = kValueDynamicComponent;
    }

    @Override
    protected Optional<Object> doGetValueEmptySign(String key, KvScope kvScope) {
        Optional<Object> resultOptional = super.doGetValueEmptySign(key, kvScope);
        if (resultOptional.isPresent()) {
            return resultOptional.filter(v -> v != KValue.KV_NULL);
        }
        return kValueDynamicComponent.dynamicGetComponent(key, kvScope, null);
    }
}
