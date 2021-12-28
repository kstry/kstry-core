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

import cn.kstry.framework.core.enums.ConfigTypeEnum;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author lykan
 */
public class PropertiesConfigResource extends BasicConfigResource implements ConfigResource {

    private static final List<String> YAML_SUFFIX_NAME_LIST = Lists.newArrayList(".yaml", ".yml");

    public PropertiesConfigResource(String configName) {
        super(configName);
    }

    @Override
    public List<Config> getConfigList() {
        return getResourceList().stream().filter(resource -> YAML_SUFFIX_NAME_LIST.stream().anyMatch(resource.getFilename()::endsWith))
                .map(PropertiesConfig::new).map(cfg -> GlobalUtil.transferNotEmpty(cfg, Config.class)).collect(Collectors.toList());
    }

    @Override
    public ConfigTypeEnum getConfigType() {
        return ConfigTypeEnum.PROPERTIES;
    }
}
