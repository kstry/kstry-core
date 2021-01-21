package cn.kstry.framework.test.demo.xiaoming.event.impl;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.test.demo.xiaoming.facade.PayRequest;
import cn.kstry.framework.test.demo.xiaoming.facade.PayResponse;
import cn.kstry.framework.test.demo.xiaoming.role.PayRole;

/**
 *
 * @author lykan
 */
@EventGroupComponent(eventGroupName = "USER_PAY_ACTION", operatorRoleClass = PayRole.class)
public class PayEventGroupImpl implements PayRole {

    @Override
    public TaskResponse<PayResponse> pay(PayRequest payRequest) {
        return null;
    }
}
