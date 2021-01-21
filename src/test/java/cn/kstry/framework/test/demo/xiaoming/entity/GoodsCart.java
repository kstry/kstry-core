package cn.kstry.framework.test.demo.xiaoming.entity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lykan
 */
public class GoodsCart {

    private final List<Goods> goodsList = new ArrayList<>();

    public List<Goods> getGoodsList() {
        return goodsList;
    }

    public void addGoods(Goods goods) {
        this.goodsList.add(goods);
    }
}
