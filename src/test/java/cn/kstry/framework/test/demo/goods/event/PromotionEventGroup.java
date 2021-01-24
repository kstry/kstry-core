package cn.kstry.framework.test.demo.goods.event;

import cn.kstry.framework.test.demo.goods.facade.ImmediatelyDiscountRequest;

/**
 * 营销系统
 *
 * @author lykan
 */
public interface PromotionEventGroup {

    void immediatelyDiscount(ImmediatelyDiscountRequest request);
}
