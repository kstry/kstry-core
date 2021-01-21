package cn.kstry.framework.test.demo.goods.role;

import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.operator.TaskActionOperatorRole;
import cn.kstry.framework.test.demo.goods.facade.PayRequest;
import cn.kstry.framework.test.demo.goods.facade.PayResponse;

/**
 *
 * @author lykan
 */
public interface PayRole extends TaskActionOperatorRole {

    TaskResponse<PayResponse> pay(PayRequest payRequest);

}
