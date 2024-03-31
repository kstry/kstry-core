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
package cn.kstry.framework.test.diagram.service;

import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 *
 * @author lykan
 */
@Slf4j
@SuppressWarnings("unused")
@TaskComponent
public class SimpleAnnotationComponent {

    @TaskService(desc = "无参服务节点")
    public void method0() {
        log.info("method0...");
    }

    @TaskService
    public void method1(int a) {
        log.info("method1...");
    }

    @TaskService
    public void method2(int a, int b) {
        log.info("method2...");
    }

    @TaskService
    public void method3(int a, int b, String c) {
        log.info("method3...");
    }

    @TaskService
    public void method4(int a, int b, String c, byte d) {
        log.info("method4...");
    }

    @TaskService
    public void method5(int a, int b, String c, byte d, long e) {
        log.info("method5...");
    }

    @TaskService
    public void method6(int a, int b, String c, byte d, long e, Object f) {
        log.info("method6...");
    }

    @TaskService
    public void method7(int a, int b, String c, byte d, long e, Object f, float g) {
        log.info("method7...");
    }

    @TaskService
    public void method8(int a, int b, String c, byte d, long e, Object f, float g, int h) {
        log.info("method8...");
    }

    @TaskService
    public void method9(int a, int b, String c, byte d, long e, Object f, float g, int h, List<Object> i) {
        log.info("method9...");
    }

    @TaskService
    public void method10(int a, int b, String c, byte d, long e, Object f, float g, int h, List<Object> i, Boolean j) {
        log.info("method10...");
    }

    @TaskService(desc = "无参服务节点")
    public int methodR0() {
        log.info("methodR0...");
        return 0;
    }

    @TaskService
    public int methodR1(int a) {
        log.info("methodR1...");
        return 0;
    }

    @TaskService
    public int methodR2(int a, int b) {
        log.info("methodR2...");
        return 0;
    }

    @TaskService
    public int methodR3(int a, int b, String c) {
        log.info("methodR3...");
        return 0;
    }

    @TaskService
    public int methodR4(int a, int b, String c, byte d) {
        log.info("methodR4...");
        return 0;
    }

    @TaskService
    public long methodR5(int a, int b, String c, byte d, long e) {
        log.info("methodR5...");
        return 0L;
    }

    @TaskService
    public byte methodR6(int a, int b, String c, byte d, long e, Object f) {
        log.info("methodR6...");
        return 0;
    }

    @TaskService
    public List<String> methodR7(int a, int b, String c, byte d, long e, Object f, float g) {
        log.info("methodR7...");
        return null;
    }

    @TaskService
    public String methodR8(int a, int b, String c, byte d, long e, Object f, float g, int h) {
        log.info("methodR8...");
        return null;
    }

    @TaskService
    public int methodR9(int a, int b, String c, byte d, long e, Object f, float g, int h, List<Object> i) {
        log.info("methodR9...");
        return 0;
    }

    @TaskService
    public Object methodR10(int a, int b, String c, byte d, long e, Object f, float g, int h, List<Object> i, Boolean j) {
        log.info("methodR10...");
        return null;
    }
}
