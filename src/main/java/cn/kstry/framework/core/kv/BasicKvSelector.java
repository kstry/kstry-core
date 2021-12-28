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
package cn.kstry.framework.core.kv;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

/**
 *
 * @author lykan
 */
public class BasicKvSelector implements KvSelector {

    private final Map<String, KValue> kValueMap = Maps.newConcurrentMap();

    @Override
    public Optional<KValue> getKValue(String scope) {
        if (StringUtils.isBlank(scope)) {
            return Optional.empty();
        }
        return Optional.ofNullable(kValueMap.get(scope));
    }

    public void addKValue(BasicKValue kValue) {
        if (kValue == null) {
            return;
        }
        kValueMap.put(kValue.getScope(), kValue);
    }
}
