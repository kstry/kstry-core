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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import cn.kstry.framework.core.constant.GlobalConstant;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;

/**
 *
 * @author lykan
 */
public class BasicKvAbility implements KvAbility {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicKvAbility.class);

    private static final Cache<String, Optional<Object>> kValueCache = CacheBuilder.newBuilder()
            .concurrencyLevel(8).initialCapacity(1024).maximumSize(50_000).expireAfterWrite(10, TimeUnit.MINUTES)
            .removalListener(notification -> LOGGER.info("kValue cache lose efficacy. key:{}, value:{}, cause:{}",
                    notification.getKey(), notification.getValue(), notification.getCause())).build();

    private final SerializerFeature[] SF = {SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect};

    private final KvSelector kvSelector;

    public BasicKvAbility(KvSelector kvSelector) {
        AssertUtil.notNull(kvSelector);
        this.kvSelector = kvSelector;
    }

    @Override
    public Optional<Object> getValue(String key) {
        if (StringUtils.isBlank(key)) {
            return Optional.empty();
        }
        KvThreadLocal.KvScope kvScope = KvThreadLocal.getKvScope().orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
        try {
            return kValueCache.get(getCacheKey(key, kvScope), () -> doGetValue(key, kvScope));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Object> getValueByScope(String scope, String key) {
        if (StringUtils.isBlank(key)) {
            return Optional.empty();
        }
        KvThreadLocal.KvScope kvScope = KvThreadLocal.getKvScope().orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
        KvThreadLocal.KvScope inKvScope = new KvThreadLocal.KvScope(scope);
        inKvScope.setBusinessId(kvScope.getBusinessId().orElse(null));
        try {
            return kValueCache.get(getCacheKey(key, inKvScope), () -> doGetValue(key, kvScope));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> Optional<T> getObject(String key, Class<T> clazz) {
        Optional<Object> valueOptional = getValue(key);
        return valueOptional.map(o -> JSON.parseObject(JSON.toJSONString(o, SF), clazz));
    }

    @Override
    public Optional<String> getString(String key) {
        Optional<Object> valueOptional = getValue(key);
        return valueOptional.map(v -> {
            if (v instanceof String) {
                return (String) v;
            }
            return JSON.toJSONString(v);
        });
    }

    @Override
    public <T> List<T> getList(String key, Class<T> clazz) {
        AssertUtil.notNull(clazz);
        Optional<Object> valueOptional = getValue(key);
        if (!valueOptional.isPresent() || !(valueOptional.get() instanceof Iterable)) {
            return Lists.newArrayList();
        }
        return JSON.parseArray(JSON.toJSONString(valueOptional.get(), SF), clazz);
    }

    private Optional<Object> doGetValue(String key, KvThreadLocal.KvScope kvScope) {
        AssertUtil.notNull(kvScope);
        if (StringUtils.isBlank(key)) {
            return Optional.empty();
        }
        if (!kvScope.getBusinessId().isPresent()) {
            return getScopeAndDefault(key, kvScope.getScope()).filter(v -> v != KValue.KV_NULL);
        }

        String businessId = kvScope.getBusinessId().orElseThrow(() -> KstryException.buildException(ExceptionEnum.SYSTEM_ERROR));
        if (!kvSelector.getKValue(businessId).isPresent()) {
            return getScopeAndDefault(key, kvScope.getScope()).filter(v -> v != KValue.KV_NULL);
        }

        String prefix = kvScope.getScope();
        Optional<Object> kValueOptional = getScopeAndDefault(prefix + "." + key, businessId);
        if(kValueOptional.isPresent()){
            return kValueOptional.filter(v -> v != KValue.KV_NULL);
        }
        return getScopeAndDefault(key, kvScope.getScope()).filter(v -> v != KValue.KV_NULL);
    }

    private Optional<Object> getScopeAndDefault(String key, String kvScope) {
        Optional<KValue> kValueOptional = kvSelector.getKValue(kvScope);
        if (kValueOptional.isPresent()) {
            Optional<Object> valueOptional = kValueOptional.get().getValue(key);
            if (valueOptional.isPresent()) {
                return valueOptional;
            }
        }
        if (!Objects.equals(kvScope, GlobalConstant.VARIABLE_SCOPE_DEFAULT)) {
            kValueOptional = kvSelector.getKValue(GlobalConstant.VARIABLE_SCOPE_DEFAULT);
            if (kValueOptional.isPresent()) {
                return kValueOptional.get().getValue(key);
            }
        }
        return Optional.empty();
    }

    private String getCacheKey(String key, KvThreadLocal.KvScope kvScope) {
        String businessId = kvScope.getBusinessId().orElse(StringUtils.EMPTY);
        String scope = kvScope.getScope();
        return key + "@" + businessId + "@" + scope;
    }
}
