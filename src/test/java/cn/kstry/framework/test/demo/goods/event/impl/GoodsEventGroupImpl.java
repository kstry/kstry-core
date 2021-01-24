package cn.kstry.framework.test.demo.goods.event.impl;

import cn.kstry.framework.core.annotation.EventGroupComponent;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.facade.TaskResponseBox;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.test.demo.goods.entity.Goods;
import cn.kstry.framework.test.demo.goods.event.GoodsEventGroup;
import cn.kstry.framework.test.demo.goods.facade.GetGoodsRequest;
import cn.kstry.framework.test.demo.goods.facade.GetGoodsResponse;
import cn.kstry.framework.test.demo.goods.role.GoodsRole;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author lykan
 */
@EventGroupComponent(eventGroupName = "GOODS_EVENT_GROUP", operatorRoleClass = GoodsRole.class)
public class GoodsEventGroupImpl implements GoodsEventGroup {

    private final List<Goods> goodsList = Lists.newArrayList(
            new Goods(1L, "商品1", 1000),
            new Goods(2L, "商品2", 500),
            new Goods(3L, "商品3", 6000),
            new Goods(4L, "商品4", 100),
            new Goods(5L, "商品5", 400000));

    @Override
    public TaskResponse<GetGoodsResponse> getGoods(GetGoodsRequest request) {
        AssertUtil.notEmpty(request.getGoodIds());

        List<Goods> collect = goodsList.stream().filter(s -> request.getGoodIds().contains(s.getId())).collect(Collectors.toList());
        GetGoodsResponse response = new GetGoodsResponse();
        response.setGoodsList(collect);
        return TaskResponseBox.buildSuccess(response);
    }
}
