package cn.kstry.framework.test.iterator.config;

import cn.kstry.framework.core.bpmn.extend.ElementIterable;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.dynamic.creator.DynamicSubProcess;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DynamicIteratorSubProcess implements DynamicSubProcess {

    /**
     * 测试子任务同步情况的批处理迭代
     */
    public static final String ITERATE_SUB_PROCESS_01 = "ITERATE_SUB_PROCESS_01";

    /**
     * 测试子任务异步情况的批处理迭代
     */
    public static final String ITERATE_SUB_PROCESS_02 = "ITERATE_SUB_PROCESS_02";

    @Override
    public List<SubProcessLink> getSubProcessLinks() {
        SubProcessLink subProcessLink2 = SubProcessLink.build(ITERATE_SUB_PROCESS_02, link -> link.nextTask("calculate-service", "batch-square").build().end());
        subProcessLink2.setElementIterable(ElementIterable.builder("req.numList").stride(2).build());
        return Lists.newArrayList(
                SubProcessLink.build(ITERATE_SUB_PROCESS_01, link -> link.nextTask("calculate-service", "batch-square").build().end()),
                subProcessLink2
        );
    }
}
