package cn.kstry.framework.test.demo.goods.facade;

import cn.kstry.framework.core.facade.TaskRequest;

import java.util.List;

/**
 *
 * @author lykan
 */
public class GetGoodsRequest implements TaskRequest {

    /**
     *
     */
    private List<Long> goodIds;

    public List<Long> getGoodIds() {
        return goodIds;
    }

    public void setGoodIds(List<Long> goodIds) {
        this.goodIds = goodIds;
    }
}
