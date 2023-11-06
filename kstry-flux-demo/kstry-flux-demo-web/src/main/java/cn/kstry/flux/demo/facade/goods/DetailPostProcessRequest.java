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
package cn.kstry.flux.demo.facade.goods;

import cn.kstry.flux.demo.dto.goods.*;
import cn.kstry.flux.demo.service.goods.ShopService;
import cn.kstry.framework.core.annotation.SpringInitialization;
import cn.kstry.framework.core.annotation.StaTaskField;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.engine.ParamLifecycle;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lykan
 */
@Data
@SpringInitialization
public class DetailPostProcessRequest implements ParamLifecycle {

    @Resource
    private ShopService shopService;

    @StaTaskField(ShowGoodsSta.SHOP_INFO)
    private ShopInfo shopInfo;

    //    @NotNull
    @StaTaskField(ShowGoodsSta.GOODS_DETAIL)
    private GoodsDetail goodsDetail;

    @StaTaskField
    private LogisticInsurance logisticInsurance;

    @StaTaskField
    private EvaluationInfo evaluationInfo;

    @StaTaskField
    private OrderInfo orderInfo;

    @StaTaskField
    private GoodsExtInfo goodsExtInfo;

    @Override
    public void before(ScopeDataOperator scopeDataOperator) {
        ParamLifecycle.super.before(scopeDataOperator);
    }

    @Override
    public void after(ScopeDataOperator scopeDataOperator) {
        if (shopInfo != null) {
            shopService.makeLabel(shopInfo.getLabels());
        }
    }
}
