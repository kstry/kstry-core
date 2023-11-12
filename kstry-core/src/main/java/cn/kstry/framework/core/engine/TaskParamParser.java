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
package cn.kstry.framework.core.engine;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.impl.TaskParamWrapper;
import cn.kstry.framework.core.bus.*;
import cn.kstry.framework.core.component.conversion.TypeConverterProcessor;
import cn.kstry.framework.core.component.validator.RequestValidator;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.container.component.MethodWrapper;
import cn.kstry.framework.core.container.component.ParamInjectDef;
import cn.kstry.framework.core.container.component.TaskInstructWrapper;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.monitor.ParamTracking;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.*;
import com.alibaba.fastjson.JSONException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static cn.kstry.framework.core.monitor.MonitorTracking.BAD_VALUE;

public class TaskParamParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskParamParser.class);

    /**
     * StoryEngine 组成模块
     */
    private final StoryEngineModule engineModule;

    public TaskParamParser(StoryEngineModule engineModule) {
        this.engineModule = engineModule;
    }

    public Object[] parseParams(boolean tracking, IterDataItem<?> iterDataItem, ServiceTask serviceTask,
                                StoryBus storyBus, Role role, MethodWrapper methodWrapper, List<ParamInjectDef> paramInjectDefs) {
        TaskInstructWrapper taskInstructWrapper = methodWrapper.getTaskInstructWrapper().orElse(null);
        Object[] params = getTaskParams(methodWrapper.isCustomRole(), tracking, serviceTask, storyBus, role, taskInstructWrapper, paramInjectDefs, iterDataItem);
        fillTaskParams(tracking, storyBus, serviceTask, params, serviceTask.getTaskParamWrapper(), paramInjectDefs, storyBus.getScopeDataOperator());
        if (ArrayUtils.isEmpty(params)) {
            return params;
        }

        for (Object param : params) {
            if (param instanceof ParamLifecycle) {
                ((ParamLifecycle) param).after(storyBus.getScopeDataOperator());
            }
            if (GlobalUtil.supportValidate()) {
                RequestValidator.validate(param);
            }
        }
        return params;
    }

    /**
     * 获取目标方法入参
     */
    private Object[] getTaskParams(boolean isCustomRole, boolean tracking, ServiceTask serviceTask, StoryBus storyBus, Role role,
                                   TaskInstructWrapper taskInstructWrapper, List<ParamInjectDef> paramInjectDefs, IterDataItem<?> iterDataItem) {
        AssertUtil.notNull(serviceTask);
        Optional<MonitorTracking> trackingOptional = Optional.of(storyBus.getMonitorTracking()).filter(t -> tracking);
        Object[] params = new Object[paramInjectDefs.size()];
        for (int i = 0; i < paramInjectDefs.size(); i++) {

            // 默认 null
            params[i] = null;

            // 没有参数定义时，取默认值
            ParamInjectDef iDef = paramInjectDefs.get(i);
            if (iDef == null) {
                continue;
            }

            // 如果是基本数据类型，进行基本数据类型初始化
            boolean isPrimitive = iDef.getParamType().isPrimitive();
            if (isPrimitive) {
                params[i] = ElementParserUtil.initPrimitive(iDef.getParamType());
            }
            if (iDef.notNeedInject()) {
                continue;
            }

            // 如果拿入参的 request 参数，直接赋值
            if (iDef.getScopeDataEnum() == ScopeTypeEnum.REQUEST && iDef.isInjectSelf()) {
                Object actualReq = storyBus.isSetReqScope() ? storyBus.getReq() : params[i];
                params[i] = actualReq;
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask, () -> ParamTracking.build(iDef.getFieldName(),
                        ScopeTypeEnum.REQUEST.name().toLowerCase(), ScopeTypeEnum.REQUEST, actualReq, Optional.ofNullable(actualReq).map(Object::getClass).orElse(null), null)
                ));
                continue;
            }

            if (InstructContent.class.isAssignableFrom(iDef.getParamType())) {
                if (taskInstructWrapper == null || StringUtils.isBlank(serviceTask.getTaskInstruct())) {
                    continue;
                }
                InstructContent instructContent = new InstructContent(serviceTask.getTaskInstruct(), serviceTask.getTaskInstructContent());
                params[i] = instructContent;
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                        () -> ParamTracking.build(iDef.getFieldName(), InstructContent.class.getSimpleName(), ScopeTypeEnum.EMPTY, instructContent, InstructContent.class, null))
                );
                continue;
            }

            if (IterDataItem.class.isAssignableFrom(iDef.getParamType())) {
                if (iterDataItem == null) {
                    continue;
                }
                params[i] = iterDataItem;
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                        () -> ParamTracking.build(iDef.getFieldName(), IterDataItem.class.getSimpleName(), ScopeTypeEnum.EMPTY, iterDataItem, IterDataItem.class, null))
                );
                continue;
            }

            // 如果目标类是 CustomRole 且方法入参需要 Role 时，直接透传 role
            if (isCustomRole && Role.class.isAssignableFrom(iDef.getParamType())) {
                params[i] = role;
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                        () -> ParamTracking.build(iDef.getFieldName(), Role.class.getSimpleName(), ScopeTypeEnum.EMPTY, null, Role.class, null))
                );
                continue;
            }

            // 入参是 ScopeDataOperator 时，注入ScopeDataOperator
            if (ScopeDataQuery.class.isAssignableFrom(iDef.getParamType())) {
                params[i] = storyBus.getScopeDataOperator();
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                        () -> ParamTracking.build(iDef.getFieldName(), ScopeDataOperator.class.getSimpleName(), ScopeTypeEnum.EMPTY, null, ScopeDataQuery.class, null))
                );
                continue;
            }

            // 参数被 @TaskParam、@ReqTaskParam、@VarTaskParam、@StaTaskParam 注解修饰时，从 StoryBus 中直接获取变量并赋值给参数
            if (iDef.getScopeDataEnum() != null && StringUtils.isNotBlank(iDef.getTargetName())) {
                Object r = storyBus.getValue(iDef.getScopeDataEnum(), iDef.getTargetName()).orElse(null);
                if (r == PropertyUtil.GET_PROPERTY_ERROR_SIGN) {
                    trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                            () -> ParamTracking.build(iDef.getFieldName(), iDef.getTargetName(), iDef.getScopeDataEnum(), BAD_VALUE, iDef.getParamType(), null))
                    );
                    continue;
                }
                if (isPrimitive && r == null) {
                    Object primitiveFinalObj = params[i];
                    trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                            () -> ParamTracking.build(iDef.getFieldName(), iDef.getTargetName(), iDef.getScopeDataEnum(), primitiveFinalObj, iDef.getParamType(), null))
                    );
                    continue;
                }
                Pair<String, ?> convertPair = engineModule.getTypeConverterProcessor().convert(iDef.getConverter(), r, iDef.getParamType(), iDef.getCollGenericType().orElse(null));
                r = convertPair.getValue();
                checkParamType(serviceTask, iDef, r);
                params[i] = r;
                Object rFinal = r;
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                        () -> ParamTracking.build(iDef.getFieldName(), iDef.getTargetName(), iDef.getScopeDataEnum(), rFinal, iDef.getParamType(), convertPair.getKey()))
                );
                continue;
            }

            // case 1：参数 Bean 需要解析注入
            // case 2：参数需要 Spring 容器实例化
            // case 3：参数实现 ParamLifecycle 接口
            if (CollectionUtils.isNotEmpty(iDef.getFieldInjectDefList()) || iDef.isSpringInitialization() || ParamLifecycle.class.isAssignableFrom(iDef.getParamType())) {
                Object o = engineModule.getParamInitStrategy().apply(iDef);
                if (o instanceof SpringParamLifecycle) {
                    ((SpringParamLifecycle) o).initContext(engineModule.getApplicationContext());
                }
                if (o instanceof ParamLifecycle) {
                    ((ParamLifecycle) o).before(storyBus.getScopeDataOperator());
                }

                List<ParamInjectDef> fieldInjectDefList = iDef.getFieldInjectDefList();
                if (CollectionUtils.isNotEmpty(fieldInjectDefList)) {
                    fieldInjectDefList.forEach(def -> {
                        if (def.notNeedInject()) {
                            return;
                        }
                        Object value = storyBus.getValue(def.getScopeDataEnum(), def.getTargetName()).orElse(null);
                        if (value == PropertyUtil.GET_PROPERTY_ERROR_SIGN) {
                            trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask, () ->
                                    ParamTracking.build(iDef.getFieldName() + "." + def.getFieldName(), def.getTargetName(), def.getScopeDataEnum(), BAD_VALUE, def.getParamType(), null))
                            );
                            return;
                        }
                        Pair<String, ?> convertPair = engineModule.getTypeConverterProcessor().convert(def.getConverter(), value, def.getParamType(), def.getCollGenericType().orElse(null));
                        value = convertPair.getValue();
                        checkParamType(serviceTask, def, value);
                        boolean setSuccess = PropertyUtil.setProperty(o, def.getFieldName(), value);
                        if (setSuccess) {
                            Object valueFinal = value;
                            trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask, () ->
                                    ParamTracking.build(iDef.getFieldName() + "." + def.getFieldName(), def.getTargetName(), def.getScopeDataEnum(), valueFinal, def.getParamType(), convertPair.getKey()))
                            );
                        }
                    });
                }
                params[i] = o;
            }
        }
        return params;
    }

    private void checkParamType(FlowElement flowElement, ParamInjectDef def, Object value) {
        boolean correctType = (value == null) || ElementParserUtil.isAssignable(def.getParamType(), value.getClass());
        AssertUtil.isTrue(correctType, ExceptionEnum.SERVICE_PARAM_ERROR, "The actual type does not match the expected type! nodeName: {}, actual: {}, expected: {}",
                () -> {
                    String actual = (value == null) ? "null" : value.getClass().getName();
                    return Lists.newArrayList(flowElement.getName(), actual, def.getParamType().getName());
                }
        );
    }

    @SuppressWarnings("unchecked")
    private void fillTaskParams(boolean tracking, StoryBus storyBus, ServiceTask serviceTask,
                                Object[] params, TaskParamWrapper taskParamWrapper, List<ParamInjectDef> paramInjectDefs, ScopeDataOperator scopeDataOperator) {
        if (!GlobalProperties.SERVICE_NODE_DEFINE_PARAMS) {
            return;
        }
        if (params == null || params.length == 0 || taskParamWrapper == null || CollectionUtils.isEmpty(paramInjectDefs)) {
            return;
        }
        Map<String, Object> taskParams = taskParamWrapper.getParams();
        if (MapUtils.isEmpty(taskParams)) {
            return;
        }
        Map<String, String> valueMapping = taskParamWrapper.getValueMapping();
        if (MapUtils.isNotEmpty(valueMapping)) {
            valueMapping.forEach((k, v) -> PropertyUtil.setProperty(taskParams, k, scopeDataOperator.getData(v).orElse(null)));
        }
        Map<String, String> converterMapping = taskParamWrapper.getConverterMapping();
        if (MapUtils.isNotEmpty(converterMapping)) {
            converterMapping.forEach((k, v) -> {
                Object val = PropertyUtil.getProperty(taskParams, k).filter(r -> r != PropertyUtil.GET_PROPERTY_ERROR_SIGN).orElse(null);
                if (val == null) {
                    return;
                }
                Pair<String, Object> convert = engineModule.getTypeConverterProcessor().convert(v, val);
                PropertyUtil.setProperty(taskParams, k, convert.getValue());
            });
        }
        LOGGER.debug("TaskParamParser fillTaskParams. valueMapping: {}, converterMapping: {}", valueMapping, converterMapping);
        TypeConverterProcessor typeConverterProcessor = engineModule.getTypeConverterProcessor();
        Optional<MonitorTracking> trackingOptional = Optional.of(storyBus.getMonitorTracking()).filter(t -> tracking);
        for (int i = 0; i < paramInjectDefs.size(); i++) {
            ParamInjectDef iDef = paramInjectDefs.get(i);
            if (iDef == null) {
                continue;
            }

            String fName = StringUtils.isBlank(iDef.getTargetName()) ? iDef.getFieldName() : iDef.getTargetName();
            AssertUtil.notBlank(fName);
            if (!taskParams.containsKey(fName)) {
                continue;
            }
            Object val = taskParams.get(fName);
            if (val == null) {
                params[i] = iDef.getParamType().isPrimitive() ? ElementParserUtil.initPrimitive(iDef.getParamType()) : null;
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask, () ->
                        ParamTracking.build(iDef.getFieldName(), fName, ScopeTypeEnum.CONFIG, null, iDef.getParamType(), null))
                );
                continue;
            }
            try {
                if (Map.class.isAssignableFrom(iDef.getParamType()) && iDef.getParamType().isAssignableFrom(val.getClass())) {
                    Pair<String, ?> convertPair = typeConverterProcessor.convert(null, val, iDef.getParamType(), iDef.getCollGenericType().orElse(null));
                    params[i] = convertPair.getValue();
                    trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask, () ->
                            ParamTracking.build(iDef.getFieldName(), fName, ScopeTypeEnum.CONFIG, convertPair.getValue(), iDef.getParamType(), convertPair.getKey()))
                    );
                    continue;
                }

                if (!(val instanceof Map)) {
                    Pair<String, Object> paramPair = parseParamValue(val, iDef.getParamType(), iDef.getCollGenericType().orElse(null));
                    params[i] = paramPair.getValue();
                    trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask, () ->
                            ParamTracking.build(iDef.getFieldName(), fName, ScopeTypeEnum.CONFIG, paramPair.getValue(), iDef.getParamType(), paramPair.getKey()))
                    );
                    continue;
                }

                if (params[i] == null) {
                    params[i] = engineModule.getParamInitStrategy().apply(iDef);
                }
                AssertUtil.notNull(params[i]);
                Map<String, Object> valMap = (Map<String, Object>) val;
                if (MapUtils.isEmpty(valMap)) {
                    continue;
                }

                Map<String, ParamInjectDef> defMap = Maps.newHashMap();
                if (CollectionUtils.isNotEmpty(iDef.getFieldInjectDefList())) {
                    iDef.getFieldInjectDefList().forEach(def -> defMap.put(def.getFieldName(), def));
                }

                for (Field field : params[i].getClass().getDeclaredFields()) {
                    String targetName = Optional.ofNullable(defMap.get(field.getName()))
                            .filter(def -> field.getType() == def.getParamType()).map(ParamInjectDef::getTargetName).orElse(field.getName());
                    if (!valMap.containsKey(targetName)) {
                        continue;
                    }
                    Object v = valMap.get(targetName);
                    Pair<String, Object> fieldPair = parseParamValue(v, field.getType(), ProxyUtil.getCollGenericType(field).orElse(null));
                    PropertyUtil.setProperty(params[i], field.getName(), fieldPair.getValue());
                    trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask, () -> ParamTracking.build(iDef.getFieldName() + "." + field.getName(),
                            fName + "." + targetName, ScopeTypeEnum.CONFIG, fieldPair.getValue(), field.getType(), fieldPair.getKey()))
                    );
                }
            } catch (JSONException e) {
                throw ExceptionUtil.buildException(e, ExceptionEnum.TYPE_TRANSFER_ERROR,
                        GlobalUtil.format("External specified parameter type conversion exception. index: {}, paramName: {}, message: {}", i, fName, e.getMessage()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Pair<String, Object> parseParamValue(Object val, Class<?> type, Class<?> collGenericType) {
        if (val == null) {
            return ImmutablePair.of(null, type.isPrimitive() ? ElementParserUtil.initPrimitive(type) : null);
        }
        Pair<String, Object> convertPair = (Pair<String, Object>) engineModule.getTypeConverterProcessor().convert(null, val, type, collGenericType);
        return ImmutablePair.of(convertPair.getKey(), convertPair.getValue() == null ? (type.isPrimitive() ? ElementParserUtil.initPrimitive(type) : null) : convertPair.getValue());
    }
}
