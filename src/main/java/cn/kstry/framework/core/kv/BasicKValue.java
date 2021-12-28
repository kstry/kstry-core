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

import cn.kstry.framework.core.constant.GlobalConstant;
import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.resource.identity.BasicIdentity;
import cn.kstry.framework.core.util.KValueUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author lykan
 */
public class BasicKValue extends BasicIdentity implements KValue {

    private String scope;

    private BasicKValue parent;

    /**
     * 保存能力值 Map
     */
    private final Map<String, Object> abilityValueMap = new HashMap<>();

    public BasicKValue(String scope) {
        super(KValueUtil.getKValueIdentityId(scope), IdentityTypeEnum.K_V_ABILITY);
        this.scope = Optional.ofNullable(scope).filter(StringUtils::isNotBlank).orElse(GlobalConstant.VARIABLE_SCOPE_DEFAULT);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Object> getValue(String key) {
        if (StringUtils.isBlank(key)) {
            return Optional.empty();
        }

        String[] keySplit = key.split("\\.");
        boolean isContainsKey = abilityValueMap.containsKey(keySplit[0]);
        Object r = abilityValueMap.get(keySplit[0]);
        for (int i = 1; i < keySplit.length; i++) {
            if (!(r instanceof Map)) {
                r = null;
                break;
            }
            isContainsKey = ((Map<Object, Object>) r).containsKey(keySplit[i]);
            r = ((Map<Object, Object>) r).get(keySplit[i]);
        }
        if (r != null) {
            return Optional.of(r);
        }
        if (isContainsKey) {
            return Optional.of(KValue.KV_NULL);
        }
        return Optional.ofNullable(getParent()).flatMap(parent -> parent.getValue(key));
    }

    /**
     * 保存能力值
     *
     * @param key 不能为空，否则保存失败
     * @param value 不能为null，否则保存失败
     */
    public void addValue(String key, Object value) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        abilityValueMap.put(key, value);
    }

    public BasicKValue getParent() {
        return parent;
    }

    public void setParent(BasicKValue parent) {
        this.parent = parent;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
