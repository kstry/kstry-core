package cn.kstry.framework.test.demo.goods.event;

import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.test.demo.goods.facade.AuthRequest;
import cn.kstry.framework.test.demo.goods.facade.AuthResponse;
import cn.kstry.framework.test.demo.goods.facade.UserLoginRequest;
import cn.kstry.framework.test.demo.goods.facade.UserLoginResponse;

/**
 *  鉴权系统
 *
 * @author lykan
 */
public interface AuthenticationEventGroup {

    TaskResponse<UserLoginResponse> userLogin(UserLoginRequest userLoginRequest);

    TaskResponse<AuthResponse> auth(AuthRequest authRequest);

    void safeCheck();
}
