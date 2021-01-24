package cn.kstry.framework.test.demo.goods.facade;

import cn.kstry.framework.test.demo.goods.entity.Goods;

import java.util.List;

/**
 *
 * @author lykan
 */
public class GetGoodsResponse {

    private List<Goods> goodsList;

    public List<Goods> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<Goods> goodsList) {
        this.goodsList = goodsList;
    }
}
