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
package cn.kstry.flux.demo.web;

import cn.kstry.flux.demo.dto.goods.GoodsDetail;
import cn.kstry.flux.demo.facade.R;
import cn.kstry.flux.demo.facade.goods.GoodsDetailRequest;
import cn.kstry.flux.demo.process.DefProcess;
import cn.kstry.flux.demo.util.WebUtil;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * @author lykan
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private StoryEngine storyEngine;

    @PostMapping("/show")
    public Mono<R<GoodsDetail>> showGoods(@RequestBody GoodsDetailRequest request) {
        StoryRequest<GoodsDetail> req = ReqBuilder.returnType(GoodsDetail.class)
                .timeout(3000).startProcess(DefProcess::buildShowGoodsLink).trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).build();
        req.setBusinessId(Optional.ofNullable(request.getBusinessId()).filter(StringUtils::isNotBlank).orElse(null));
        req.setRecallStoryHook(WebUtil::recallStoryHook);
        Mono<GoodsDetail> fireAsync = storyEngine.fireAsync(req);
        return WebUtil.dataDecorate(request, fireAsync);
    }
}
