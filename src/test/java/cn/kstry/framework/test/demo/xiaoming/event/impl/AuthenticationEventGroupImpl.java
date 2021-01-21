package cn.kstry.framework.test.demo.xiaoming.event.impl;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.test.demo.xiaoming.event.AuthenticationEventGroup;
import cn.kstry.framework.test.demo.xiaoming.role.AuthenticationRole;

/**
 *
 * @author lykan
 */
@EventGroupComponent(eventGroupName = "USER_PAY_ACTION", operatorRoleClass = AuthenticationRole.class)
public class AuthenticationEventGroupImpl implements AuthenticationEventGroup {

}
