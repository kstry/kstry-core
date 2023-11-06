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

import cn.kstry.flux.demo.facade.goods.CheckInfo;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.kv.KvAbility;
import cn.kstry.framework.core.util.AssertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 风控
 *
 * @author lykan
 */
@SuppressWarnings("unused")
@Slf4j
@TaskComponent
public class RiskControlService {

    @Resource
    private KvAbility kvAbility;

    @TaskService(desc = "校验图片", kvScope = "risk-control")
    public void checkImg(CheckInfo checkInfo) {

        AssertUtil.notNull(checkInfo);
        AssertUtil.notBlank(checkInfo.getImg());
        log.info("check img: {}, size: {}", checkInfo.getImg(), kvAbility.getString("img-max-size").orElse(null));
    }

    @TaskService(name = "checkImg", kvScope = "risk-control", ability = "triple")
    public void tripleCheckImg(CheckInfo checkInfo) {

        AssertUtil.notNull(checkInfo);
        AssertUtil.notBlank(checkInfo.getImg());
        log.info("triple check img: {}, size: {}", checkInfo.getImg(), kvAbility.getString("img-max-size").orElse(null));
    }

    @TaskService(desc = "风控统计")
    public void statistics(CheckInfo checkInfo) {
        AssertUtil.notNull(checkInfo);
        AssertUtil.notBlank(checkInfo.getImg());
        log.info("statistics triple service: " + checkInfo.getImg());
    }
}
