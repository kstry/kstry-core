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
package cn.kstry.framework.core.util;

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.container.component.MethodWrapper;
import cn.kstry.framework.core.exception.BusinessException;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.kv.KvScope;
import cn.kstry.framework.core.kv.KvThreadLocal;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

import java.util.function.Supplier;


/**
 * @author lykan
 */
public class ProxyUtil {

    public static boolean isProxyObject(Object o) {
        if (o == null) {
            return false;
        }
        return AopUtils.isAopProxy(o) || AopUtils.isCglibProxy(o) || AopUtils.isJdkDynamicProxy(o);
    }

    public static Class<?> noneProxyClass(Object candidate) {
        if (candidate == null) {
            return null;
        }
        if (!ProxyUtil.isProxyObject(candidate)) {
            return candidate.getClass();
        }
        return AopUtils.getTargetClass(candidate);
    }

    public static Object invokeMethod(StoryBus storyBus, MethodWrapper methodWrapper, ServiceTask serviceTask, Object target) {
        return invokeMethod(storyBus, methodWrapper, serviceTask, target, () -> new Object[0]);
    }

    public static Object invokeMethod(StoryBus storyBus, MethodWrapper methodWrapper, ServiceTask serviceTask, Object target, Supplier<Object[]> paramsSupplier) {
        try {
            KvScope newKvScope = new KvScope(methodWrapper.getKvScope());
            newKvScope.setBusinessId(storyBus.getBusinessId());
            KvThreadLocal.setKvScope(newKvScope);
            Object[] params = paramsSupplier.get();
            return ReflectionUtils.invokeMethod(methodWrapper.getMethod(), target, params);
        } catch (Exception e) {
            if ((e instanceof KstryException) && !(e instanceof BusinessException)) {
                throw (KstryException) e;
            }

            BusinessException businessException;
            if (e instanceof BusinessException) {
                businessException = GlobalUtil.transferNotEmpty(e, BusinessException.class);
            } else {
                businessException = new BusinessException(ExceptionEnum.BUSINESS_INVOKE_ERROR.getExceptionCode(), e.getMessage(), e);
            }
            businessException.setTaskIdentity(TaskServiceUtil.joinName(serviceTask.getTaskComponent(), serviceTask.getTaskService()));
            businessException.setMethodName(methodWrapper.getMethod().getName());
            throw businessException;
        } finally {
            KvThreadLocal.clear();
        }
    }
}
