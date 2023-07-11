package cn.kstry.framework.core.component.bpmn;

import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.enums.ResourceTypeEnum;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ResourceUtil;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * BPMN 流程解析器
 */
public class BpmnProcessParser implements ProcessParser {

    /**
     * BPMN 文件解析器 Camunda
     */
    private static final CamundaProcessModelTransfer processModelTransfer = new CamundaProcessModelTransfer();

    private final BpmnModelInstance modelInstance;

    private final ConfigResource configResource;

    public BpmnProcessParser(String resourceName, String resource) {
        AssertUtil.anyNotBlank(resourceName, resource);
        this.configResource = ResourceUtil.getConfigResource(resourceName, ResourceTypeEnum.BPMN_PROCESS);
        this.modelInstance = Bpmn.readModelFromStream(new ByteArrayInputStream(resource.getBytes()));
    }

    public BpmnProcessParser(ConfigResource configResource, InputStream resource) {
        AssertUtil.notNull(configResource);
        AssertUtil.notNull(resource);
        this.configResource = configResource;
        this.modelInstance = Bpmn.readModelFromStream(resource);
    }

    @Override
    public Map<String, ProcessLink> getAllProcessLink() {
        Collection<StartEvent> startEventList = modelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.StartEvent.class);
        Map<String, ProcessLink> processLinkMap = Maps.newHashMap();
        startEventList.forEach(startEvent -> {
            Optional<ProcessLink> model = getProcessLink(startEvent.getId());
            if (!model.isPresent()) {
                return;
            }
            processLinkMap.put(startEvent.getId(), model.get());
        });
        return processLinkMap;
    }

    @Override
    public Optional<ProcessLink> getProcessLink(String startEventId) {
        if (StringUtils.isBlank(startEventId)) {
            return Optional.empty();
        }
        return processModelTransfer.getProcessLink(configResource, modelInstance, startEventId);
    }

    @Override
    public Map<String, SubProcessLink> getAllSubProcessLink() {
        Collection<org.camunda.bpm.model.bpmn.instance.SubProcess> subProcesses = modelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.SubProcess.class);
        if (CollectionUtils.isEmpty(subProcesses)) {
            return Maps.newHashMap();
        }
        return subProcesses.stream().map(subProcess -> getSubProcessLink(subProcess.getId()).orElse(null))
                .filter(Objects::nonNull).collect(Collectors.toMap(SubProcessLink::getSubProcessId, Function.identity(), (x, y) -> y));
    }

    @Override
    public Optional<SubProcessLink> getSubProcessLink(String subProcessId) {
        if (StringUtils.isBlank(subProcessId)) {
            return Optional.empty();
        }
        return processModelTransfer.getSubProcessLink(configResource, modelInstance, subProcessId);
    }

    public List<SubProcessLink> getSeparatedSubProcessLinks() {
        return processModelTransfer.getSeparatedSubProcessLinks(configResource, modelInstance);
    }
}
