package cn.kstry.framework.test.demo.goods.facade;

import cn.kstry.framework.core.facade.TaskRequest;
import cn.kstry.framework.test.demo.goods.entity.User;

/**
 *
 * @author lykan
 */
public class AuthRequest implements TaskRequest {

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
