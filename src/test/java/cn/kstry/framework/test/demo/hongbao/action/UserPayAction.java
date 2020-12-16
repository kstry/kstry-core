package cn.kstry.framework.test.demo.hongbao.action;

import cn.kstry.framework.core.facade.RouteMapResponse;
import cn.kstry.framework.test.demo.hongbao.facade.UserPayRequest;
import cn.kstry.framework.test.demo.hongbao.facade.UserPayResponse;

public interface UserPayAction {

    /**
     * 支付
     *
     * @param request 非空
     * @return 支付结果
     */
    RouteMapResponse<UserPayResponse> pay(UserPayRequest request);


    void first();
}
