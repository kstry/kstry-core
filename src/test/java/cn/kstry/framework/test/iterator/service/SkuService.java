/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.test.iterator.service;

import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.test.iterator.bo.SkuBo;
import com.alibaba.fastjson.JSON;
import org.junit.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
@TaskComponent(name = "sku-service")
public class SkuService {

    @NoticeScope(target = "sku-list", scope = ScopeTypeEnum.STABLE)
    @TaskService(name = "get-sku-list")
    public List<SkuBo> getSkuList() {
        List<SkuBo> collect = LongStream.range(1, 11).mapToObj(i -> {
            SkuBo skuBo = new SkuBo();
            skuBo.setId(i);
            return skuBo;
        }).collect(Collectors.toList());
        System.out.println(Thread.currentThread().getName() + " - init sku ->" + JSON.toJSONString(collect));
        return collect;
    }

    @NoticeResult
    @TaskService(name = "set-sku-name")
    public List<SkuBo> setSkuName(@StaTaskParam("sku-list") List<SkuBo> skuList, ScopeDataOperator dataOperator) {
        Optional<SkuBo> o = dataOperator.iterDataItem();
        o.ifPresent(bo -> {
            if (bo.getId() == 4) {
                throw new RuntimeException("加载名称失败了，但是无伤大雅，因为设置了best策略！");
            }
            bo.setName("SKU名称" + bo.getId());
            System.out.println(Thread.currentThread().getName() + " - set sku name ->" + JSON.toJSONString(bo));
        });
        return skuList;
    }

    @TaskService(name = "set-sku-img")
    public void setSkuImg(ScopeDataOperator dataOperator) {
        Assert.assertEquals("test-prop", dataOperator.getTaskProperty().orElse(null));
        Optional<SkuBo> o = dataOperator.iterDataItem();
        o.ifPresent(bo -> {
            bo.setImg("SKU图片" + bo.getId());
            System.out.println(Thread.currentThread().getName() + " - set sku img ->" + JSON.toJSONString(bo));
        });
    }
}
