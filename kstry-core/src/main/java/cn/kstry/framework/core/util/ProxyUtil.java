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

import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.container.component.MethodWrapper;
import cn.kstry.framework.core.container.task.TaskComponentRegister;
import cn.kstry.framework.core.exception.BusinessException;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.exception.ResourceException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


/**
 * @author lykan
 */
public class ProxyUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyUtil.class);

    private static final Cache<String, Field> classFieldCache = CacheBuilder.newBuilder()
            .concurrencyLevel(8).initialCapacity(1024).maximumSize(20_000).expireAfterAccess(1, TimeUnit.HOURS)
            .removalListener(notification -> LOGGER.info("Class field cache lose efficacy. key: {}, value: {}, cause: {}",
                    notification.getKey(), notification.getValue(), notification.getCause())).build();

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

    public static Object invokeMethod(MethodWrapper methodWrapper, ServiceTask serviceTask, Object target) {
        return invokeMethod(methodWrapper, serviceTask, target, () -> new Object[0]);
    }

    public static Object invokeMethod(MethodWrapper methodWrapper, ServiceTask serviceTask, Object target, Supplier<Object[]> paramsSupplier) {
        try {
            Object[] params = paramsSupplier.get();
            return ReflectionUtils.invokeMethod(methodWrapper.getMethod(), target, params);
        } catch (Throwable e) {
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
        }
    }

    /**
     * left: componentName
     * right: serviceName
     */
    public static Pair<String, String> getComponentServiceFromLambda(Serializable serializable) {
        AssertUtil.notNull(serializable);
        SerializedLambda serializedLambda = getSerializedLambda(serializable);
        String taskComponentName = null;
        String taskServiceName = serializedLambda.getImplMethodName();
        try {
            Class<?> componentClass = Class.forName(serializedLambda.getImplClass().replace("/", "."));
            if (!TaskComponentRegister.class.isAssignableFrom(componentClass)) {
                TaskComponent taskComponent = AnnotationUtils.findAnnotation(componentClass, TaskComponent.class);
                if (taskComponent != null) {
                    taskComponentName = StringUtils.isBlank(taskComponent.name()) ? StringUtils.uncapitalize(componentClass.getSimpleName()) : taskComponent.name();
                }
                if (StringUtils.isBlank(taskComponentName)) {
                    taskComponentName = StringUtils.uncapitalize(componentClass.getSimpleName());
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("[{}] Lambda expression configuration process component class parsing failure! taskServiceName: {}",
                    ExceptionEnum.LAMBDA_PROCESS_PARSE_FAILURE.getExceptionCode(), taskServiceName, e);
        }
        return ImmutablePair.of(taskComponentName, taskServiceName);
    }

    public static SerializedLambda getSerializedLambda(Serializable serializable) {
        try {
            Method write = serializable.getClass().getDeclaredMethod("writeReplace");
            write.setAccessible(true);
            return (SerializedLambda) write.invoke(serializable);
        } catch (Throwable e) {
            throw new ResourceException(ExceptionEnum.LAMBDA_PROCESS_PARSE_FAILURE, e);
        }
    }

    public static Optional<Class<?>> getCollGenericType(Object obj) {
        if (obj == null) {
            return Optional.empty();
        }
        ResolvableType resolvableType = null;
        if (obj instanceof Parameter) {
            Parameter parameter = (Parameter) obj;
            if (notCollGeneric(parameter.getType())) {
                return Optional.empty();
            }
            Type type = ((Parameter) obj).getParameterizedType();
            if (type == null) {
                return Optional.empty();
            }
            resolvableType = ResolvableType.forType(type);
        } else if (obj instanceof Field) {
            Field field = (Field) obj;
            if (notCollGeneric(field.getType())) {
                return Optional.empty();
            }
            resolvableType = ResolvableType.forField(field);
        }
        if (resolvableType == null) {
            return Optional.empty();
        }
        Class<?>[] generics = resolvableType.resolveGenerics();
        if (ArrayUtils.isEmpty(generics)) {
            return Optional.empty();
        }
        return Optional.of(generics[0]);
    }

    public static Optional<Field> getField(Class<?> clazz, String fieldName) {
        if (clazz == null || StringUtils.isBlank(fieldName)) {
            return Optional.empty();
        }
        String cacheKey = clazz.getName() + "@" + clazz.hashCode() + "@" + fieldName;
        Field fieldCache = classFieldCache.getIfPresent(cacheKey);
        if (fieldCache != null) {
            return Optional.of(fieldCache);
        }
        Field field = FieldUtils.getField(clazz, fieldName, true);
        if (field != null) {
            classFieldCache.put(cacheKey, field);
        }
        return Optional.ofNullable(field);
    }

    public static Optional<Object> getFieldValue(Object obj, String fieldName) {
        if (obj == null || StringUtils.isBlank(fieldName)) {
            return Optional.empty();
        }
        Optional<Field> fieldOptional = getField(obj.getClass(), fieldName);
        if (!fieldOptional.isPresent()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(FieldUtils.readField(fieldOptional.get(), obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean notCollGeneric(Class<?> clazz) {
        if (clazz == null) {
            return true;
        }
        return !Set.class.isAssignableFrom(clazz) && !List.class.isAssignableFrom(clazz);
    }
}
