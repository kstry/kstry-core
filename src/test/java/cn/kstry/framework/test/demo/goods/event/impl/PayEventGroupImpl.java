package cn.kstry.framework.test.demo.goods.event.impl;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.core.facade.TaskResponse;
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
    public TaskResponse<PayResponse> pay(PayRequest payRequest) {
        return null;
    }
}
