package cn.kstry.framework.core.component.bpmn;

import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;

import java.util.Map;
import java.util.Optional;

/**
 * 流程解析器
 */
public interface ProcessParser {

    /**
     * 获取全部主流程
     *
     * @return 全部主流程
     */
    Map<String, ProcessLink> getAllProcessLink();

    /**
     * 获取单个主流程
     *
     * @param startEventId startEventId
     * @return 单个主流程
     */
    Optional<ProcessLink> getProcessLink(String startEventId);

    /**
     * 获取全部子流程
     *
     * @return 全部子流程
     */
    Map<String, SubProcessLink> getAllSubProcessLink();

    /**
     * 获取单个子流程
     *
     * @param subProcessId subProcessId
     * @return 单个子流程
     */
    Optional<SubProcessLink> getSubProcessLink(String subProcessId);
}
