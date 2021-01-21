package cn.kstry.framework.test.demo.goods.event.impl;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.test.demo.goods.event.PromotionEventGroup;
import cn.kstry.framework.test.demo.goods.role.PromotionRole;

/**
 *
 * @author lykan
 */
@EventGroupComponent(eventGroupName = "PROMOTION_EVENT_GROUP", operatorRoleClass = PromotionRole.class)
public class PromotionEventGroupImpl implements PromotionEventGroup {

}
