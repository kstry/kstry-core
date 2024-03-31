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
package cn.kstry.framework.core.component.dynamic;

import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.component.dynamic.creator.*;
import cn.kstry.framework.core.enums.DynamicComponentType;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.TaskServiceUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class SpringDynamicComponent<Component> implements DynamicComponent<Component> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringDynamicComponent.class);

    private static final Map<DynamicComponentType, Class<? extends DynamicComponentCreator<?>>> COMPONENT_TYPE_CREATOR_MAPPING = Maps.newHashMap();

    static {
        COMPONENT_TYPE_CREATOR_MAPPING.put(DynamicComponentType.ROLE, DynamicRole.class);
        COMPONENT_TYPE_CREATOR_MAPPING.put(DynamicComponentType.SUB_PROCESS, DynamicSubProcess.class);
        COMPONENT_TYPE_CREATOR_MAPPING.put(DynamicComponentType.PROCESS, DynamicProcess.class);
        COMPONENT_TYPE_CREATOR_MAPPING.put(DynamicComponentType.K_VALUE, DynamicKValue.class);
    }

    private final Cache<String, Component> componentCache;

    private final List<DynamicComponentCreator<Component>> componentCreatorList;

    @SuppressWarnings("all")
    public SpringDynamicComponent(long maxCacheSize, ApplicationContext applicationContext) {
        this.componentCache = buildCache(maxCacheSize);
        Map<String, DynamicComponentCreator> creatorBeanMap = applicationContext.getBeansOfType(DynamicComponentCreator.class);
        if (MapUtils.isEmpty(creatorBeanMap)) {
            this.componentCreatorList = ImmutableList.of();
        } else {
            List<DynamicComponentCreator<Component>> creatorList = creatorBeanMap.values().stream().map(obj -> (DynamicComponentCreator<Component>) obj)
                    .filter(creator -> ElementParserUtil.isAssignable(COMPONENT_TYPE_CREATOR_MAPPING.get(getComponentType()), creator.getClass())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(creatorList)) {
                this.componentCreatorList = ImmutableList.of();
            } else {
                this.componentCreatorList = Collections.unmodifiableList(creatorList);
            }
        }
    }

    @Override
    public Optional<Component> dynamicGetComponent(String defKey, Object param, ScopeDataQuery scopeDataQuery) {
        if ((scopeDataQuery == null && StringUtils.isBlank(defKey)) || CollectionUtils.isEmpty(componentCreatorList)) {
            return Optional.empty();
        }

        Set<String> keySet = Sets.newHashSet();
        List<Component> componentList = componentCreatorList.stream().map(creator -> {
            String key = StringUtils.isNotBlank(defKey) ? defKey : creator.getKey(scopeDataQuery);
            if (StringUtils.isBlank(key)) {
                return null;
            }

            long version = -1;
            if (creator instanceof ComponentCacheSupport) {
                version = GlobalUtil.transferNotEmpty(creator, ComponentCacheSupport.class).version(key);
            }
            if (version < 0) {
                Component component = creator.getComponent(key, param).flatMap(c -> SpringDynamicComponent.this.checkDecorateComponent(c, param)).orElse(null);
                if (component != null) {
                    keySet.add(key);
                }
                return component;
            }

            String cacheKey = TaskServiceUtil.joinVersion(key, version);
            Component compCache = componentCache.getIfPresent(cacheKey);
            if (compCache != null) {
                keySet.add(cacheKey);
                return compCache;
            }
            synchronized (SpringDynamicComponent.this) {
                compCache = componentCache.getIfPresent(cacheKey);
                if (compCache != null) {
                    keySet.add(cacheKey);
                    return compCache;
                }

                Optional<Component> componentOptional = creator.getComponent(key, param);
                componentOptional.flatMap(c -> SpringDynamicComponent.this.checkDecorateComponent(c, param)).ifPresent(component -> {
                    keySet.add(cacheKey);
                    componentCache.put(cacheKey, component);
                });
                return componentOptional.orElse(null);
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(componentList)) {
            return Optional.empty();
        }
        AssertUtil.oneSize(componentList, ExceptionEnum.COMPONENT_DUPLICATION_ERROR, "Duplicate components when getting dynamic components. type: {}, keys: {}", getComponentType(), keySet);
        return Optional.of(componentList.get(0));
    }

    /**
     * 对获取的最新组件进行校验和装饰，如果存在非法的地方，将返回空
     *
     * @param component 最新组件
     * @param param 获取组件时传入的参数
     * @return 装饰组件结果
     */
    public Optional<Component> checkDecorateComponent(Component component, Object param) {
        if (component == null) {
            return Optional.empty();
        }
        return Optional.of(component);
    }

    private Cache<String, Component> buildCache(long maxCacheSize) {
        if (maxCacheSize <= 0) {
            return null;
        }
        return CacheBuilder.newBuilder()
                .concurrencyLevel(8).initialCapacity(1024).maximumSize(maxCacheSize).expireAfterWrite(1, TimeUnit.DAYS)
                .removalListener(notification -> LOGGER.info("dynamic component cache lose efficacy. key: {}, value: {}, cause: {}",
                        notification.getKey(), notification.getValue(), notification.getCause())).build();
    }
}
