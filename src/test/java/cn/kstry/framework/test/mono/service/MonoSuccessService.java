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
package cn.kstry.framework.test.mono.service;

import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.test.mono.bo.SayInfoRequest;
import org.junit.Assert;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
@TaskComponent(name = "mono-service")
public class MonoSuccessService {

    @TaskService(name = "say_empty_info", invoke = @Invoke(demotion = "pr:mono-service@say_empty_info3_demotion@ability"))
    public Mono<Void> sayEmptyInfo(@ReqTaskParam(reqSelf = true) SayInfoRequest request, @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertEquals(345, a);
        Assert.assertEquals(true, b);
        Assert.assertEquals("Mono测试", c);
        request.setA(request.getA() + 1);
        return Mono.fromFuture(CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, Executors.newFixedThreadPool(1)));
    }

    @NoticeResult
    @TaskService(name = "say_info", invoke = @Invoke(demotion = "pr:mono-service@say_info2_demotion"))
    public Mono<SayInfoRequest> sayInfo(@ReqTaskParam(reqSelf = true) SayInfoRequest request,
                                        @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertTrue(a - 345 >= 0);
        Assert.assertEquals(true, b);
        Assert.assertEquals("Mono测试", c);
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return request;
        }, Executors.newFixedThreadPool(1)));
    }

    @TaskService(name = "say_empty_info2", invoke = @Invoke(retry = 1))
    public Mono<Void> sayEmptyInfo2(@ReqTaskParam(reqSelf = true) SayInfoRequest request,
                                    @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertEquals(true, b);
        Assert.assertEquals("Mono测试", c);
        try {
            int ac = 1 / request.getD();
        } catch (Exception e) {
            request.setD(request.getD() + 1);
            throw e;
        }
        return Mono.fromFuture(CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, Executors.newFixedThreadPool(1)));
    }

    @TaskService(name = "say_empty_info3", invoke = @Invoke(retry = 3, demotion = "pr:mono-service@say_empty_info3_demotion@ability"))
    public Mono<Void> sayEmptyInfo3(@ReqTaskParam(reqSelf = true) SayInfoRequest request,
                                    @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertEquals(true, b);
        Assert.assertEquals("Mono测试", c);
        int ac = 1 / request.getD();
        return Mono.fromFuture(CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, Executors.newFixedThreadPool(1)));
    }

    @TaskService(name = "say_empty_info4", invoke = @Invoke(retry = 3, demotion = "pr:mono-service@say_empty_info4_demotion"))
    public Mono<Void> sayEmptyInfo4(@ReqTaskParam(reqSelf = true) SayInfoRequest request,
                                    @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertEquals(true, b);
        Assert.assertEquals("Mono测试", c);
        int ac = 1 / request.getD();
        return Mono.fromFuture(CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, Executors.newFixedThreadPool(1)));
    }

    @TaskService(name = "say_empty_info5", invoke = @Invoke(demotion = "pr:mono-service@say_empty_info3_demotion@ability"))
    public Mono<Void> sayEmptyInfo5(@ReqTaskParam(reqSelf = true) SayInfoRequest request,
                                    @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertEquals(true, b);
        Assert.assertEquals("Mono测试", c);
        System.out.println("say_empty_info5...");
        return Mono.fromFuture(CompletableFuture.runAsync(() -> {
            int ac = 1 / request.getD();
        }, Executors.newFixedThreadPool(1)));
    }

    @NoticeResult
    @TaskService(name = "say_info2", invoke = @Invoke(demotion = "pr:mono-service@say_info2_demotion"))
    public Mono<SayInfoRequest> sayInfo2(@ReqTaskParam(reqSelf = true) SayInfoRequest request,
                                         @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertTrue(a - 345 >= 0);
        Assert.assertEquals(true, b);
        Assert.assertEquals("Mono测试", c);
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("测试Mono方法体执行失败后的降级");
        }, Executors.newFixedThreadPool(1)));
    }

    @TaskService(name = "say_empty_info6")
    public Mono<Void> sayEmptyInfo6(@ReqTaskParam(reqSelf = true) SayInfoRequest request,
                                    @ReqTaskParam int a, @ReqTaskParam Boolean b, @ReqTaskParam String c) {
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Assert.assertEquals(345, a);
        Assert.assertEquals(true, b);
        Assert.assertEquals("Mono测试", c);
        request.setA(request.getA() + 1);
        return Mono.fromFuture(CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, Executors.newFixedThreadPool(1)));
    }
}
