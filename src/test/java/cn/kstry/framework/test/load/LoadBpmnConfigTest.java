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

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.ResourceException;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

/**
 *
 * @author lykan
 */

public class LoadBpmnConfigTest {

    /**
     * 【正常】测试：未指定bpmnPath
     */
    @Test
    public void loadTest001() {
        Exception err = null;
        try {
            new AnnotationConfigApplicationContext(TestLoadConfiguration.LoadBpmnConfigTest001.class);
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        AssertUtil.isNull(err);
    }

    /**
     * 【异常】测试：BPMN 文件读取失败
     */
    @Test
    public void loadTest002() {
        Exception err = null;
        try {
            new AnnotationConfigApplicationContext(TestLoadConfiguration.LoadBpmnConfigTest002.class);
        } catch (Exception e) {
            err = e;
        }
        Optional<ResourceException> exception = ExceptionUtil.getErrFromCause(err, ResourceException.class);
        Assert.assertTrue(exception.isPresent());

        exception.get().printStackTrace();
        String errorCode = exception.get().getErrorCode();
        Assert.assertEquals(ExceptionEnum.CONFIGURATION_PARSE_FAILURE.getExceptionCode(), errorCode);
    }

    /**
     * 【异常】测试：配置文件中存在两个相同ID的子流程
     */
    @Test
    public void loadTest003() {
        Exception err = null;
        try {
            new AnnotationConfigApplicationContext(TestLoadConfiguration.LoadBpmnConfigTest003.class);
        } catch (Exception e) {
            err = e;
        }
        Optional<ResourceException> exception = ExceptionUtil.getErrFromCause(err, ResourceException.class);
        Assert.assertTrue(exception.isPresent());

        exception.get().printStackTrace();
        String errorCode = exception.get().getErrorCode();
        Assert.assertEquals(ExceptionEnum.ELEMENT_DUPLICATION_ERROR.getExceptionCode(), errorCode);
    }

    /**
     * 【异常】测试：配置文件中出现两个相同Id的 StartEvent
     */
    @Test
    public void loadTest004() {
        Exception err = null;
        try {
            new AnnotationConfigApplicationContext(TestLoadConfiguration.LoadBpmnConfigTest004.class);
        } catch (Exception e) {
            err = e;
        }
        Optional<ResourceException> exception = ExceptionUtil.getErrFromCause(err, ResourceException.class);
        Assert.assertTrue(exception.isPresent());

        exception.get().printStackTrace();
        String errorCode = exception.get().getErrorCode();
        Assert.assertEquals(ExceptionEnum.ELEMENT_DUPLICATION_ERROR.getExceptionCode(), errorCode);
    }

    /**
     * 【异常】测试：链路中存在元素相互依赖的情况
     */
    @Test
    public void loadTest005() {
        Exception err = null;
        try {
            new AnnotationConfigApplicationContext(TestLoadConfiguration.LoadBpmnConfigTest005.class);
        } catch (Exception e) {
            err = e;
        }
        Optional<ResourceException> exception = ExceptionUtil.getErrFromCause(err, ResourceException.class);
        Assert.assertTrue(exception.isPresent());

        exception.get().printStackTrace();
        String errorCode = exception.get().getErrorCode();
        Assert.assertEquals(ExceptionEnum.CONFIGURATION_FLOW_ERROR.getExceptionCode(), errorCode);
    }

    /**
     * 【异常】测试：服务节点有两个入度
     */
    @Test
    public void loadTest006() {
        Exception err = null;
        try {
            new AnnotationConfigApplicationContext(TestLoadConfiguration.LoadBpmnConfigTest006.class);
        } catch (Exception e) {
            err = e;
        }

        Optional<ResourceException> exception = ExceptionUtil.getErrFromCause(err, ResourceException.class);
        Assert.assertTrue(exception.isPresent());

        exception.get().printStackTrace();
        String errorCode = exception.get().getErrorCode();
        Assert.assertEquals(ExceptionEnum.CONFIGURATION_FLOW_ERROR.getExceptionCode(), errorCode);
    }

    /**
     * 【异常】测试：排他网关有两个入度
     */
    @Test
    public void loadTest007() {
        Exception err = null;
        try {
            new AnnotationConfigApplicationContext(TestLoadConfiguration.LoadBpmnConfigTest007.class);
        } catch (Exception e) {
            err = e;
        }

        Optional<ResourceException> exception = ExceptionUtil.getErrFromCause(err, ResourceException.class);
        Assert.assertTrue(exception.isPresent());

        exception.get().printStackTrace();
        String errorCode = exception.get().getErrorCode();
        Assert.assertEquals(ExceptionEnum.CONFIGURATION_FLOW_ERROR.getExceptionCode(), errorCode);
    }

}
