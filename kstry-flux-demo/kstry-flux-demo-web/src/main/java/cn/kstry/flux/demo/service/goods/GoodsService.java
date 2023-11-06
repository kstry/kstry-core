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

import cn.kstry.flux.demo.dto.goods.*;
import cn.kstry.flux.demo.facade.goods.ShowGoodsSta;
import cn.kstry.flux.demo.facade.goods.DetailPostProcessRequest;
import cn.kstry.flux.demo.facade.goods.GoodsDetailRequest;
import cn.kstry.flux.demo.facade.goods.InitSkuResponse;
import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.kv.KvAbility;
import cn.kstry.framework.core.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author lykan
 */
@SuppressWarnings("unused")
@Slf4j
@TaskComponent
public class GoodsService {

    @Resource
    private KvAbility kvAbility;

    @NoticeScope(target = ShowGoodsSta.GOODS_DETAIL, scope = {ScopeTypeEnum.RESULT, ScopeTypeEnum.STABLE})
    @TaskService(desc = "初始化商品信息")
    public GoodsDetail initBaseInfo(@ReqTaskParam(reqSelf = true) GoodsDetailRequest request) {
        GoodsDetail d = GoodsDetail.builder().id(request.getId()).name("商品").img("https://xxx.png").needEvaluate(true).build();
        log.info("goods id: {}, initBaseInfo: {}", request.getId(), JSON.toJSONString(d));
        return d;
    }

    @TaskService(desc = "加载SKU信息")
    public InitSkuResponse initSku(@ReqTaskParam(GoodsDetailRequest.F.id) Long goodsId) {
        AssertUtil.equals(goodsId, 1L);

        SkuInfo sku1 = new SkuInfo();
        sku1.setSkuId(100L);
        sku1.setSkuName("sku1");
        sku1.setImg("https://xxx.png");
        sku1.setPrice(1000L);
        sku1.setStock(10);

        SkuInfo sku2 = new SkuInfo();
        sku2.setSkuId(101L);
        sku2.setSkuName("sku2");
        sku2.setImg("https://xxx2.png");
        sku2.setPrice(2000L);
        sku2.setStock(20);

        ArrayList<SkuInfo> skuInfos = Lists.newArrayList(sku1, sku2);
        log.info("goods id: {}, initSku: {}", goodsId, JSON.toJSONString(skuInfos));
        return InitSkuResponse.builder().skuInfos(skuInfos).build();
    }

    @TaskService(desc = "商品信息后置处理")
    public void detailPostProcess(DetailPostProcessRequest request) {
        GoodsDetail goodsDetail = request.getGoodsDetail();
        ShopInfo shopInfo = request.getShopInfo();
        if (shopInfo != null) {
            goodsDetail.setShopInfo(shopInfo);
        }
        LogisticInsurance logisticInsurance = request.getLogisticInsurance();
        if (logisticInsurance != null) {
            goodsDetail.setLogisticInsurance(logisticInsurance);
        }

        GoodsExtInfo goodsExtInfo = request.getGoodsExtInfo();
        OrderInfo orderInfo = request.getOrderInfo();
        EvaluationInfo evaluationInfo = request.getEvaluationInfo();
        goodsDetail.setStatistics(Lists.newArrayList(goodsExtInfo.getCollectCount(), orderInfo.getOrderedCount(), evaluationInfo.getEvaluateCount()));
        log.info("goods id: {}, detailPostProcess...", request.getGoodsDetail().getId());

        // 从 default 域，获取店铺黑名单
        List<String> list = kvAbility.getList("shop-blacklist-ids", String.class);
        String banner = kvAbility.getObject("banner", String.class).orElse(null);
        log.info("shop-blacklist-ids: {}", JSON.toJSONString(list));
        log.info("banner: {}", banner);
    }

    @NoticeSta
    @TaskService(desc = "获取额外信息", invoke = @Invoke(retry = 2, demotion = "pr:goodsServiceDemotion@getGoodsExtInfoDemotion", timeout = 500, strictMode = false))
    public GoodsExtInfo getGoodsExtInfo(@ReqTaskParam(GoodsDetailRequest.F.id) Long goodsId) {
        GoodsExtInfo goodsExtInfo = new GoodsExtInfo();
        goodsExtInfo.setCollectCount(30);
        int i = Math.abs(new Random().nextInt()) % 1000;
        try {
            TimeUnit.MILLISECONDS.sleep(i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("goods id: {}, get GoodsExtInfo: {}", goodsId, JSON.toJSONString(goodsExtInfo));
        return goodsExtInfo;
    }
}
