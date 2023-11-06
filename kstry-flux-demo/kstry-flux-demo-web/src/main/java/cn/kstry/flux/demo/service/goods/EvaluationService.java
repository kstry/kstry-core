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

import cn.kstry.flux.demo.dto.goods.EvaluationInfo;
import cn.kstry.flux.demo.facade.goods.GoodsDetailRequest;
import cn.kstry.framework.core.annotation.NoticeSta;
import cn.kstry.framework.core.annotation.ReqTaskParam;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lykan
 */
@Slf4j
@TaskComponent
public class EvaluationService {

    @NoticeSta
    @TaskService(desc = "获取评价信息")
    public EvaluationInfo getEvaluationInfo(@ReqTaskParam(GoodsDetailRequest.F.id) Long goodsId) {
        EvaluationInfo evaluationInfo = new EvaluationInfo();
        evaluationInfo.setEvaluateCount(20);
//        TimeUnit.MILLISECONDS.sleep(200L);
        log.info("goods id: {}, get EvaluationInfo: {}", goodsId, JSON.toJSONString(evaluationInfo));
        return evaluationInfo;
    }
}
