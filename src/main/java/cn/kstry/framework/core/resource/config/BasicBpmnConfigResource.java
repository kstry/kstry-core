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
import cn.kstry.framework.core.component.bpmn.BpmnProcessParser;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BPMN 配置信息，一个对象对应一个 BPMN 文件
 *
 * @author lykan
 */
public class BasicBpmnConfigResource extends AbstractConfigResource implements BpmnConfigResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicBpmnConfigResource.class);

    /**
     * BPMN 文件解析器Camunda
     */
    private BpmnProcessParser bpmnProcessParser;


    public BasicBpmnConfigResource(Resource resource) {
        super(resource);
        LOGGER.info("Load bpmn resource. path: {}", getUri());
    }

    @Override
    public void init(Resource resource, InputStream inputStream) {
        try {
            bpmnProcessParser = new BpmnProcessParser(this.getConfigName(), inputStream);
        } catch (Throwable e) {
            throw ExceptionUtil.buildException(e, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, GlobalUtil.format("BPMN configuration file parsing failure! fileName: {}", getConfigName()));
        }
    }

    @Override
    public Map<String, SubProcessLink> getSubProcessMap() {
        return bpmnProcessParser.getAllSubProcessLink();
    }

    @Override
    public List<StartEvent> getStartEventList() {
        return bpmnProcessParser.getAllBpmnLink().values().stream().map(link -> (StartEvent) link.getElement()).collect(Collectors.toList());
    }
}
