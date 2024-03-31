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
package cn.kstry.framework.core.component.jsprocess.transfer;

import cn.kstry.framework.core.bpmn.impl.SubProcessImpl;
import cn.kstry.framework.core.component.bpmn.ProcessModelTransfer;
import cn.kstry.framework.core.component.bpmn.ProcessParser;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.jsprocess.metadata.JsonProcess;
import cn.kstry.framework.core.enums.ResourceTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.ResourceUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JSON 流程解析器
 *
 * @author lykan
 */
public class JsonProcessParser implements ProcessParser {

    private static final ProcessModelTransfer<List<JsonProcess>> processModelTransfer = new JsonProcessModelTransfer();

    private final ConfigResource configResource;

    private final List<JsonProcess> jsonProcess;

    public JsonProcessParser(String resourceName, String resource) {
        AssertUtil.anyNotBlank(resourceName, resource);
        this.configResource = ResourceUtil.getConfigResource(resourceName, ResourceTypeEnum.JSON_PROCESS);
        this.jsonProcess = JSON.parseArray(resource, JsonProcess.class);
    }

    public JsonProcessParser(ConfigResource configResource, InputStream resource) {
        AssertUtil.notNull(configResource);
        AssertUtil.notNull(resource);
        this.configResource = configResource;
        try {
            this.jsonProcess = JSON.parseArray(CharStreams.toString(new InputStreamReader(resource, Charsets.UTF_8)), JsonProcess.class);
        } catch (Exception e) {
            throw ExceptionUtil.buildException(e, ExceptionEnum.CONFIGURATION_RESOURCE_ERROR, null);
        }
    }

    @Override
    public Map<String, ProcessLink> getAllProcessLink() {
        if (CollectionUtils.isEmpty(jsonProcess)) {
            return Maps.newHashMap();
        }
        return jsonProcess.stream()
                .filter(process -> !process.isSubProcess()).map(p -> getProcessLink(p.getStartId()).orElse(null))
                .filter(Objects::nonNull).collect(Collectors.toMap(p -> p.getElement().getId(), Function.identity(), (x, y) -> x));
    }

    @Override
    public Optional<ProcessLink> getProcessLink(String startEventId) {
        if (StringUtils.isBlank(startEventId)) {
            return Optional.empty();
        }
        return processModelTransfer.getProcessLink(configResource, jsonProcess, startEventId);
    }

    @Override
    public Map<String, SubProcessLink> getAllSubProcessLink() {
        Map<String, SubProcessLink> subProcessMap = Maps.newHashMap();
        if (jsonProcess == null) {
            return subProcessMap;
        }

        List<JsonProcess> subProcessList = jsonProcess.stream().filter(process -> process.isSubProcess() && StringUtils.isNotBlank(process.getStartId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(subProcessList)) {
            return Maps.newHashMap();
        }
        subProcessList.forEach(sp -> {
            Optional<SubProcessLink> subProcessLinkOptional = getSubProcessLink(sp.getProcessId());
            if (!subProcessLinkOptional.isPresent()) {
                return;
            }
            SubProcessImpl subProcess = subProcessLinkOptional.get().getSubProcess();
            AssertUtil.notTrue(subProcessMap.containsKey(subProcess.getId()), ExceptionEnum.ELEMENT_DUPLICATION_ERROR,
                    "There are duplicate SubProcess ids defined! identity: {}, fileName: {}", subProcess.identity(), configResource.getConfigName());
            subProcessMap.put(subProcess.getId(), subProcessLinkOptional.get());
        });
        return subProcessMap;
    }

    @Override
    public Optional<SubProcessLink> getSubProcessLink(String subProcessId) {
        if (StringUtils.isBlank(subProcessId)) {
            return Optional.empty();
        }
        return processModelTransfer.getSubProcessLink(configResource, jsonProcess, subProcessId);
    }
}
