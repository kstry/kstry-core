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

import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.bpmn.SubProcess;
import cn.kstry.framework.core.bpmn.impl.ServiceTaskImpl;
import cn.kstry.framework.core.bpmn.impl.SubProcessImpl;
import cn.kstry.framework.core.component.bpmn.DiagramTraverseSupport;
import cn.kstry.framework.core.constant.GlobalConstant;
import cn.kstry.framework.core.container.component.MethodWrapper;
import cn.kstry.framework.core.container.component.ParamInjectDef;
import cn.kstry.framework.core.container.task.TaskComponentRegister;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.resource.service.ServiceNodeResource;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ElementParserUtil
 *
 * @author lykan
 */
public class ElementParserUtil {

    /**
     * 基本数据类型默认值
     */
    private static final Map<Class<?>, Object> PRIMITIVE_INIT_MAP = Maps.newHashMap();

    private static final Map<Class<?>, Object> PRIMITIVE_PACK_TYPE = Maps.newHashMap();

    static {
        PRIMITIVE_INIT_MAP.put(long.class, 0L);
        PRIMITIVE_INIT_MAP.put(int.class, 0);
        PRIMITIVE_INIT_MAP.put(short.class, (short) 0);
        PRIMITIVE_INIT_MAP.put(byte.class, (byte) 0);
        PRIMITIVE_INIT_MAP.put(char.class, (char) 0);
        PRIMITIVE_INIT_MAP.put(double.class, 0.0);
        PRIMITIVE_INIT_MAP.put(float.class, 0.0f);
        PRIMITIVE_INIT_MAP.put(boolean.class, false);

        PRIMITIVE_PACK_TYPE.put(long.class, Long.class);
        PRIMITIVE_PACK_TYPE.put(int.class, Integer.class);
        PRIMITIVE_PACK_TYPE.put(short.class, Short.class);
        PRIMITIVE_PACK_TYPE.put(byte.class, Byte.class);
        PRIMITIVE_PACK_TYPE.put(char.class, Character.class);
        PRIMITIVE_PACK_TYPE.put(double.class, Double.class);
        PRIMITIVE_PACK_TYPE.put(float.class, Float.class);
        PRIMITIVE_PACK_TYPE.put(boolean.class, Boolean.class);
    }

    public static Object initPrimitive(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return PRIMITIVE_INIT_MAP.get(clazz);
    }

    public static Optional<Object> newInstance(Class<?> clazz) {
        if (clazz == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(clazz.getDeclaredConstructor().newInstance());
        } catch (Throwable e) {
            throw ExceptionUtil.buildException(e, ExceptionEnum.SERVICE_PARAM_ERROR,
                    GlobalUtil.format(ExceptionEnum.SERVICE_PARAM_ERROR.getDesc() + " class: {}", clazz.getName()));
        }
    }

    public static Optional<MethodWrapper.TaskFieldProperty> getTaskParamAnnotation(Parameter p, String paramName) {
        if (p == null) {
            return Optional.empty();
        }

        TaskParam taskParamAnn = p.getAnnotation(TaskParam.class);
        if (taskParamAnn != null) {
            if (taskParamAnn.value() != null && taskParamAnn.value().length > 0) {
                paramName = Stream.of(taskParamAnn.value()).filter(StringUtils::isNotBlank).collect(Collectors.joining("."));
            }
            return Optional.of(new MethodWrapper.TaskFieldProperty(paramName, taskParamAnn.scopeEnum(), taskParamAnn.converter()));
        }

        ReqTaskParam reqTaskParamAnn = p.getAnnotation(ReqTaskParam.class);
        if (reqTaskParamAnn != null) {
            if (reqTaskParamAnn.value() != null && reqTaskParamAnn.value().length > 0) {
                paramName = Stream.of(reqTaskParamAnn.value()).filter(StringUtils::isNotBlank).collect(Collectors.joining("."));
            }
            MethodWrapper.TaskFieldProperty taskFieldProperty = new MethodWrapper.TaskFieldProperty(paramName, ScopeTypeEnum.REQUEST, reqTaskParamAnn.converter());
            taskFieldProperty.setInjectSelf(reqTaskParamAnn.reqSelf());
            return Optional.of(taskFieldProperty);
        }

        StaTaskParam staTaskParamAnn = p.getAnnotation(StaTaskParam.class);
        if (staTaskParamAnn != null) {
            if (staTaskParamAnn.value() != null && staTaskParamAnn.value().length > 0) {
                paramName = Stream.of(staTaskParamAnn.value()).filter(StringUtils::isNotBlank).collect(Collectors.joining("."));
            }
            return Optional.of(new MethodWrapper.TaskFieldProperty(paramName, ScopeTypeEnum.STABLE, staTaskParamAnn.converter()));
        }

        VarTaskParam varTaskParamAnn = p.getAnnotation(VarTaskParam.class);
        if (varTaskParamAnn != null) {
            if (varTaskParamAnn.value() != null && varTaskParamAnn.value().length > 0) {
                paramName = Stream.of(varTaskParamAnn.value()).filter(StringUtils::isNotBlank).collect(Collectors.joining("."));
            }
            return Optional.of(new MethodWrapper.TaskFieldProperty(paramName, ScopeTypeEnum.VARIABLE, varTaskParamAnn.converter()));
        }
        return Optional.empty();
    }

