package cn.kstry.framework.test.demo.goods.event.impl;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.facade.TaskResponseBox;
import cn.kstry.framework.test.demo.goods.facade.PayRequest;
import cn.kstry.framework.test.demo.goods.facade.PayResponse;
import cn.kstry.framework.test.demo.goods.role.PayRole;

/**
 *
 * @author lykan
 */
@EventGroupComponent(eventGroupName = "PAY_EVENT_GROUP", operatorRoleClass = PayRole.class)
public class PayEventGroupImpl implements PayRole {

    @Override
    public TaskResponse<PayResponse> wxPay(PayRequest payRequest) {

        System.out.println("wxPay money ->" + payRequest.getMoney());
        return TaskResponseBox.buildSuccess(new PayResponse());
    }

    @Override
    public TaskResponse<PayResponse> aliPay(PayRequest payRequest) {
        System.out.println("aliPay money ->" + payRequest.getMoney());
        return TaskResponseBox.buildSuccess(new PayResponse());
    }
}
