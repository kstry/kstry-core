/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.test.flow.service;

import cn.kstry.framework.core.annotation.Invoke;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;

import java.util.concurrent.TimeUnit;

/**
 * @author lykan
 */
@SuppressWarnings("unused")
@TaskComponent(name = "hello_service")
public class HelloService {

    @TaskService(name = "say_hello", invoke = @Invoke(demotion = "pr:hello_service@say_hello_demotion"))
    public void sayHello() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(200);
        System.out.println("say hello ~");
    }

    @TaskService(name = "say_hello_demotion")
    public int sayHelloDemotion() {
        return -1;
    }

    @TaskService(name = "say_hello1", invoke = @Invoke(retry = 2, timeout = 100, demotion = "pr:hello_service@say_hello11"))
    public void sayHello1(int a) {
        System.out.println("say hello1 ~ " + a);
        throw new RuntimeException("aaaaaaaaaaaaaaaaaaa");
    }

    @TaskService(name = "say_hello11")
    public void sayHello11(int a) {
        System.out.println("say hello11 ~ " + a);
    }

    @TaskService(name = "say_hello2")
    public void sayHello2() {
        System.out.println("say hello2 ~");
    }
}
