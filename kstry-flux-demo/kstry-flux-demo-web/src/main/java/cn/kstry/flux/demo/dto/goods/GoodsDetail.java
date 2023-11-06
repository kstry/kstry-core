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
package cn.kstry.flux.demo.dto.goods;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.List;

/**
 * @author lykan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(innerTypeName = "F")
public class GoodsDetail {

    private Long id;

    private String name;

    private String img;

    private boolean needEvaluate;

    /**
     * Sku 组件执行完成之后，会将结果被直通知到 skuInfos 字段
     */
    private List<SkuInfo> skuInfos;

    /**
     * Shop 组件执行完成后，会将 shopInfo 保存至 sta 域，后面从 sta 域获取赋值
     */
    private ShopInfo shopInfo;

    /**
     * 运费险
     */
    private LogisticInsurance logisticInsurance;

    private List<Integer> statistics;
}
