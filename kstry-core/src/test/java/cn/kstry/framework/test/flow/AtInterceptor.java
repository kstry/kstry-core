package cn.kstry.framework.test.flow;

import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.engine.interceptor.SubProcessIdentity;
import cn.kstry.framework.core.engine.interceptor.SubProcessInterceptor;
import cn.kstry.framework.core.role.Role;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author lykan
 */
@Component
public class AtInterceptor implements SubProcessInterceptor {

    @Override
    public Set<SubProcessIdentity> getSubProcessIdentity() {
        return Sets.newHashSet(new SubProcessIdentity("Event_1ql4cvh"));
    }

    @Override
    public boolean beforeProcessor(ScopeDataOperator dataOperator, Role role) {
        return true;
    }

    @Override
    public void afterProcessor(ScopeDataOperator dataOperator, Role role) {
        System.out.println();
    }

    @Override
    public void errorProcessor(Throwable exception, ScopeDataOperator dataOperator, Role role) {
        System.out.println("errorProcessor...");
    }

    @Override
    public void finallyProcessor(ScopeDataOperator dataOperator, Role role) {
    }
}
