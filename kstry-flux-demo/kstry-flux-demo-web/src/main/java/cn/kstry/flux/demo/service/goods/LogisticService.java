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

import cn.kstry.flux.demo.dto.goods.LogisticInsurance;
import cn.kstry.flux.demo.facade.goods.GetLogisticInsuranceRequest;
import cn.kstry.framework.core.annotation.NoticeSta;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import lombok.extern.slf4j.Slf4j;

/**
 * 物流
 *
 * @author lykan
 */
@Slf4j
@SuppressWarnings("unused")
@TaskComponent
public class LogisticService {

    @NoticeSta
    @TaskService(desc = "运费险")
    public LogisticInsurance getLogisticInsurance(GetLogisticInsuranceRequest request) {
//        TimeUnit.MILLISECONDS.sleep(100L);
        log.info("request source：{}, getLogisticInsurance...", request.getSource());
        LogisticInsurance logisticInsurance = new LogisticInsurance();
        logisticInsurance.setDesc("运费险描述");
        logisticInsurance.setType(1);
        return logisticInsurance;
    }
}
