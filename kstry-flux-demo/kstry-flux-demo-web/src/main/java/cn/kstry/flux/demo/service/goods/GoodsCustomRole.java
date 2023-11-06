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

import cn.kstry.flux.demo.dto.goods.GoodsDetail;
import cn.kstry.framework.core.annotation.CustomRole;
import cn.kstry.framework.core.annotation.StaTaskParam;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.role.BasicRole;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.KeyUtil;
import cn.kstry.framework.core.util.LambdaUtil;
import cn.kstry.framework.core.util.PermissionUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;


/**
 * @author lykan
 */
@Slf4j
@TaskComponent(name = "goods-custom-role")
public class GoodsCustomRole {

    @CustomRole
    @TaskService(desc = "角色自定义", name = "goods-detail")
    public void goodsDetail(@StaTaskParam GoodsDetail goodsDetail, Role role) {

        if (goodsDetail != null && role != null) {
            BasicRole basicRole = new BasicRole();
            basicRole.addPermission(PermissionUtil.permissionList(KeyUtil.r(LambdaUtil.getServiceName(GoodsService::initSku))));
            role.addParentRole(Sets.newHashSet(basicRole));

            log.info("add permission: {}", JSON.toJSONString(basicRole));
        }
    }
}
