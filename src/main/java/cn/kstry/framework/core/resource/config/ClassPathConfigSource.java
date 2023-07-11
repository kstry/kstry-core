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
package cn.kstry.framework.core.resource.config;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 类路径配置来源
 *
 * @author lykan
 */
public abstract class ClassPathConfigSource implements ConfigSource {

    /**
     * 配置路径
     */
    private final String configName;

    public ClassPathConfigSource(String configName) {
        AssertUtil.notBlank(configName);
        this.configName = configName;
    }

    protected List<Resource> getResourceList() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources;
        try {
            resources = Stream.of(configName).flatMap(c -> Stream.of(c.split(","))).flatMap(c -> {
                try {
                    return Stream.of(resolver.getResources(c));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        } catch (Throwable e) {
            throw ExceptionUtil.buildException(e, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, null);
        }
        if (CollectionUtils.isEmpty(resources)) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(resources);
    }
}
