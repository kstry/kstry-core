package cn.kstry.framework.test.demo.goods.event.impl;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.facade.TaskResponseBox;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.test.demo.goods.entity.User;
import cn.kstry.framework.test.demo.goods.enums.UserTypeEnum;
import cn.kstry.framework.test.demo.goods.event.AuthenticationEventGroup;
import cn.kstry.framework.test.demo.goods.facade.AuthRequest;
import cn.kstry.framework.test.demo.goods.facade.AuthResponse;
import cn.kstry.framework.test.demo.goods.facade.UserLoginRequest;
import cn.kstry.framework.test.demo.goods.facade.UserLoginResponse;
import cn.kstry.framework.test.demo.goods.role.AuthenticationRole;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author lykan
 */
@EventGroupComponent(eventGroupName = "USER_AUTHENTICATION_EVENT_GROUP", operatorRoleClass = AuthenticationRole.class)
public class UserAuthenticationEventGroupImpl implements AuthenticationEventGroup {

    protected final List<User> userList = Lists.newArrayList(new User(1L, "张三", 10000), new User(2L, "李四", 10000));

    @Override
    public TaskResponse<UserLoginResponse> userLogin(UserLoginRequest userLoginRequest) {

        List<User> collect = userList.stream().filter(user -> user.getUserId().equals(userLoginRequest.getUserId())).collect(Collectors.toList());
        AssertUtil.oneSize(collect);
        User user = collect.get(0);
        user.setUserTypeEnum(UserTypeEnum.USER);

        UserLoginResponse response = new UserLoginResponse();
        response.setUser(user);
        System.out.println("userLogin ->" + JSON.toJSONString(user));

        return TaskResponseBox.buildSuccess(response);
    }

    @Override
    public TaskResponse<AuthResponse> auth(AuthRequest authRequest) {

        AssertUtil.notNull(authRequest.getUser());
        if (userList.stream().noneMatch(user -> user.getUserId() != null && user.getUserId().equals(authRequest.getUser().getUserId()))) {
            return TaskResponseBox.buildError("50001", "登陆失败");
        }
        AuthResponse authResponse = new AuthResponse();
        System.out.println("userAuth ->" + JSON.toJSONString(authRequest.getUser()));
        return TaskResponseBox.buildSuccess(authResponse);
    }

    @Override
    public void safeCheck() {
        System.out.println(Thread.currentThread().getName() + " safe check!");
    }
}
