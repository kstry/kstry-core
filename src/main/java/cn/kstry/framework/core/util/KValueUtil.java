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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.constant.GlobalConstant;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.kv.BasicKValue;
import cn.kstry.framework.core.kv.KvScope;
import cn.kstry.framework.core.resource.config.Config;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.resource.config.PropertiesConfig;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.SingletonBeanRegistry;

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

    public static KvScope getKvScope(String scope) {
        AssertUtil.notBlank(scope);
        String[] split = scope.split("@");
        AssertUtil.isTrue(split.length > 0 && split.length <= 2, ExceptionEnum.KV_SCOPE_PARSING_ERROR,
                ExceptionEnum.KV_SCOPE_PARSING_ERROR.getDesc() + " scope: {}", scope);
        if (split.length == 1) {
            return new KvScope(split[0], null);
        }
        return new KvScope(split[0], split[1]);
    }

    public static void initKValue(List<ConfigResource> propConfigs, List<String> activeProfiles, SingletonBeanRegistry beanFactory) {
        AssertUtil.notNull(activeProfiles);
        AssertUtil.notNull(beanFactory);
        if (CollectionUtils.isEmpty(propConfigs)) {
            return;
        }

        Map<String, BasicKValue> kValueMap = Maps.newHashMap();
        propConfigs.forEach(propConfig -> {
            List<Config> configList = propConfig.getConfigList();
            configList.stream().map(config -> GlobalUtil.transferNotEmpty(config, PropertiesConfig.class)).forEach(config -> {
                List<BasicKValue> kValueList = config.getKValueList();
                kValueList.forEach(kValue -> {
                    KvScope kvScope = KValueUtil.getKvScope(kValue.getScope());
                    if (StringUtils.isNotBlank(kvScope.getActiveProfile()) && !activeProfiles.contains(kvScope.getActiveProfile())) {
                        return;
                    }
                    kValueMap.put(kValue.getScope(), kValue);
                });
            });
        });

        Maps.newHashMap(kValueMap).forEach((k, v) -> {
            KvScope kvScope = KValueUtil.getKvScope(k);
            if (StringUtils.isNotBlank(kvScope.getActiveProfile())) {
                BasicKValue parentKValue = kValueMap.get(kvScope.getScope());
                if (parentKValue == null) {
                    return;
                }
                v.setParent(parentKValue);
                kValueMap.remove(kvScope.getScope());
            }
        });

        List<String> collect = kValueMap.keySet().stream().map(KValueUtil::getKvScope).map(KvScope::getScope)
                .collect(Collectors.toMap(e -> e, e -> 1, Integer::sum)).entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey)
                .collect(Collectors.toList());
        AssertUtil.isEmpty(collect, ExceptionEnum.KV_SCOPE_PARSING_ERROR,
                "kv scope cannot be defined repeatedly even in different files! scope: {}", collect);
        kValueMap.forEach((k, v) -> {
            KvScope kvScope = KValueUtil.getKvScope(k);
            v.setScope(kvScope.getScope());
            beanFactory.registerSingleton(GlobalUtil.format(GlobalConstant.KV_SCOPE_DEFAULT_BEAN_NAME, kvScope.getScope()), v);
        });
    }
}