    public static List<ParamInjectDef> getFieldInjectDefList(Class<?> clazz) {
        List<ParamInjectDef> fieldInjectDefList = Lists.newArrayList();
        if (clazz == null) {
            return fieldInjectDefList;
        }

        Set<Field> fieldSet = Sets.newHashSet();
        Field[] taskFields = FieldUtils.getFieldsWithAnnotation(clazz, TaskField.class);
        if (ArrayUtils.isNotEmpty(taskFields)) {
            Stream.of(taskFields).forEach(field -> {
                if (fieldSet.contains(field)) {
                    return;
                }
                fieldSet.add(field);
                TaskField taskFieldAnn = field.getAnnotation(TaskField.class);
                AssertUtil.notNull(taskFieldAnn);
                String targetName = Optional.of(taskFieldAnn).map(TaskField::value).map(s ->
                                Stream.of(s).filter(StringUtils::isNotBlank).collect(Collectors.joining("."))
                        )
                        .filter(StringUtils::isNotBlank).orElse(field.getName());
                MethodWrapper.TaskFieldProperty taskFieldProperty = new MethodWrapper.TaskFieldProperty(targetName, taskFieldAnn.scopeEnum(), taskFieldAnn.converter());
                ParamInjectDef injectDef = new ParamInjectDef(GlobalConstant.STORY_DATA_SCOPE.contains(
                        taskFieldProperty.getScopeDataEnum()), field.getType(), ProxyUtil.getCollGenericType(field).orElse(null), field.getName(), taskFieldProperty
                );
                fieldInjectDefList.add(injectDef);
            });
        }

        Field[] reqTaskFields = FieldUtils.getFieldsWithAnnotation(clazz, ReqTaskField.class);
        if (ArrayUtils.isNotEmpty(reqTaskFields)) {
            Stream.of(reqTaskFields).forEach(field -> {
                if (fieldSet.contains(field)) {
                    return;
                }
                fieldSet.add(field);
                ReqTaskField taskFieldAnn = field.getAnnotation(ReqTaskField.class);
                AssertUtil.notNull(taskFieldAnn);
                String targetName = Optional.of(taskFieldAnn).map(ReqTaskField::value).map(s ->
                                Stream.of(s).filter(StringUtils::isNotBlank).collect(Collectors.joining("."))
                        )
                        .filter(StringUtils::isNotBlank).orElse(field.getName());
                MethodWrapper.TaskFieldProperty taskFieldProperty = new MethodWrapper.TaskFieldProperty(targetName, ScopeTypeEnum.REQUEST, taskFieldAnn.converter());
                ParamInjectDef injectDef = new ParamInjectDef(true, field.getType(), ProxyUtil.getCollGenericType(field).orElse(null), field.getName(), taskFieldProperty);
                fieldInjectDefList.add(injectDef);
            });
        }

        Field[] staTaskFields = FieldUtils.getFieldsWithAnnotation(clazz, StaTaskField.class);
        if (ArrayUtils.isNotEmpty(staTaskFields)) {
            Stream.of(staTaskFields).forEach(field -> {
                if (fieldSet.contains(field)) {
                    return;
                }
                fieldSet.add(field);
                StaTaskField taskFieldAnn = field.getAnnotation(StaTaskField.class);
                AssertUtil.notNull(taskFieldAnn);
                String targetName = Optional.of(taskFieldAnn).map(StaTaskField::value).map(s ->
                                Stream.of(s).filter(StringUtils::isNotBlank).collect(Collectors.joining("."))
                        )
                        .filter(StringUtils::isNotBlank).orElse(field.getName());
                MethodWrapper.TaskFieldProperty taskFieldProperty = new MethodWrapper.TaskFieldProperty(targetName, ScopeTypeEnum.STABLE, taskFieldAnn.converter());
                ParamInjectDef injectDef = new ParamInjectDef(true, field.getType(), ProxyUtil.getCollGenericType(field).orElse(null), field.getName(), taskFieldProperty);
                fieldInjectDefList.add(injectDef);
            });
        }

        Field[] varTaskFields = FieldUtils.getFieldsWithAnnotation(clazz, VarTaskField.class);
        if (ArrayUtils.isNotEmpty(varTaskFields)) {
            Stream.of(varTaskFields).forEach(field -> {
                if (fieldSet.contains(field)) {
                    return;
                }
                fieldSet.add(field);
                VarTaskField taskFieldAnn = field.getAnnotation(VarTaskField.class);
                AssertUtil.notNull(taskFieldAnn);
                String targetName = Optional.of(taskFieldAnn).map(VarTaskField::value).map(s ->
                                Stream.of(s).filter(StringUtils::isNotBlank).collect(Collectors.joining("."))
                        )
                        .filter(StringUtils::isNotBlank).orElse(field.getName());
                MethodWrapper.TaskFieldProperty taskFieldProperty = new MethodWrapper.TaskFieldProperty(targetName, ScopeTypeEnum.VARIABLE, taskFieldAnn.converter());
                ParamInjectDef injectDef = new ParamInjectDef(true, field.getType(), ProxyUtil.getCollGenericType(field).orElse(null), field.getName(), taskFieldProperty);
                fieldInjectDefList.add(injectDef);
            });
        }
        return fieldInjectDefList;
    }

