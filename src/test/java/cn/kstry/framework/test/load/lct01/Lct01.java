/*
 *
 *  * Copyright (c) 2020-2022, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.test.load.lct01;

import cn.kstry.framework.core.annotation.EnableKstry;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * @author lykan
 */
@SuppressWarnings("unused")
public class Lct01 {

    /**
     * 【异常】测试：服务节点重复定义
     */
    @EnableKstry
    @ComponentScan(basePackageClasses = {Lct01.class})
    public static class LoadComponentTest001 {

    }

    @TaskComponent(name = "service-duplication")
    public interface ServiceDuplication {

    }

    @Component
    public static class ServiceDuplication1 implements ServiceDuplication {

        @TaskService(name = "tc-service")
        public void c1() {

        }
    }

    @Component
    public static class ServiceDuplication2 implements ServiceDuplication {

        @TaskService(name = "tc-service")
        public void c2() {

        }
    }
}
