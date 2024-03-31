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

import cn.kstry.framework.core.bpmn.*;
import cn.kstry.framework.core.bpmn.enums.BpmnTypeEnum;
import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.bpmn.extend.ElementIterable;
import cn.kstry.framework.core.bpmn.impl.SubProcessImpl;
import cn.kstry.framework.core.component.bpmn.DiagramTraverseSupport;
import cn.kstry.framework.core.component.jsprocess.metadata.JsonNode;
import cn.kstry.framework.core.component.jsprocess.metadata.JsonNodeProperties;
import cn.kstry.framework.core.component.jsprocess.metadata.JsonProcess;
import cn.kstry.framework.core.constant.BpmnElementProperties;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JSON 序列化迭代器
 *
 * @author lykan
 */
public class JsonSerializeIterator extends DiagramTraverseSupport<Map<String, JsonNode>> {

    private final StartEvent startEvent;
    private final List<JsonProcess> jsonProcesses;

    private boolean needOriginalId = false;

    public JsonSerializeIterator(boolean recursiveSubProcess, StartEvent startEvent, JsonProcess jsonProcess) {
        super(start -> Maps.newHashMap(), recursiveSubProcess);
        this.startEvent = startEvent;
        this.jsonProcesses = Lists.newArrayList(jsonProcess);
    }

    public List<JsonProcess> getJsonProcesses() {
        traverse(startEvent);
        return jsonProcesses;
    }

    @Override
    public void doSubProcess(SubProcessImpl subProcess) {
        if (!recursiveSubProcess) {
            return;
        }

        Optional<JsonProcess> jsonProcessOptional = getSubJsonProcess(subProcess);
        if (jsonProcessOptional.isPresent()) {
            return;
        }

        JsonProcess subJsonProcess = new JsonProcess();
        subJsonProcess.setSubProcess(true);
        subJsonProcess.setStartId(GlobalUtil.getOriginalId(needOriginalId, subProcess.getStartEvent().getId()));
        subJsonProcess.setStartName(subProcess.getStartEvent().getName());
        subJsonProcess.setProcessId(subProcess.getStartEvent().getProcessId());
        subJsonProcess.setProcessName(subProcess.getStartEvent().getProcessName());
        subJsonProcess.setJsonNodes(Lists.newArrayList());
        subJsonProcess.setProperties(getElementProperties(subProcess));
        jsonProcesses.add(subJsonProcess);
    }

    @Override
    public void doPlainElement(Map<String, JsonNode> course, FlowElement node, SubProcess subProcess) {
        JsonProcess jsonProcess = (subProcess == null) ? jsonProcesses.get(0) : GlobalUtil.notEmpty(getSubJsonProcess(subProcess));

        String actualId = GlobalUtil.getOriginalId(needOriginalId && (jsonProcess.isSubProcess() || !BpmnTypeEnum.START_EVENT.is(node.getElementType().getType())), node.getId());
        course.computeIfAbsent((node instanceof SubProcess) ? getSingleId(actualId, node.comingList().get(0).getId()) : actualId, id -> {
            JsonNode jn = new JsonNode();
            jn.setId(actualId);
            jn.setName(node.getName());
            jn.setType(node.getElementType().getType());
            jn.setProperties(getElementProperties(node));
            jn.setNextNodes(node.outingList().stream().map(FlowElement::getId).map(nextId -> {
                String actualNextId = GlobalUtil.getOriginalId(needOriginalId, nextId);
                if (CollectionUtils.size(node.outingList()) == 1 && node.outingList().get(0).getElementType() == BpmnTypeEnum.SUB_PROCESS) {
                    return getSingleId(actualNextId, node.getId());
                }
                return actualNextId;
            }).collect(Collectors.toList()));
            if (node instanceof SubProcess) {
                jn.setCallSubProcessId(jn.getId());
                jn.setId(getSingleId(jn.getId(), node.comingList().get(0).getId()));
            }
            if (node instanceof SequenceFlow) {
                ((SequenceFlow) node).getConditionExpression().ifPresent(c -> jn.setFlowExpression(c.getPlainExpression()));
            }
            jsonProcess.getJsonNodes().add(jn);
            return jn;
        });
    }

