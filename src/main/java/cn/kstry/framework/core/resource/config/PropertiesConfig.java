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

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.kv.BasicKValue;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

/**
 *
 * @author lykan
 */
public class PropertiesConfig extends BasicConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfig.class);

    private List<BasicKValue> kValueList;

    public PropertiesConfig(Resource resource) {
        try {
            AssertUtil.notNull(resource, ExceptionEnum.CONFIGURATION_RESOURCE_ERROR);
            setConfigName(resource.getFilename());
            setConfigUri(resource.getURI());

            parserResource(resource);
            LOGGER.info("load properties resource. path: {}", getConfigUri());
        } catch (Exception e) {
            KstryException.throwException(e, ExceptionEnum.CONFIGURATION_PARSE_FAILURE);
        }
    }

    public List<BasicKValue> getKValueList() {
        return kValueList;
    }

    @SuppressWarnings("unchecked")
    private void parserResource(Resource resource) {
        YamlMapFactoryBean yamlMapFactoryBean = new YamlMapFactoryBean();
        yamlMapFactoryBean.setResources(resource);
        yamlMapFactoryBean.afterPropertiesSet();
        Map<String, BasicKValue> kValueMap = Maps.newHashMap();
        yamlMapFactoryBean.getObject().forEach((k, v) -> {
            if (!(v instanceof Map)) {
                return;
            }
            BasicKValue kValue = kValueMap.computeIfAbsent(k, BasicKValue::new);
            ((Map<Object, Object>) v).forEach((inK, inV) -> {
                String inKey = GlobalUtil.transferNotEmpty(inK, String.class);
                kValue.addValue(inKey, inV);
            });
        });
        kValueList = ImmutableList.copyOf(kValueMap.values());
    }
}
