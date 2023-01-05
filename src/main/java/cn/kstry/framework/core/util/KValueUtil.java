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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.constant.GlobalConstant;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.kv.BasicKValue;
import cn.kstry.framework.core.kv.KValue;
import cn.kstry.framework.core.kv.KvScopeProfile;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * KValueUtil
 *
 * @author lykan
 */
public class KValueUtil {

    public static String getKValueIdentityId(String scope) {
        if (StringUtils.isBlank(scope)) {
            scope = GlobalConstant.VARIABLE_SCOPE_DEFAULT;
        }
        return scope;
    }

    public static KvScopeProfile getKvScopeProfile(String scope) {
        AssertUtil.notBlank(scope);
        String[] split = scope.split("@");
        AssertUtil.isTrue(split.length > 0 && split.length <= 2, ExceptionEnum.KV_SCOPE_PARSING_ERROR, ExceptionEnum.KV_SCOPE_PARSING_ERROR.getDesc() + " scope: {}", scope);
        if (split.length == 1) {
            return new KvScopeProfile(split[0], null);
        }
        return new KvScopeProfile(split[0], split[1]);
    }

    public static Map<String, BasicKValue> getKValueMap(List<KValue> kValueList, List<String> activeProfiles) {
        AssertUtil.notNull(activeProfiles);
        if (CollectionUtils.isEmpty(kValueList)) {
            return Maps.newHashMap();
        }

        List<String> scopeNameList = Lists.newArrayList();
        Map<String, BasicKValue> kValueMap = Maps.newHashMap();
        kValueList.stream().map(kv -> GlobalUtil.transferNotEmpty(kv, BasicKValue.class)).forEach(kValue -> {
            scopeNameList.add(kValue.getScope());
            KvScopeProfile kvScopeProfile = KValueUtil.getKvScopeProfile(kValue.getScope());
            if (StringUtils.isNotBlank(kvScopeProfile.getActiveProfile()) && !activeProfiles.contains(kvScopeProfile.getActiveProfile())) {
                return;
            }
            kValueMap.put(kValue.getScope(), kValue);
        });
        List<String> dKeys = scopeNameList.stream().collect(Collectors.toMap(e -> e, e -> 1, Integer::sum))
                .entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
        AssertUtil.isEmpty(dKeys, ExceptionEnum.KV_SCOPE_PARSING_ERROR, "kv scope cannot be defined repeatedly even in different files! scope: {}", dKeys);

        Maps.newHashMap(kValueMap).forEach((k, v) -> {
            KvScopeProfile kvScopeProfile = KValueUtil.getKvScopeProfile(k);
            if (StringUtils.isNotBlank(kvScopeProfile.getActiveProfile())) {
                BasicKValue parentKValue = kValueMap.get(kvScopeProfile.getScope());
                if (parentKValue == null) {
                    return;
                }
                v.setParent(parentKValue);
                kValueMap.remove(kvScopeProfile.getScope());
            }
        });
        List<String> collect = kValueMap.keySet().stream().map(KValueUtil::getKvScopeProfile).map(KvScopeProfile::getScope)
                .collect(Collectors.toMap(e -> e, e -> 1, Integer::sum)).entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
        AssertUtil.isEmpty(collect, ExceptionEnum.KV_SCOPE_PARSING_ERROR, "kv scope cannot be defined repeatedly even in different files! scope: {}", collect);
        kValueMap.forEach((k, v) -> {
            KvScopeProfile kvScopeProfile = KValueUtil.getKvScopeProfile(k);
            v.setScope(kvScopeProfile.getScope());
        });
        return kValueMap;
    }
}
