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

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.impl.SubProcessImpl;
import cn.kstry.framework.core.component.bpmn.BpmnProcessParser;
import cn.kstry.framework.core.component.bpmn.SerializeProcessParser;
import cn.kstry.framework.core.component.jsprocess.metadata.JsonProcess;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 序列化流程至JSON
 *
 * @author lykan
 */
public class JsonSerializeProcessParser implements SerializeProcessParser<String> {

    @Override
    public Optional<String> serialize(StartEvent startEvent) {
        if (startEvent == null) {
            return Optional.empty();
        }
        JsonSerializeIterator iterator = new JsonSerializeIterator(true, startEvent, getJsonProcess(false, startEvent));
        iterator.setNeedOriginalId(true);
        return Optional.of(iterator.getJsonProcesses()).map(JSON::toJSONString);
    }

    @Override
    public Optional<String> bpmnSerialize(String bpmn) {
        if (StringUtils.isBlank(bpmn)) {
            return Optional.empty();
        }

        List<JsonProcess> jsonProcesses = Lists.newArrayList();
        BpmnProcessParser bpmnProcessParser = new BpmnProcessParser(JsonSerializeProcessParser.class.getName(), bpmn);
        bpmnProcessParser.getAllProcessLink().values().forEach(processLink -> {
            StartEvent startEvent = processLink.getElement();
            JsonProcess jsonProcess = getJsonProcess(false, startEvent);
            jsonProcesses.addAll(new JsonSerializeIterator(false, startEvent, jsonProcess).getJsonProcesses());
        });
        bpmnProcessParser.getSeparatedSubProcessLinks().forEach(subProcessLink -> {
            SubProcessImpl subProcess = subProcessLink.getSubProcess();
            StartEvent startEvent = subProcess.getStartEvent();
            JsonProcess jsonProcess = getJsonProcess(true, startEvent);
            JsonSerializeIterator jsonSerializeIterator = new JsonSerializeIterator(false, startEvent, jsonProcess);
            jsonProcess.setProperties(jsonSerializeIterator.getElementProperties(subProcess));
            jsonProcesses.addAll(jsonSerializeIterator.getJsonProcesses());
        });
        return Optional.of(jsonProcesses).map(p -> JSON.toJSONString(p, SerializerFeature.DisableCircularReferenceDetect));
    }

    private JsonProcess getJsonProcess(boolean isSubProcess, StartEvent startEvent) {
        JsonProcess jsonProcess = new JsonProcess();
        jsonProcess.setSubProcess(isSubProcess);
        jsonProcess.setStartId(startEvent.getId());
        jsonProcess.setStartName(startEvent.getName());
        jsonProcess.setProcessId(startEvent.getProcessId());
        jsonProcess.setProcessName(startEvent.getProcessName());
        jsonProcess.setJsonNodes(Lists.newArrayList());
        return jsonProcess;
    }
}
