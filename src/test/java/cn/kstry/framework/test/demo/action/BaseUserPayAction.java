package cn.kstry.framework.test.demo.action;

import cn.kstry.framework.core.facade.RouteMapResponse;
import cn.kstry.framework.core.operator.TaskActionOperatorRole;
import cn.kstry.framework.core.route.RouteTaskAction;
import cn.kstry.framework.test.demo.UserPayRole;
import cn.kstry.framework.test.demo.facade.UserPayRequest;
import cn.kstry.framework.test.demo.facade.UserPayResponse;


public abstract class BaseUserPayAction extends RouteTaskAction implements UserPayAction {

    @Override
    public RouteMapResponse<UserPayResponse> pay(UserPayRequest request) {
        throw new RuntimeException("");
    }

    @Override
    public Class<? extends TaskActionOperatorRole> getTaskActionOperatorRoleClass() {
        return UserPayRole.class;
    }
}
