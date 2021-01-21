package cn.kstry.framework.test.demo.goods.event.impl;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.test.demo.goods.event.GoodsEventGroup;
import cn.kstry.framework.test.demo.goods.role.GoodsRole;

/**
 *
 * @author lykan
 */
@EventGroupComponent(eventGroupName = "GOODS_EVENT_GROUP", operatorRoleClass = GoodsRole.class)
public class GoodsEventGroupImpl implements GoodsEventGroup {

}
