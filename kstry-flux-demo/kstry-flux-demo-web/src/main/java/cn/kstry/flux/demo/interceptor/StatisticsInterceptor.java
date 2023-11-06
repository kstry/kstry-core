package cn.kstry.flux.demo.interceptor;

import cn.kstry.flux.demo.process.DefSubProcess;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.component.bpmn.link.SubBpmnLink;
import cn.kstry.framework.core.engine.interceptor.SubProcessInterceptor;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.LambdaUtil;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author lykan
 */
@Slf4j
@Component
public class StatisticsInterceptor implements SubProcessInterceptor {

    @Override
    public Set<String> pointcut() {
        return Sets.newHashSet(SubBpmnLink.relatedStartId(LambdaUtil.getProcessName(DefSubProcess::buildStatisticsSubProcess)));
    }

    @Override
    public boolean beforeProcessor(ScopeDataOperator dataOperator, Role role) {
        // 开始统计
        log.info("开始统计...");
        return true;
    }

    @Override
    public void afterProcessor(ScopeDataOperator dataOperator, Role role) {
        // 统计结束
        log.info("统计结束...");
    }

    @Override
    public void finallyProcessor(ScopeDataOperator dataOperator, Role role) {
        // 统计结束
        log.info("兜底逻辑...");
    }
}