    public static boolean isAssignable(Class<?> left, Class<?> right) {
        if (left == null || right == null) {
            return false;
        }
        if (left == right) {
            return true;
        }
        if (Objects.equals(GlobalConstant.VOID, left.getName()) || Objects.equals(GlobalConstant.VOID, right.getName())) {
            return false;
        }
        if (left.isPrimitive()) {
            left = (Class<?>) PRIMITIVE_PACK_TYPE.get(left);
        }
        if (right.isPrimitive()) {
            right = (Class<?>) PRIMITIVE_PACK_TYPE.get(right);
        }
        return left.isAssignableFrom(right);
    }

    public static boolean isSpringInitialization(Class<?> clazz) {
        if (clazz == null || clazz.isPrimitive()) {
            return false;
        }
        return AnnotationUtils.findAnnotation(clazz, SpringInitialization.class) != null;
    }

    public static boolean isTaskComponentClass(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        TaskComponent taskComponentAnnotation = AnnotationUtils.findAnnotation(clazz, TaskComponent.class);
        return taskComponentAnnotation != null || TaskComponentRegister.class.isAssignableFrom(clazz);
    }

    public static boolean isValidDataExpression(String expression) {
        if (StringUtils.isBlank(expression)) {
            return false;
        }
        if (Objects.equals(ScopeTypeEnum.RESULT.getKey(), expression)) {
            return true;
        }
        if (Pattern.matches(GlobalConstant.VALID_DATA_EXPRESSION_PATTERN, expression)) {
            return true;
        }
        String[] split = expression.split("\\.", 3);
        if (split.length < 3) {
            return false;
        }
        if (Objects.equals(split[0], "$")) {
            return GlobalConstant.STORY_DATA_SCOPE.contains(ScopeTypeEnum.of(split[1]).orElse(null));
        }
        return Objects.equals(split[1], "$") && GlobalConstant.STORY_DATA_SCOPE.contains(ScopeTypeEnum.of(split[0]).orElse(null));
    }

    public static void fillSubProcess(Map<String, SubProcess> allSubProcess, StartEvent startEvent) {
        new DiagramTraverseSupport<Object>() {

            @Override
            public void doSubProcess(SubProcessImpl subProcess) {
                SubProcessImpl sp = (SubProcessImpl) allSubProcess.get(subProcess.getId());
                AssertUtil.notNull(sp, ExceptionEnum.CONFIGURATION_SUBPROCESS_ERROR, "Element id cannot match to SubProcess instance! calledElement identity: {}", subProcess.identity());
                sp.cloneSubProcess(allSubProcess, subProcess);
            }
        }.traverse(startEvent);
    }

    public static void tryFillTaskName(ServiceTaskImpl serviceTask, List<ServiceNodeResource> serviceNodeResources) {
        if (StringUtils.isNotBlank(serviceTask.getName()) || CollectionUtils.isEmpty(serviceNodeResources)) {
            return;
        }
        serviceNodeResources.stream().filter(r ->
                StringUtils.isBlank(r.getAbilityName()) && StringUtils.isNotBlank(r.getDescription())).findFirst().ifPresent(r -> serviceTask.setName(r.getDescription()));
    }
}
