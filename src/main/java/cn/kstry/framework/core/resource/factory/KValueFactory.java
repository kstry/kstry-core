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
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Lists;

import cn.kstry.framework.core.enums.ResourceTypeEnum;
import cn.kstry.framework.core.kv.KValue;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.resource.config.PropertiesConfigResource;
import cn.kstry.framework.core.util.GlobalUtil;

/**
 * KValue 资源创建工厂
 *
 * @author lykan
 */
public class KValueFactory extends BasicResourceFactory<KValue> {

    public KValueFactory(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public List<KValue> getResourceList() {
        List<ConfigResource> configResourceList = getConfigResource(ResourceTypeEnum.PROPERTIES);
        if (CollectionUtils.isEmpty(configResourceList)) {
            return Lists.newArrayList();
        }

        List<PropertiesConfigResource> propResourceList =
                configResourceList.stream().map(c -> GlobalUtil.transferNotEmpty(c, PropertiesConfigResource.class)).collect(Collectors.toList());
        return propResourceList.stream().flatMap(propResource -> propResource.getKValueList().stream()).collect(Collectors.toList());
    }
}
