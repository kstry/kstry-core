package cn.kstry.framework.test.flow.service;

import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.test.flow.bo.Goods;
import cn.kstry.framework.test.flow.bo.Hospital;
import cn.kstry.framework.test.flow.bo.Te4Request;
import com.alibaba.fastjson.JSON;
import reactor.core.publisher.Mono;

@SuppressWarnings("unused")
@TaskComponent(name = "goods_service")
public class GoodsService {

    @NoticeScope(scope = {ScopeTypeEnum.STABLE}, target = "goods")
    @TaskService(name = "get_goods")
    public Goods getGoods(@ReqTaskParam("goodsId") Long id) {
        Goods goods = new Goods();
        goods.setId(id);
        goods.setName("商品名称");
        goods.setPrice(20L);
        return goods;
    }

    @TaskService(name = "fill_goods")
    public Mono<Void> fillGoods(@StaTaskParam("goods") Goods goods, Hospital hospital) {
        goods.setHospital(hospital);
        System.out.println("thread ->" + Thread.currentThread().getName() + ", fill goods ->" + JSON.toJSONString(goods));
        return Mono.empty();
    }

    @TaskService(name = "say_request")
    public void sayRequest(@ReqTaskParam(reqSelf = true) Te4Request request) {
        request.increase();
//        System.out.println("thread ->" + Thread.currentThread().getName() + ", request ->" + JSON.toJSONString(request));
    }

    @TaskService(name = "check_params")
    public void checkParams(Goods goods) {
        assert goods != null && goods.getId() != null;
    }
}
