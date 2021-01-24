package cn.kstry.framework.test.demo.goods.facade;

import cn.kstry.framework.core.facade.TaskRequest;

/**
 *
 * @author lykan
 */
public class PayRequest implements TaskRequest {

    private long money;

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }
}
