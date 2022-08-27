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
package cn.kstry.framework.core.resource.config;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.net.URI;

/**
 * @author lykan
 */
public class AbstractConfigResource implements ConfigResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfigResource.class);

    /**
     * 配置文件名
     */
    private final String configName;

    /**
     * 配置文件 URI
     */
    private final URI configUri;

    public AbstractConfigResource(Resource resource) {
        AssertUtil.notNull(resource, ExceptionEnum.CONFIGURATION_RESOURCE_ERROR);
        try {
            this.configName = resource.getFilename();
            this.configUri = resource.getURI();
            InputStream inputStream = resource.getInputStream();
            AssertUtil.notBlank(configName, ExceptionEnum.CONFIGURATION_RESOURCE_ERROR);
            AssertUtil.anyNotNull(configUri, inputStream, ExceptionEnum.CONFIGURATION_RESOURCE_ERROR);
            LOGGER.info("Load bpmn resource. path: {}", getConfigUri());
            init(resource, inputStream);
            inputStream.close();
        } catch (KstryException e) {
            throw e;
        } catch (Throwable e) {
            throw ExceptionUtil.buildException(e, ExceptionEnum.CONFIGURATION_RESOURCE_ERROR, e.getMessage());
        }
    }

    /**
     * 使用 InputStream 初始化配置文件实例
     *
     * @param resource    资源
     * @param inputStream 输入流
     */
    public void init(Resource resource, InputStream inputStream) {
        // DO NOTHING
    }

    @Override
    public String getConfigName() {
        return configName;
    }

    @Override
    public URI getConfigUri() {
        return configUri;
    }
}
