package cn.kstry.framework.test.demo.hongbao.action;

import cn.kstry.framework.core.facade.RouteMapResponse;
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.route.RouteEventGroup;
import cn.kstry.framework.test.demo.hongbao.UserPayRole;
import cn.kstry.framework.test.demo.hongbao.facade.UserPayRequest;
import cn.kstry.framework.test.demo.hongbao.facade.UserPayResponse;


public abstract class BaseUserPayActionGroup extends RouteEventGroup implements UserPayAction {

    @Override
    public RouteMapResponse<UserPayResponse> pay(UserPayRequest request) {
        throw new RuntimeException("");
    }

    @Override
    public Class<? extends EventOperatorRole> getTaskActionOperatorRoleClass() {
        return UserPayRole.class;
    }
}
