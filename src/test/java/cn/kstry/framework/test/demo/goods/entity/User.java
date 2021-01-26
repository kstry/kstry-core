package cn.kstry.framework.test.demo.goods.entity;

import cn.kstry.framework.test.demo.goods.enums.UserTypeEnum;

/**
 *
 * @author lykan
 */
public class User {

    private Long userId;

    private String name;

    private long money;

    private UserTypeEnum userTypeEnum;

    public User() {

    }

    public User(Long userId, String name, long money) {
        this.userId = userId;
        this.name = name;
        this.money = money;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public UserTypeEnum getUserTypeEnum() {
        return userTypeEnum;
    }

    public void setUserTypeEnum(UserTypeEnum userTypeEnum) {
        this.userTypeEnum = userTypeEnum;
    }
}
