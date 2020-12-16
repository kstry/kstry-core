package cn.kstry.framework.test.demo.hongbao.facade;


public class CreateHongBaoRequest extends BaseHongBaoRequest {


    private static final long serialVersionUID = 1646288691754346153L;
    /**
     * 红包总金额 单位：分
     */
    private Long fullMoney;

    /**
     * 红包库存
     */
    private Integer count;

    public Long getFullMoney() {
        return fullMoney;
    }

    public void setFullMoney(Long fullMoney) {
        this.fullMoney = fullMoney;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
