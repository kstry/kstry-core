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
package cn.kstry.framework.test.load;

import java.util.Optional;

import cn.kstry.framework.core.util.ExceptionUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.test.load.lct01.Lct01;

/**
 * @author lykan
 */
public class LoadComponentTest {

    /**
     * 【异常】测试：服务节点重复定义
     */
    @Test
    public void loadComponentTest001() {
        Exception err = null;
        try {
            new AnnotationConfigApplicationContext(Lct01.LoadComponentTest001.class);
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        Optional<KstryException> exception = ExceptionUtil.getErrFromCause(err, KstryException.class);
        Assert.assertTrue(exception.isPresent());
        String errorCode = exception.get().getErrorCode();
        Assert.assertEquals(ExceptionEnum.COMPONENT_DUPLICATION_ERROR.getExceptionCode(), errorCode);
    }
}
