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
package cn.kstry.framework.core.resource.factory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Lists;

import cn.kstry.framework.core.enums.ResourceTypeEnum;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.resource.config.ConfigSource;
import cn.kstry.framework.core.util.AssertUtil;

/**
 * @author lykan
 */
public abstract class BasicResourceFactory<R> implements ResourceFactory<R> {

    /**
     * Spring 环境上下文
     */
    protected final ApplicationContext applicationContext;

    public BasicResourceFactory(ApplicationContext applicationContext) {
        AssertUtil.notNull(applicationContext);
        this.applicationContext = applicationContext;
    }

    protected List<ConfigResource> getConfigResource(ResourceTypeEnum resourceType) {
        AssertUtil.notNull(resourceType);
        Map<String, ConfigSource> configSourceMap = applicationContext.getBeansOfType(ConfigSource.class);
        if (MapUtils.isEmpty(configSourceMap)) {
            return Lists.newArrayList();
        }
        List<ConfigSource> configSource = configSourceMap.values().stream().filter(source -> source.getResourceType() == resourceType).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(configSource)) {
            return Lists.newArrayList();
        }
        return configSource.stream().flatMap(source -> source.getConfigResourceList().stream()).collect(Collectors.toList());
    }
}
