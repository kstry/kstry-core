package cn.kstry.framework.test.demo.goods.facade;

import java.util.List;

/**
 *
 * @author lykan
 */
public class BuyGoodsRequest {

    private Long userId;

    private Integer userType;

    private List<Long> goodIds;

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

    public List<Long> getGoodIds() {
        return goodIds;
    }

    public void setGoodIds(List<Long> goodIds) {
        this.goodIds = goodIds;
    }
}
