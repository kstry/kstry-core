package cn.kstry.framework.core.component.bpmn;

import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.link.BpmnLink;
import cn.kstry.framework.core.resource.config.ConfigResource;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * BPMN 流程解析器
 */
public class BpmnProcessParser implements ConfigResource {

    /**
     * BPMN 文件解析器 Camunda
     */
    private static final BpmnModelTransfer<BpmnModelInstance> bpmnModelTransfer = new CamundaBpmnModelTransfer();

    private final BpmnModelInstance modelInstance;

    private final String resourceName;

    public BpmnProcessParser(String resourceName, String resource) {
        AssertUtil.anyNotBlank(resourceName, resource);
        this.resourceName = resourceName;
        this.modelInstance = Bpmn.readModelFromStream(new ByteArrayInputStream(resource.getBytes()));
    }

    public BpmnProcessParser(String resourceName, InputStream resource) {
        AssertUtil.notBlank(resourceName);
        AssertUtil.notNull(resource);
        this.resourceName = resourceName;
        this.modelInstance = Bpmn.readModelFromStream(resource);
    }

    public Map<String, BpmnLink> getAllBpmnLink() {
        Collection<StartEvent> startEventList = modelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.StartEvent.class);
        Map<String, BpmnLink> bpmnLinkMap = Maps.newHashMap();
        startEventList.forEach(startEvent -> {
            Optional<BpmnLink> model = bpmnModelTransfer.getBpmnLink(this, modelInstance, startEvent.getId());
            if (!model.isPresent()) {
                return;
            }
            bpmnLinkMap.put(startEvent.getId(), model.get());
        });
        return bpmnLinkMap;
    }

    public Optional<BpmnLink> getBpmnLink(String startEventId) {
        if (StringUtils.isBlank(startEventId)) {
            return Optional.empty();
        }
        return bpmnModelTransfer.getBpmnLink(this, modelInstance, startEventId);
    }

    public Map<String, SubProcessLink> getAllSubProcessLink() {
        return bpmnModelTransfer.getAllSubProcessLink(this, modelInstance);
    }

    public Optional<SubProcessLink> getSubProcessLink(String subProcessId) {
        if (StringUtils.isBlank(subProcessId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(getAllSubProcessLink().get(subProcessId));
    }

    @Override
    public String getConfigName() {
        return resourceName;
    }
}
