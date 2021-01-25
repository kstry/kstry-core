package cn.kstry.framework.test.demo.goods.event.impl;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.core.facade.NoticeBusTaskResponse;
import cn.kstry.framework.core.facade.NoticeBusTaskResponseBox;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.test.demo.goods.entity.User;
import cn.kstry.framework.test.demo.goods.enums.UserTypeEnum;
import cn.kstry.framework.test.demo.goods.event.AuthenticationEventGroup;
import cn.kstry.framework.test.demo.goods.facade.UserLoginRequest;
import cn.kstry.framework.test.demo.goods.facade.UserLoginResponse;
import cn.kstry.framework.test.demo.goods.role.AuthenticationRole;
import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author lykan
 */
@EventGroupComponent(eventGroupName = "CUSTOMER_AUTHENTICATION_EVENT_GROUP", operatorRoleClass = AuthenticationRole.class)
public class CustomerAuthenticationEventGroupImpl extends UserAuthenticationEventGroupImpl implements AuthenticationEventGroup {

    @Override
    public NoticeBusTaskResponse<UserLoginResponse> userLogin(UserLoginRequest userLoginRequest) {

        List<User> collect = userList.stream().filter(user -> user.getUserId().equals(userLoginRequest.getUserId())).collect(Collectors.toList());
        AssertUtil.oneSize(collect);
        User user = collect.get(0);
        user.setUserTypeEnum(UserTypeEnum.CUSTOMER);

        UserLoginResponse response = new UserLoginResponse();
        response.setUser(user);
        System.out.println("customerLogin ->" + JSON.toJSONString(user));
        NoticeBusTaskResponse<UserLoginResponse> responseBox = NoticeBusTaskResponseBox.buildSuccess(response);
        responseBox.updateVariableDataMap(new HashMap<String, Object>() {{
            put("user", user);
        }});
        return responseBox;
    }
}
