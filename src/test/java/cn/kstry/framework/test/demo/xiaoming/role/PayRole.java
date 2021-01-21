package cn.kstry.framework.test.demo.xiaoming.role;

import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.operator.TaskActionOperatorRole;
import cn.kstry.framework.test.demo.xiaoming.facade.PayRequest;
import cn.kstry.framework.test.demo.xiaoming.facade.PayResponse;

/**
 *
 * @author lykan
 */
public interface PayRole extends TaskActionOperatorRole {

    TaskResponse<PayResponse> pay(PayRequest payRequest);

}
