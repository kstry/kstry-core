package cn.kstry.framework.test.demo.xiaoming.event.impl;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.test.demo.xiaoming.event.GoodsEventGroup;
import cn.kstry.framework.test.demo.xiaoming.role.GoodsRole;

/**
 *
 * @author lykan
 */
@EventGroupComponent(eventGroupName = "USER_PAY_ACTION", operatorRoleClass = GoodsRole.class)
public class GoodsEventGroupImpl implements GoodsEventGroup {

}
