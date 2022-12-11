package cn.kstry.framework.test.kv.service;

import cn.kstry.framework.core.component.bpmn.link.BpmnLink;
import cn.kstry.framework.core.component.bpmn.link.StartBpmnLink;
import cn.kstry.framework.core.component.dynamic.creator.DynamicProcess;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KvDynamicProcess implements DynamicProcess {

    public static final String KV_PROCESS_01 = "KV_PROCESS_01";

    public static final String KV_PROCESS_02 = "KV_PROCESS_02";
    public static final String KV_PROCESS_03 = "KV_PROCESS_03";
    public static final String KV_PROCESS_04 = "KV_PROCESS_04";
    public static final String KV_PROCESS_05 = "KV_PROCESS_05";

    @Override
    public Optional<BpmnLink> getBpmnLink(String startId) {
        if (KV_PROCESS_01.equals(startId)) {
            BpmnLink bpmnLink = StartBpmnLink.build(KV_PROCESS_01);
            bpmnLink.nextTask("kv-test", "kv-channel-scope").build().end();
            return Optional.of(bpmnLink);
        }
        if (KV_PROCESS_02.equals(startId)) {
            BpmnLink bpmnLink = StartBpmnLink.build(KV_PROCESS_02);
            bpmnLink.nextTask("kv-test", "kv-scope-env").build().end();
            return Optional.of(bpmnLink);
        }
        if (KV_PROCESS_03.equals(startId)) {
            BpmnLink bpmnLink = StartBpmnLink.build(KV_PROCESS_03);
            bpmnLink.nextTask("kv-test", "kv-default-env").build().end();
            return Optional.of(bpmnLink);
        }
        if (KV_PROCESS_04.equals(startId)) {
            BpmnLink bpmnLink = StartBpmnLink.build(KV_PROCESS_04);
            bpmnLink.nextTask("kv-test", "kv-empty-get").build().end();
            return Optional.of(bpmnLink);
        }
        if (KV_PROCESS_05.equals(startId)) {
            BpmnLink bpmnLink = StartBpmnLink.build(KV_PROCESS_05);
            bpmnLink.nextTask("kv-test", "kv-dynamic-get").build().end();
            return Optional.of(bpmnLink);
        }
        return Optional.empty();
    }
}
