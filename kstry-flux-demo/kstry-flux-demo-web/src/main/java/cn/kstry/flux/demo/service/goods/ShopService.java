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

import cn.kstry.flux.demo.facade.goods.ShowGoodsSta;
import cn.kstry.flux.demo.dto.goods.ShopInfo;
import cn.kstry.flux.demo.dto.goods.ShopLabel;
import cn.kstry.flux.demo.facade.goods.GoodsDetailRequest;
import cn.kstry.framework.core.annotation.NoticeSta;
import cn.kstry.framework.core.annotation.ReqTaskParam;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.kv.KvAbility;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author lykan
 */
@SuppressWarnings("unused")
@Slf4j
@TaskComponent
public class ShopService {

    private final Map<Long, ShopInfo> goodsIdShopInfoMapping = new HashMap<Long, ShopInfo>() {
        {
            ShopInfo shopInfo = new ShopInfo();
            shopInfo.setShopName("店铺名称");
            shopInfo.setId(1L);
            shopInfo.setSalesNumMonthly(22);
            put(1L, shopInfo);
        }
    };

    private final CopyOnWriteArrayList<CompletableFuture<ShopInfo>> list = new CopyOnWriteArrayList<>();

    @Resource
    private KvAbility kvAbility;

//    @TaskService(name = ShopCompKey.getShopInfoByGoodsId)
//    public ShopInfo getShopInfoByGoodsId(@ReqTaskParam("id") Long goodsId) throws InterruptedException {
//        TimeUnit.MILLISECONDS.sleep(500L);
//        ShopInfo shopInfo = goodsIdShopInfoMapping.get(goodsId);
//        log.info("goods id: {}, getShopInfoByGoodsId: {}", goodsId, JSON.toJSONString(shopInfo));
//        return shopInfo;
//    }

    @NoticeSta(target = ShowGoodsSta.SHOP_INFO)
    @TaskService(desc = "获取店铺信息", kvScope = "init-shop-info")
    public Mono<ShopInfo> getShopInfoByGoodsId(@ReqTaskParam(GoodsDetailRequest.F.id) Long goodsId) {

        List<ShopLabel> labels = kvAbility.getList("labels", ShopLabel.class);

        synchronized (this) {
            CompletableFuture<ShopInfo> future = new CompletableFuture<>();
            list.add(future);
            Mono<ShopInfo> shopInfoMono = Mono.fromFuture(future);
            shopInfoMono.subscribe(shopInfo -> shopInfo.setLabels(labels));
            return shopInfoMono;
        }
    }

    public void makeLabel(List<ShopLabel> labels) {
        System.out.println("makeLabel... " + JSON.toJSONString(labels));
    }

    @Scheduled(fixedDelay = 1000)
    public void init() {
        synchronized (this) {
            List<CompletableFuture<ShopInfo>> completableFutures = Lists.newArrayList(list);
            list.clear();
            for (CompletableFuture<ShopInfo> cf : completableFutures) {
                ShopInfo shopInfo = goodsIdShopInfoMapping.get(1L);
                log.info("goods id: {}, getShopInfoByGoodsId: {}", 1, JSON.toJSONString(shopInfo));
                cf.complete(shopInfo);
            }
        }
    }
}
