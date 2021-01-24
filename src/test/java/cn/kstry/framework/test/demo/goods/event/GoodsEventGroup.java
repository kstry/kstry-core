package cn.kstry.framework.test.demo.goods.event;

import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.test.demo.goods.facade.GetGoodsRequest;
import cn.kstry.framework.test.demo.goods.facade.GetGoodsResponse;

/**
 * 商品系统
 *
 * @author lykan
 */
public interface GoodsEventGroup {

    TaskResponse<GetGoodsResponse> getGoods(GetGoodsRequest request);
}
