package cn.kstry.framework.test.demo.goods.facade;

import cn.kstry.framework.core.facade.TaskRequest;

/**
 *
 * @author lykan
 */
public class UserLoginRequest implements TaskRequest {

    private Integer userType;

    private Long userId;

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
