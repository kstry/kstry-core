package cn.kstry.framework.test.demo.goods.event.impl;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.test.demo.goods.event.PromotionEventGroup;
import cn.kstry.framework.test.demo.goods.facade.ImmediatelyDiscountRequest;
import cn.kstry.framework.test.demo.goods.role.PromotionRole;
import org.apache.commons.collections.CollectionUtils;

/**
 *
 * @author lykan
 */
@EventGroupComponent(eventGroupName = "PROMOTION_EVENT_GROUP", operatorRoleClass = PromotionRole.class)
public class PromotionEventGroupImpl implements PromotionEventGroup {

    @Override
    public void immediatelyDiscount(ImmediatelyDiscountRequest request) {
        if (CollectionUtils.isEmpty(request.getGoodsList())) {
            return;
        }

        System.out.println(Thread.currentThread().getName() + " - immediatelyDiscount");
        request.getGoodsList().forEach(g -> g.setMoney(g.getMoney() - 10));
    }
}
