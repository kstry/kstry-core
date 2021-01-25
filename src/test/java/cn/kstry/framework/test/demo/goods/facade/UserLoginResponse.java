package cn.kstry.framework.test.demo.goods.facade;

import cn.kstry.framework.core.annotation.NoticeStableField;
import cn.kstry.framework.test.demo.goods.entity.User;

/**
 *
 * @author lykan
 */
public class UserLoginResponse {

    @NoticeStableField(name = "user")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
