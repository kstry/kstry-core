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
package cn.kstry.framework.core.resource.config;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.google.common.collect.Lists;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;

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
        Resource[] resources;
        try {
            resources = resolver.getResources(configName);
        } catch (Exception e) {
            throw KstryException.buildException(e, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, null);
        }
        if (resources == null || resources.length == 0) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(resources);
    }
}
