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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.constant.ConfigPropertyNameConstant;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.exception.ExceptionEnum;
import com.alibaba.fastjson.JSON;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Optional;

/**
 * PropertyUtil
 *
 * @author lykan
 */
public class PropertyUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtil.class);

    /**
     * 获取解析属性值失败，返回该标识
     */
    public static final Object GET_PROPERTY_ERROR_SIGN = new Object();

    public static Optional<Object> getProperty(Object bean, String propertyName) {
        if (bean == null || StringUtils.isBlank(propertyName)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(PropertyUtils.getProperty(bean, propertyName));
        } catch (NoSuchMethodException e) {
            LOGGER.warn("[{}] Error accessing a non-existent variable! propertyName:{}, class:{}",
                    ExceptionEnum.FAILED_GET_PROPERTY.getExceptionCode(), propertyName, bean.getClass());
            return Optional.of(GET_PROPERTY_ERROR_SIGN);
        } catch (Exception e) {
            LOGGER.warn("[{}] BeanUtils Failed to get bean property! propertyName:{}", ExceptionEnum.FAILED_GET_PROPERTY.getExceptionCode(), propertyName, e);
            return Optional.of(GET_PROPERTY_ERROR_SIGN);
        }
    }

    public static void setProperty(Object target, String propertyName, Object value) {
        if (target == null || StringUtils.isBlank(propertyName)) {
            return;
        }

        try {
            PropertyUtils.setProperty(target, propertyName, value);
        } catch (NoSuchMethodException e) {
            LOGGER.warn("[{}] Unknown property to set bean property! target:{}, propertyName:{}, value:{}",
                    ExceptionEnum.FAILED_SET_PROPERTY.getExceptionCode(), JSON.toJSONString(target), propertyName, value);
        } catch (Exception e) {
            LOGGER.warn("[{}] BeanUtils Failed to set bean property! target:{}, propertyName:{}, value:{}",
                    ExceptionEnum.FAILED_SET_PROPERTY.getExceptionCode(), JSON.toJSONString(target), propertyName, value, e);
        }
    }

    public static void initGlobalProperties(ConfigurableEnvironment environment) {
        AssertUtil.notNull(environment);
        GlobalProperties.ASYNC_TASK_DEFAULT_TIMEOUT = NumberUtils.toInt(
                environment.getProperty(ConfigPropertyNameConstant.KSTRY_STORY_TIMEOUT), GlobalProperties.ASYNC_TASK_DEFAULT_TIMEOUT
        );
        GlobalProperties.THREAD_POOL_CORE_SIZE = NumberUtils.toInt(
                environment.getProperty(ConfigPropertyNameConstant.KSTRY_THREAD_POOL_CORE_SIZE), GlobalProperties.THREAD_POOL_CORE_SIZE
        );
        GlobalProperties.THREAD_POOL_MAX_SIZE = NumberUtils.toInt(
                environment.getProperty(ConfigPropertyNameConstant.KSTRY_THREAD_POOL_MAX_SIZE), GlobalProperties.THREAD_POOL_MAX_SIZE
        );
        GlobalProperties.KSTRY_THREAD_POOL_QUEUE_SIZE = NumberUtils.toInt(
                environment.getProperty(ConfigPropertyNameConstant.KSTRY_THREAD_POOL_QUEUE_SIZE), GlobalProperties.KSTRY_THREAD_POOL_QUEUE_SIZE
        );
        GlobalProperties.ENGINE_SHUTDOWN_SLEEP_SECONDS = NumberUtils.toLong(
                environment.getProperty(ConfigPropertyNameConstant.KSTRY_THREAD_SHUTDOWN_AWAIT), GlobalProperties.ENGINE_SHUTDOWN_SLEEP_SECONDS
        );
        GlobalProperties.THREAD_POOL_KEEP_ALIVE_TIME = NumberUtils.toLong(
                environment.getProperty(ConfigPropertyNameConstant.KSTRY_THREAD_POOL_KEEP_ALIVE_TIME), GlobalProperties.THREAD_POOL_KEEP_ALIVE_TIME
        );
        GlobalProperties.ENGINE_SHUTDOWN_NOW_SLEEP_SECONDS = NumberUtils.toLong(
                environment.getProperty(ConfigPropertyNameConstant.KSTRY_THREAD_SHUTDOWN_NOW_AWAIT), GlobalProperties.ENGINE_SHUTDOWN_NOW_SLEEP_SECONDS
        );
        GlobalProperties.START_EVENT_ID_PREFIX = environment.getProperty(ConfigPropertyNameConstant.KSTRY_STORY_PREFIX, GlobalProperties.START_EVENT_ID_PREFIX);
        GlobalProperties.STORY_SUCCESS_CODE = environment.getProperty(ConfigPropertyNameConstant.KSTRY_STORY_SUCCESS_CODE, GlobalProperties.STORY_SUCCESS_CODE);
        GlobalProperties.STORY_MONITOR_TRACKING_TYPE =
                environment.getProperty(ConfigPropertyNameConstant.KSTRY_STORY_TRACKING_TYPE, GlobalProperties.STORY_MONITOR_TRACKING_TYPE);

        String kstryStoryTrackingLog = environment.getProperty(ConfigPropertyNameConstant.KSTRY_STORY_TRACKING_LOG);
        if (StringUtils.isNotBlank(kstryStoryTrackingLog)) {
            GlobalProperties.KSTRY_STORY_TRACKING_LOG = BooleanUtils.toBoolean(kstryStoryTrackingLog);
        }
        GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME =
                environment.getProperty(ConfigPropertyNameConstant.KSTRY_STORY_REQUEST_ID_NAME, GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME);
    }
}
