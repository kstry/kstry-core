/*
 *  * Copyright (c) 2020-2024, xiongzhongwei (xiongzhongwei@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express   or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 */
package cn.kstry.framework.test.monitor;

import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.monitor.FieldSerializeTracking;
import cn.kstry.framework.core.monitor.ParamTracking;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for Field Serialize Tracking
 *
 * @author xiongzhongwei
 * @date 12/21/2023 9:18 PM
 */
public class FieldSerializeTrackingTest {

    @Test
    public void testValueOfNull() {
        // test：when passValue is null
        ParamTracking paramTracking1 = ParamTracking.build(
                "param", "source", ScopeTypeEnum.REQUEST,
                null, String.class, "converter"
        );

        FieldSerializeTracking fieldSerializeTracking1 = new FieldSerializeTracking();
        String serializedValue1 = fieldSerializeTracking1.valueSerialize(paramTracking1);
        Assert.assertNull(serializedValue1);
    }

    @Test
    public void testMaxLengthError() {
        // test: Can the length be truncated if it is too long
        GlobalProperties.KSTRY_STORY_TRACKING_PARAMS_LENGTH_LIMIT = 50;
        ParamTracking paramTracking2 = ParamTracking.build(
                "param", "source",
                ScopeTypeEnum.REQUEST, "This is a very long string that exceeds the length limit " +
                        "for serialization",
                String.class, "converter"
        );

        FieldSerializeTracking fieldSerializeTracking2 = new FieldSerializeTracking();
        String serializedValue2 = fieldSerializeTracking2.valueSerialize(paramTracking2);
        Assert.assertEquals(
                "This is a very long string that exceeds the length",
                serializedValue2
        );
    }
}
