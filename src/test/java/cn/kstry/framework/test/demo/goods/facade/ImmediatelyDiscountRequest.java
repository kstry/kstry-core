package cn.kstry.framework.test.demo.goods.facade;

import cn.kstry.framework.core.facade.TaskRequest;
import cn.kstry.framework.test.demo.goods.entity.Goods;
import cn.kstry.framework.test.demo.goods.entity.User;

import java.util.List;

/**
 *
 * @author lykan
 */
public class ImmediatelyDiscountRequest implements TaskRequest {

    private List<Goods> goodsList;

    public List<Goods> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<Goods> goodsList) {
        this.goodsList = goodsList;
    }
}
