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
package cn.kstry.framework.core.monitor;

import cn.kstry.framework.core.constant.GlobalProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字段序列化器
 */
public class FieldSerializeTracking implements SerializeTracking {

    private static final Logger LOGGER = LoggerFactory.getLogger(FieldSerializeTracking.class);

    @Override
    public String valueSerialize(FieldTracking fieldTracking) {
        Object passValue = fieldTracking.getPassValue();
        if (passValue == null) {
            return null;
        }

        String v;
        if (passValue instanceof String) {
            v = (String) passValue;
        } else {
            v = JSON.toJSONString(passValue, SerializerFeature.DisableCircularReferenceDetect);
        }
        if (GlobalProperties.KSTRY_STORY_TRACKING_PARAMS_LENGTH_LIMIT == -1 || (v.length() <= GlobalProperties.KSTRY_STORY_TRACKING_PARAMS_LENGTH_LIMIT)) {
            return v;
        } else {
            try {
                return v.substring(0, GlobalProperties.KSTRY_STORY_TRACKING_PARAMS_LENGTH_LIMIT);
            } catch (Exception e) {
                LOGGER.error("build ParamTracking error! tracking: {}", fieldTracking, e);
            }
        }
        return null;
    }
}
