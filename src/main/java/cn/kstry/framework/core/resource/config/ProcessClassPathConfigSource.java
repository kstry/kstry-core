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

import cn.kstry.framework.core.enums.SourceTypeEnum;

import java.util.List;
import java.util.stream.Collectors;

/**
 * BPMN 文件类路径配置来源
 *
 * @author lykan
 */
public class ProcessClassPathConfigSource extends ClassPathConfigSource implements ConfigSource {

    public ProcessClassPathConfigSource(String configName) {
        super(configName);
    }

    @Override
    public List<ConfigResource> getConfigResourceList() {
        return getResourceList().stream().map(MultiProtocolProcessConfigResource::new).collect(Collectors.toList());
    }

    @Override
    public SourceTypeEnum getSourceType() {
        return SourceTypeEnum.PROCESS;
    }
}
