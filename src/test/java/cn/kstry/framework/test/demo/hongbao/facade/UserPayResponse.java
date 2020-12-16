package cn.kstry.framework.test.demo.hongbao.facade;

public class UserPayResponse {

    private Money money;

    public Money getMoney() {
        return money;
    }

    public void setMoney(Money money) {
        this.money = money;
    }

    public static class Money {
        private Long payMoney;

        public Long getPayMoney() {
            return payMoney;
        }

        public void setPayMoney(Long payMoney) {
            this.payMoney = payMoney;
        }
    }
}
