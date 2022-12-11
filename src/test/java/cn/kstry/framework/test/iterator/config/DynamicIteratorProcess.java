package cn.kstry.framework.test.iterator.config;

import cn.kstry.framework.core.bpmn.extend.ElementIterable;
import cn.kstry.framework.core.bpmn.impl.BasicElementIterable;
import cn.kstry.framework.core.component.bpmn.link.BpmnLink;
import cn.kstry.framework.core.component.bpmn.link.StartBpmnLink;
import cn.kstry.framework.core.component.dynamic.creator.DynamicProcess;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DynamicIteratorProcess implements DynamicProcess {

    /**
     * 测试同步情况的批处理迭代
     */
    public static final String ITERATE_PROCESS_01 = "ITERATE_PROCESS_01";

    /**
     * 测试异步情况的批处理迭代
     */
    public static final String ITERATE_PROCESS_02 = "ITERATE_PROCESS_02";

    /**
     * 测试子流程同步情况的批处理迭代
     */
    public static final String ITERATE_PROCESS_03 = "ITERATE_PROCESS_03";

    /**
     * 测试子流程异步情况的批处理迭代
     */
    public static final String ITERATE_PROCESS_04 = "ITERATE_PROCESS_04";

    @Override
    public Optional<BpmnLink> getBpmnLink(String startId) {
        if (ITERATE_PROCESS_01.equals(startId)) {
            BasicElementIterable iterable = ElementIterable.builder("req.numList").stride(2).build();
            BpmnLink bpmnLink = StartBpmnLink.build(ITERATE_PROCESS_01);
            bpmnLink.nextTask("calculate-service", "batch-square").iterable(iterable).build().end();
            return Optional.of(bpmnLink);
        }

        if (ITERATE_PROCESS_02.equals(startId)) {
            BasicElementIterable iterable = ElementIterable.builder("req.numList").stride(3).openAsync().build();
            BpmnLink bpmnLink = StartBpmnLink.build(ITERATE_PROCESS_02);
            bpmnLink.nextTask("calculate-service", "batch-square").iterable(iterable).build().end();
            return Optional.of(bpmnLink);
        }
        if (ITERATE_PROCESS_03.equals(startId)) {
            BasicElementIterable iterable = ElementIterable.builder("req.numList").stride(3).openAsync().build();
            BpmnLink bpmnLink = StartBpmnLink.build(ITERATE_PROCESS_03);
            bpmnLink.nextSubProcess(DynamicIteratorSubProcess.ITERATE_SUB_PROCESS_01).iterable(iterable).build().end();
            return Optional.of(bpmnLink);
        }
        if (ITERATE_PROCESS_04.equals(startId)) {
            BpmnLink bpmnLink = StartBpmnLink.build(ITERATE_PROCESS_04);
            bpmnLink.nextSubProcess(DynamicIteratorSubProcess.ITERATE_SUB_PROCESS_02).build().end();
            return Optional.of(bpmnLink);
        }
        return Optional.empty();
    }
}
