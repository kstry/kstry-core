package cn.kstry.framework.test.demo.xiaoming.event.impl;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.test.demo.xiaoming.event.PromotionEventGroup;
import cn.kstry.framework.test.demo.xiaoming.role.PromotionRole;

/**
 *
 * @author lykan
 */
@EventGroupComponent(eventGroupName = "USER_PAY_ACTION", operatorRoleClass = PromotionRole.class)
public class PromotionEventGroupImpl implements PromotionEventGroup {


}
