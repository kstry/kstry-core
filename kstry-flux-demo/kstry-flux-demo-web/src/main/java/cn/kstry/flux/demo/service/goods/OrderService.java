/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.flux.demo.service.goods;

import cn.kstry.flux.demo.dto.goods.OrderInfo;
import cn.kstry.flux.demo.facade.goods.GoodsDetailRequest;
import cn.kstry.framework.core.annotation.NoticeScope;
import cn.kstry.framework.core.annotation.ReqTaskParam;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lykan
 */
@Slf4j
@TaskComponent
public class OrderService {

    @NoticeScope(scope = {ScopeTypeEnum.STABLE, ScopeTypeEnum.VARIABLE})
    @TaskService(desc = "获取订单信息")
    public OrderInfo getOrderInfo(@ReqTaskParam(GoodsDetailRequest.F.id) Long goodsId) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderedCount(10);
        log.info("goods id: {}, get OrderInfo: {}", goodsId, JSON.toJSONString(orderInfo));
        return orderInfo;
    }
}
