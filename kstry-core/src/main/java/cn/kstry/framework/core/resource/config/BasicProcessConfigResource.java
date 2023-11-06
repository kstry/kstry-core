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

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.component.bpmn.ProcessParser;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.enums.ResourceTypeEnum;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程配置信息，一个对象对应一个流程文件
 *
 * @author lykan
 */
public class BasicProcessConfigResource extends AbstractConfigResource implements ProcessConfigResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicProcessConfigResource.class);

    private static final List<String> BPMN_PROCESS_EXTENSION_LIST = Lists.newArrayList("bpmn");

    private static final List<String> JSON_PROCESS_EXTENSION_LIST = Lists.newArrayList("js", "json");

    /**
     * 流程解析器
     */
    protected ProcessParser processParser;

    public BasicProcessConfigResource(Resource resource) {
        super(resource);
        LOGGER.info("Load bpmn resource. path: {}", getUri());
    }

    @Override
    public Map<String, SubProcessLink> getSubProcessMap() {
        return processParser.getAllSubProcessLink();
    }

    @Override
    public List<StartEvent> getStartEventList() {
        return processParser.getAllProcessLink().values().stream().map(link -> (StartEvent) link.getElement()).collect(Collectors.toList());
    }

    @Override
    public ResourceTypeEnum getResourceType() {
        String fileExtension = Files.getFileExtension(getConfigName());
        if (StringUtils.isBlank(fileExtension)) {
            return ResourceTypeEnum.CUSTOM;
        }
        if (BPMN_PROCESS_EXTENSION_LIST.contains(fileExtension.toLowerCase(Locale.ROOT))) {
            return ResourceTypeEnum.BPMN_PROCESS;
        }
        if (JSON_PROCESS_EXTENSION_LIST.contains(fileExtension.toLowerCase(Locale.ROOT))) {
            return ResourceTypeEnum.JSON_PROCESS;
        }
        return ResourceTypeEnum.CUSTOM;
    }
}
