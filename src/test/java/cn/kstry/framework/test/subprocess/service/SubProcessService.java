/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.test.subprocess.service;

import cn.kstry.framework.core.annotation.NoticeResult;
import cn.kstry.framework.core.annotation.StaTaskParam;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.test.subprocess.bo.SubProcessBo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author lykan
 */
@SuppressWarnings("unused")
@Slf4j
@TaskComponent(name = "sub-process-service")
public class SubProcessService {

    @TaskService(name = "say_empty_info")
    public void sayEmptyInfo(@StaTaskParam("tbo.a") int a, @StaTaskParam("tbo.b") long b, @StaTaskParam("tbo.c") boolean c,
                             @StaTaskParam("tbo.d") String d, @StaTaskParam("tbo.e") double e, @StaTaskParam("tbo.f") AtomicInteger f) {
        Assert.assertEquals(1, a);
        Assert.assertEquals(100L, b);
        Assert.assertTrue(c);
        Assert.assertEquals("subProcessTest", d);
        Assert.assertEquals(3.4d, e, 0);
        f.incrementAndGet();
    }

    @NoticeResult
    @TaskService(name = "say_info")
    public SubProcessBo sayInfo(@StaTaskParam("tbo.a") int a, @StaTaskParam("tbo.b") long b, @StaTaskParam("tbo.c") boolean c,
                                @StaTaskParam("tbo.d") String d, @StaTaskParam("tbo.e") double e, @StaTaskParam("tbo.f") AtomicInteger f) {
        Assert.assertEquals(1, a);
        Assert.assertEquals(100L, b);
        Assert.assertTrue(c);
        Assert.assertEquals("subProcessTest", d);
        Assert.assertEquals(3.4d, e, 0);
        SubProcessBo subProcessBo = new SubProcessBo();
        subProcessBo.setA(a);
        subProcessBo.setB(b);
        subProcessBo.setC(c);
        subProcessBo.setD(d);
        subProcessBo.setE(e);
        subProcessBo.setF(f);
        f.incrementAndGet();
        return subProcessBo;
    }

    @TaskService(name = "mono_say_empty_info")
    public Mono<Void> monoSayEmptyInfo(@StaTaskParam("tbo.a") int a, @StaTaskParam("tbo.b") long b, @StaTaskParam("tbo.c") boolean c,
                                       @StaTaskParam("tbo.d") String d, @StaTaskParam("tbo.e") double e, @StaTaskParam("tbo.f") AtomicInteger f) {
        Assert.assertEquals(1, a);
        Assert.assertEquals(100L, b);
        Assert.assertTrue(c);
        Assert.assertEquals("subProcessTest", d);
        Assert.assertEquals(3.4d, e, 0);
        f.incrementAndGet();
        return Mono.empty();
    }

    @NoticeResult
    @TaskService(name = "mono_say_info", targetType = SubProcessBo.class)
    public Mono<SubProcessBo> monoSayInfo(@StaTaskParam("tbo.a") int a, @StaTaskParam("tbo.b") long b, @StaTaskParam("tbo.c") boolean c,
                                          @StaTaskParam("tbo.d") String d, @StaTaskParam("tbo.e") double e, @StaTaskParam("tbo.f") AtomicInteger f) {
        Assert.assertEquals(1, a);
        Assert.assertEquals(100L, b);
        Assert.assertTrue(c);
        Assert.assertEquals("subProcessTest", d);
        Assert.assertEquals(3.4d, e, 0);
        SubProcessBo subProcessBo = new SubProcessBo();
        subProcessBo.setA(a);
        subProcessBo.setB(b);
        subProcessBo.setC(c);
        subProcessBo.setD(d);
        subProcessBo.setE(e);
        subProcessBo.setF(f);
        f.incrementAndGet();
        return Mono.just(subProcessBo);
    }

    @TaskService(name = "say_error")
    public void sayError() {
        throw new RuntimeException("测试子链路抛异常");
    }

    @TaskService(name = "say_timeout")
    public void sayTimeout() throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
    }
}
