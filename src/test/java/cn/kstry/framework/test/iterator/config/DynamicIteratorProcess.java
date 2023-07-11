package cn.kstry.framework.test.iterator.config;

import cn.kstry.framework.core.bpmn.extend.ElementIterable;
import cn.kstry.framework.core.bpmn.impl.BasicElementIterable;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.bpmn.link.StartProcessLink;
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
    public Optional<ProcessLink> getProcessLink(String startId) {
        if (ITERATE_PROCESS_01.equals(startId)) {
            BasicElementIterable iterable = ElementIterable.builder("req.numList").stride(2).build();
            ProcessLink processLink = StartProcessLink.build(ITERATE_PROCESS_01);
            processLink.nextTask("calculate-service", "batch-square").iterable(iterable).build().end();
            return Optional.of(processLink);
        }

        if (ITERATE_PROCESS_02.equals(startId)) {
            ProcessLink processLink = StartProcessLink.build(ITERATE_PROCESS_02);
            processLink.nextTask("calculate-service", "batch-square").build().end();
            return Optional.of(processLink);
        }
        if (ITERATE_PROCESS_03.equals(startId)) {
            BasicElementIterable iterable = ElementIterable.builder("req.numList").stride(3).openAsync().build();
            ProcessLink processLink = StartProcessLink.build(ITERATE_PROCESS_03);
            processLink.nextSubProcess(DynamicIteratorSubProcess.ITERATE_SUB_PROCESS_01).iterable(iterable).build().end();
            return Optional.of(processLink);
        }
        if (ITERATE_PROCESS_04.equals(startId)) {
            ProcessLink processLink = StartProcessLink.build(ITERATE_PROCESS_04);
            processLink.nextSubProcess(DynamicIteratorSubProcess.ITERATE_SUB_PROCESS_02).build().end();
            return Optional.of(processLink);
        }
        return Optional.empty();
    }
}
