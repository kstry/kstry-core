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

import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.component.expression.Exp;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
@TaskComponent(name = "calculate-service")
public class CalculateService {

    /**
     * 求平方再放回
     */
    @TaskService(name = "square")
    public void square(ScopeDataOperator dataOperator) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(230);
        Optional<Integer> iterDataItem = dataOperator.iterDataItem();
        iterDataItem.ifPresent(i ->
                dataOperator.computeIfAbsent(Exp.b(e -> e.sta("squareResult", "a")), Lists::newCopyOnWriteArrayList).ifPresent(list -> {
                    list.add(i * i);
                    dataOperator.setResult(list);
                })
        );
    }

    /**
     * 求平方再放回
     */
    @TaskService(name = "batch-square")
    public void batchSquare(ScopeDataOperator dataOperator) {
        Optional<List<Integer>> iterDataItem = dataOperator.iterDataItem();
        iterDataItem.ifPresent(ts -> ts.forEach(i ->
                dataOperator.computeIfAbsent(ScopeTypeEnum.RESULT.getKey(), Lists::newCopyOnWriteArrayList).ifPresent(list -> {
                    list.add(i * i);
                    dataOperator.setResult(list);
                })
        ));
    }

    /**
     * 求平方再放回
     */
    @TaskService(name = "batch-square2")
    public void batchSquare2(ScopeDataOperator dataOperator) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(230);
        Optional<List<Integer>> iterDataItem = dataOperator.iterDataItem();
        iterDataItem.ifPresent(ds -> ds.forEach(i ->
                        dataOperator.computeIfAbsent(Exp.b(e -> e.sta("squareResult", "a")), Lists::newCopyOnWriteArrayList).ifPresent(list -> {
                            list.add(i * i);
                            dataOperator.setResult(list);
                        })
                )
        );
    }

    /**
     * 求平方再放回，测试 IterateStrategyEnum.BEST_SUCCESS 策略
     */
    @TaskService(name = "square-best-strategy")
    public void squareBestStrategy(ScopeDataOperator dataOperator) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(230);
        Optional<Integer> iterDataItem = dataOperator.iterDataItem();
        iterDataItem.ifPresent(i ->
                dataOperator.computeIfAbsent(ScopeTypeEnum.RESULT.getKey(), Lists::newCopyOnWriteArrayList).ifPresent(list -> {
                    if (i == 4) {
                        throw new RuntimeException("系统中偶发的异常！！！");
                    }
                    list.add(i * i);
                })
        );
    }

    /**
     * 求平方再放回，测试 IterateStrategyEnum.ANY_SUCCESS 策略
     */
    @TaskService(name = "square-any-strategy")
    public void squareAnyStrategy(ScopeDataOperator dataOperator) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(230);
        Optional<Integer> iterDataItem = dataOperator.iterDataItem();
        iterDataItem.ifPresent(i ->
                dataOperator.computeIfAbsent(Exp.b(e -> e.var("squareResult")), Lists::newCopyOnWriteArrayList).ifPresent(list -> {
                    if (i == 0 || i == 1 || i == 2) {
                        throw new RuntimeException("系统中偶发的异常！！！");
                    }
                    list.add(i * i);
                    dataOperator.setResult(list);
                })
        );
    }
}
