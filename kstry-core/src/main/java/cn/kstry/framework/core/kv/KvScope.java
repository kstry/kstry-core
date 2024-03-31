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
package cn.kstry.framework.core.kv;

import cn.kstry.framework.core.constant.GlobalConstant;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class KvScope {

    private final String scope;

    private String businessId;

    public KvScope(String scope) {
        this.scope = Optional.ofNullable(scope).filter(StringUtils::isNotBlank).orElse(GlobalConstant.VARIABLE_SCOPE_DEFAULT);
    }

    public KvScope(String scope, String businessId) {
        this(scope);
        this.businessId = businessId;
    }

    public String getScope() {
        return scope;
    }

    public Optional<String> getBusinessId() {
        return Optional.ofNullable(businessId).filter(StringUtils::isNotBlank);
    }
}
