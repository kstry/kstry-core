package cn.kstry.framework.test.demo.hongbao.facade;

import cn.kstry.framework.core.facade.TaskRequest;


public class UserPayRequest implements TaskRequest {

    private static final long serialVersionUID = 6069122752279651200L;

    /**
     * 支付钱，单位：分
     */
    private Long payMoney;

    public Long getPayMoney() {
        return payMoney;
    }

    public void setPayMoney(Long payMoney) {
        this.payMoney = payMoney;
    }
}
