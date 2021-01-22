package cn.kstry.framework.test.demo.goods.facade;

/**
 *
 * @author lykan
 */
public class BuyGoodsRequest {

    private Long userId;

    private Integer userType;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }
}