    public JsonNodeProperties getElementProperties(FlowElement node) {
        JsonNodeProperties properties = new JsonNodeProperties();
        if (node instanceof ParallelGateway) {
            ParallelGateway parallelGateway = GlobalUtil.transferNotEmpty(node, ParallelGateway.class);
            properties.put(BpmnElementProperties.TASK_STRICT_MODE, parallelGateway.isStrictMode());
            properties.put(BpmnElementProperties.ASYNC_ELEMENT_OPEN_ASYNC, parallelGateway.openAsync());
        } else if (node instanceof InclusiveGateway) {
            InclusiveGateway inclusiveGateway = GlobalUtil.transferNotEmpty(node, InclusiveGateway.class);
            fillPropertiesByServiceTask(properties, inclusiveGateway.getServiceTask().orElse(null));
            properties.put(BpmnElementProperties.ASYNC_ELEMENT_OPEN_ASYNC, inclusiveGateway.openAsync());
            properties.put(BpmnElementProperties.INCLUSIVE_GATEWAY_COMPLETED_COUNT, inclusiveGateway.getCompletedCount());
            properties.put(BpmnElementProperties.INCLUSIVE_GATEWAY_MIDWAY_START_ID, inclusiveGateway.getMidwayStartId());
        } else if (node instanceof ExclusiveGateway) {
            ExclusiveGateway exclusiveGateway = GlobalUtil.transferNotEmpty(node, ExclusiveGateway.class);
            fillPropertiesByServiceTask(properties, exclusiveGateway.getServiceTask().orElse(null));
        } else if (node instanceof ServiceTask) {
            ServiceTask serviceTask = GlobalUtil.transferNotEmpty(node, ServiceTask.class);
            fillPropertiesByServiceTask(properties, serviceTask);
        } else if (node instanceof SubProcess) {
            SubProcess subProcess = GlobalUtil.transferNotEmpty(node, SubProcess.class);
            ElementIterable elementIterable = subProcess.getElementIterable().orElse(null);
            if (elementIterable != null) {
                if (BooleanUtils.isTrue(elementIterable.openAsync())) {
                    properties.put(BpmnElementProperties.ITERATE_ASYNC, elementIterable.openAsync());
                }
                if (BooleanUtils.isTrue(elementIterable.getIteAlignIndex())) {
                    properties.put(BpmnElementProperties.ITERATE_ALIGN_INDEX, elementIterable.getIteAlignIndex());
                }
                properties.put(BpmnElementProperties.ITERATE_SOURCE, elementIterable.getIteSource());
                properties.put(BpmnElementProperties.ITERATE_STRATEGY, Optional.ofNullable(elementIterable.getIteStrategy()).map(IterateStrategyEnum::getKey).orElse(null));
                properties.put(BpmnElementProperties.ITERATE_STRIDE, elementIterable.getStride());
            }
            properties.put(BpmnElementProperties.TASK_STRICT_MODE, subProcess.strictMode());
            properties.put(BpmnElementProperties.TASK_TIMEOUT, subProcess.getTimeout());
        } else {
            return null;
        }
        return properties;
    }

    private void fillPropertiesByServiceTask(JsonNodeProperties properties, ServiceTask serviceTask) {
        if (properties == null || serviceTask == null) {
            return;
        }
        properties.put(BpmnElementProperties.SERVICE_TASK_TASK_COMPONENT, serviceTask.getTaskComponent());
        properties.put(BpmnElementProperties.SERVICE_TASK_TASK_SERVICE, serviceTask.getTaskService());
        properties.put(BpmnElementProperties.SERVICE_TASK_TASK_PROPERTY, serviceTask.getTaskProperty());
        properties.put(BpmnElementProperties.SERVICE_TASK_TASK_PARAMS, serviceTask.getTaskParams());
        properties.put(BpmnElementProperties.SERVICE_TASK_RETRY_TIMES, serviceTask.getRetryTimes());
        properties.put(BpmnElementProperties.TASK_ALLOW_ABSENT, serviceTask.allowAbsent());
        properties.put(BpmnElementProperties.TASK_STRICT_MODE, serviceTask.strictMode());
        properties.put(BpmnElementProperties.TASK_TIMEOUT, serviceTask.getTimeout());
        if (StringUtils.isNotBlank(serviceTask.getTaskInstruct())) {
            properties.put(BpmnElementProperties.SERVICE_TASK_TASK_INSTRUCT + serviceTask.getTaskInstruct(), serviceTask.getTaskInstructContent() == null ? StringUtils.EMPTY : serviceTask.getTaskInstructContent());
        }
        ElementIterable elementIterable = serviceTask.getElementIterable().orElse(null);
        if (elementIterable != null) {
            properties.put(BpmnElementProperties.ITERATE_SOURCE, elementIterable.getIteSource());
            properties.put(BpmnElementProperties.ITERATE_STRATEGY, Optional.ofNullable(elementIterable.getIteStrategy()).map(IterateStrategyEnum::getKey).orElse(null));
            properties.put(BpmnElementProperties.ITERATE_STRIDE, elementIterable.getStride());
            if (BooleanUtils.isTrue(elementIterable.openAsync())) {
                properties.put(BpmnElementProperties.ITERATE_ASYNC, elementIterable.openAsync());
            }
            if (BooleanUtils.isTrue(elementIterable.getIteAlignIndex())) {
                properties.put(BpmnElementProperties.ITERATE_ALIGN_INDEX, elementIterable.getIteAlignIndex());
            }
        }
    }

    public void setNeedOriginalId(boolean needOriginalId) {
        this.needOriginalId = needOriginalId;
    }

    private String getSingleId(String id, String salt) {
        if (StringUtils.isAnyBlank(id, salt)) {
            return id;
        }
        if (id.endsWith("-" + salt)) {
            return id;
        }
        return id + "-" + salt;
    }

    private Optional<JsonProcess> getSubJsonProcess(SubProcess subProcess) {
        return jsonProcesses.stream().filter(p -> p.isSubProcess() && Objects.equals(p.getStartId(), GlobalUtil.getOriginalId(needOriginalId, subProcess.getStartEvent().getId()))).findFirst();
    }
}
