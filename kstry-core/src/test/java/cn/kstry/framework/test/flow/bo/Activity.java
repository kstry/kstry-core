package cn.kstry.framework.test.flow.bo;

import cn.kstry.framework.core.annotation.NoticeSta;

@NoticeSta
public class Activity {

    private Long id;

    private Long discountPrice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(Long discountPrice) {
        this.discountPrice = discountPrice;
    }
}
