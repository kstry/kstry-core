/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.flux.demo.service.goods;

import cn.kstry.flux.demo.dto.goods.GoodsExtInfo;
import cn.kstry.flux.demo.facade.goods.GoodsDetailRequest;
import cn.kstry.framework.core.annotation.ReqTaskParam;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lykan
 */
@SuppressWarnings("unused")
@Slf4j
@TaskComponent(scanSuper = false)
public class GoodsServiceDemotion extends GoodsService {

    @TaskService
    public GoodsExtInfo getGoodsExtInfoDemotion(@ReqTaskParam(GoodsDetailRequest.F.id) Long goodsId) {
        GoodsExtInfo goodsExtInfo = new GoodsExtInfo();
        goodsExtInfo.setCollectCount(0);
        log.info("获取收藏数失败，执行降级方法。。。");
        return goodsExtInfo;
    }
}
