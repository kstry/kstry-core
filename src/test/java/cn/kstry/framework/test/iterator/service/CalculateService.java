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
import cn.kstry.framework.core.bus.IterDataItem;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.component.expression.Exp;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @NoticeScope(target = "squareResult.a", scope = {ScopeTypeEnum.RESULT, ScopeTypeEnum.STABLE})
    public Mono<Integer> square(ScopeDataOperator dataOperator, IterDataItem<Integer> data) {
        Optional<Integer> iterDataItem = dataOperator.iterDataItem();
        Optional<Integer> data1 = data.getData();
        CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(230);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            AssertUtil.equals(iterDataItem.orElse(null), data1.orElse(null));
            return iterDataItem.map(i -> i * i).orElse(null);
        }, Executors.newFixedThreadPool(1));
        return Mono.fromFuture(integerCompletableFuture);
    }

    /**
     * 求平方再放回
     */
    @NoticeResult
    @TaskService(name = "batch-square", iterator = @Iterator(sourceScope = ScopeTypeEnum.REQUEST, source = "numList", stride = 3, async = true, alignIndex = true))
    public List<Integer> batchSquare(ScopeDataOperator dataOperator, IterDataItem<Integer> data) {
        Optional<List<Integer>> iterDataItem = dataOperator.iterDataItem();
        System.out.println("index ->" + data.getIndex());
        AssertUtil.equals(iterDataItem.map(CollectionUtils::size).orElse(null), Optional.of(data.getDataList()).map(CollectionUtils::size).orElse(null));
        return data.getDataList().stream().map(i -> i * i).collect(Collectors.toList());
    }

    /**
     * 求平方再放回
     */
    @TaskService(name = "batch-square2")
    @NoticeScope(target = "squareResult.a", scope = {ScopeTypeEnum.RESULT, ScopeTypeEnum.STABLE})
    public Mono<List<Integer>> batchSquare2(ScopeDataOperator dataOperator, IterDataItem<Integer> data) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(230);
        Optional<List<Integer>> iterDataItem = dataOperator.iterDataItem();
        AssertUtil.equals(iterDataItem.map(CollectionUtils::size).orElse(null), Optional.of(data.getDataList()).map(CollectionUtils::size).orElse(null));
        return Mono.just(data.getDataList().stream().map(i -> i * i).collect(Collectors.toList()));
    }

    /**
     * 求平方再放回，测试 IterateStrategyEnum.BEST_SUCCESS 策略
     */
    @NoticeResult
    @TaskService(name = "square-best-strategy", iterator = @Iterator(alignIndex = true))
    public Integer squareBestStrategy(ScopeDataOperator dataOperator, IterDataItem<Integer> data) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(230);
        Optional<Integer> iterDataItem = data.getData();
        Integer integer = iterDataItem.orElse(-1);
        if (integer == 4) {
            throw new RuntimeException("系统中偶发的异常！！！");
        }
        return integer * integer;
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
