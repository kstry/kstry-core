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
package cn.kstry.framework.test.load;

import cn.kstry.framework.core.annotation.EnableKstry;

/**
 * @author lykan
 */
public class TestLoadConfiguration {

    /**
     * 【正常】测试：未指定bpmnPath
     */
    @EnableKstry
    public static class LoadBpmnConfigTest001 {

    }

    /**
     * 【异常】测试：BPMN 文件读取失败
     */
    @EnableKstry(bpmnPath = "./bpmn/load/load_bpmn_002.bpmn")
    public static class LoadBpmnConfigTest002 {

    }

    /**
     * 【异常】测试：配置文件中存在两个相同ID的子流程
     */
    @EnableKstry(bpmnPath = "classpath:bpmn/load/load_bpmn_0030*.bpmn")
    public static class LoadBpmnConfigTest003 {

    }

    /**
     * 【异常】测试：配置文件中出现两个相同Id的 StartEvent
     */
    @EnableKstry(bpmnPath = "classpath:bpmn/load/load_bpmn_0040*.bpmn")
    public static class LoadBpmnConfigTest004 {

    }

    /**
     * 【异常】测试：链路中存在元素相互依赖的情况
     */
    @EnableKstry(bpmnPath = "classpath:bpmn/load/load_bpmn_005.bpmn")
    public static class LoadBpmnConfigTest005 {

    }

    /**
     * 【异常】测试：服务节点有两个入度
     */
    @EnableKstry(bpmnPath = "./bpmn/load/load_bpmn_006.bpmn")
    public static class LoadBpmnConfigTest006 {

    }

    /**
     * 【异常】测试：排他网关有两个入度
     */
    @EnableKstry(bpmnPath = "./bpmn/load/load_bpmn_007.bpmn")
    public static class LoadBpmnConfigTest007 {

    }
}
