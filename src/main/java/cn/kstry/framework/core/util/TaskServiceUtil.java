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

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bus.*;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.container.component.ParamInjectDef;
import cn.kstry.framework.core.container.component.TaskInstructWrapper;
import cn.kstry.framework.core.engine.ParamLifecycle;
import cn.kstry.framework.core.engine.SpringParamLifecycle;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.monitor.ParamTracking;
import cn.kstry.framework.core.role.Role;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static cn.kstry.framework.core.monitor.MonitorTracking.BAD_VALUE;

/**
 * TaskServiceUtil
 *
 * @author lykan
 */
public class TaskServiceUtil {

    /**
     * service name + ability name
     *
     * @param left  service name
     * @param right ability name
     * @return name
     */
    public static String joinName(String left, String right) {
        return innerJoin(left, right, "@");
    }

    public static String joinVersion(String left, long version) {
        return innerJoin(left, String.valueOf(version), "-");
    }

    private static String innerJoin(String left, String right, String sign) {
        AssertUtil.notBlank(left);
        if (StringUtils.isBlank(right)) {
            return left;
        }
        return left + sign + right;
    }

    /**
     * 获取目标方法入参
     */
    public static Object[] getTaskParams(boolean isCustomRole, boolean tracking, ServiceTask serviceTask, StoryBus storyBus, Role role, TaskInstructWrapper taskInstructWrapper,
                                         List<ParamInjectDef> paramInjectDefs, Function<ParamInjectDef, Object> paramInitStrategy, ApplicationContext applicationContext,
                                         IterDataItem<?> iterDataItem) {
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
                params[i] = storyBus.getReq();
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask, () ->
                        ParamTracking.build(iDef.getFieldName(), ScopeTypeEnum.REQUEST.name().toLowerCase(), ScopeTypeEnum.REQUEST, storyBus.getReq(), storyBus.getReq().getClass()))
                );
                continue;
            }

            if (InstructContent.class.isAssignableFrom(iDef.getParamType())) {
                if (taskInstructWrapper == null || StringUtils.isBlank(serviceTask.getTaskInstruct())) {
                    continue;
                }
                InstructContent instructContent = new InstructContent(serviceTask.getTaskInstruct(), serviceTask.getTaskInstructContent());
                params[i] = instructContent;
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                        () -> ParamTracking.build(iDef.getFieldName(), InstructContent.class.getSimpleName(), ScopeTypeEnum.EMPTY, instructContent, InstructContent.class))
                );
                continue;
            }

            if (IterDataItem.class.isAssignableFrom(iDef.getParamType())) {
                if (iterDataItem == null) {
                    continue;
                }
                params[i] = iterDataItem;
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                        () -> ParamTracking.build(iDef.getFieldName(), IterDataItem.class.getSimpleName(), ScopeTypeEnum.EMPTY, iterDataItem, IterDataItem.class))
                );
                continue;
            }

            // 如果目标类是 CustomRole 且方法入参需要 Role 时，直接透传 role
            if (isCustomRole && Role.class.isAssignableFrom(iDef.getParamType())) {
                params[i] = role;
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                        () -> ParamTracking.build(iDef.getFieldName(), Role.class.getSimpleName(), ScopeTypeEnum.EMPTY, null, Role.class))
                );
                continue;
            }

            // 入参是 ScopeDataOperator 时，注入ScopeDataOperator
            if (ScopeDataQuery.class.isAssignableFrom(iDef.getParamType())) {
                params[i] = storyBus.getScopeDataOperator();
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                        () -> ParamTracking.build(iDef.getFieldName(), ScopeDataOperator.class.getSimpleName(), ScopeTypeEnum.EMPTY, null, ScopeDataQuery.class))
                );
                continue;
            }

            // 参数被 @TaskParam、@ReqTaskParam、@VarTaskParam、@StaTaskParam 注解修饰时，从 StoryBus 中直接获取变量并赋值给参数
            if (iDef.getScopeDataEnum() != null && StringUtils.isNotBlank(iDef.getTargetName())) {
                Object r = storyBus.getValue(iDef.getScopeDataEnum(), iDef.getTargetName()).orElse(null);
                if (r == PropertyUtil.GET_PROPERTY_ERROR_SIGN) {
                    trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                            () -> ParamTracking.build(iDef.getFieldName(), iDef.getTargetName(), iDef.getScopeDataEnum(), BAD_VALUE, iDef.getParamType()))
                    );
                    continue;
                }
                if (isPrimitive && r == null) {
                    Object primitiveFinalObj = params[i];
                    trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                            () -> ParamTracking.build(iDef.getFieldName(), iDef.getTargetName(), iDef.getScopeDataEnum(), primitiveFinalObj, iDef.getParamType()))
                    );
                    continue;
                }
                checkParamType(serviceTask, iDef, r);
                params[i] = r;
                trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask,
                        () -> ParamTracking.build(iDef.getFieldName(), iDef.getTargetName(), iDef.getScopeDataEnum(), r, iDef.getParamType()))
                );
                continue;
            }

            // case 1：参数 Bean 需要解析注入
            // case 2：参数需要 Spring 容器实例化
            // case 3：参数实现 ParamLifecycle 接口
            if (CollectionUtils.isNotEmpty(iDef.getFieldInjectDefList()) || iDef.isSpringInitialization() || ParamLifecycle.class.isAssignableFrom(iDef.getParamType())) {
                Object o = paramInitStrategy.apply(iDef);
                if (o instanceof SpringParamLifecycle) {
                    ((SpringParamLifecycle) o).initContext(applicationContext);
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
                                    ParamTracking.build(iDef.getFieldName() + "." + def.getFieldName(), def.getTargetName(), def.getScopeDataEnum(), BAD_VALUE, def.getParamType()))
                            );
                            return;
                        }
                        checkParamType(serviceTask, def, value);
                        boolean setSuccess = PropertyUtil.setProperty(o, def.getFieldName(), value);
                        if (setSuccess) {
                            trackingOptional.ifPresent(mt -> mt.trackingNodeParams(serviceTask, () ->
                                    ParamTracking.build(iDef.getFieldName() + "." + def.getFieldName(), def.getTargetName(), def.getScopeDataEnum(), value, def.getParamType()))
                            );
                        }
                    });
                }
                params[i] = o;
            }
        }
        return params;
    }

    private static void checkParamType(FlowElement flowElement, ParamInjectDef def, Object value) {
        boolean correctType = (value == null) || ElementParserUtil.isAssignable(def.getParamType(), value.getClass());
        AssertUtil.isTrue(correctType, ExceptionEnum.SERVICE_PARAM_ERROR, "The actual type does not match the expected type! nodeName: {}, actual: {}, expected: {}",
                () -> {
                    String actual = (value == null) ? "null" : value.getClass().getName();
                    return Lists.newArrayList(flowElement.getName(), actual, def.getParamType().getName());
                }
        );
    }

    @SuppressWarnings("unchecked")
    public static void fillTaskParams(Object[] params, Map<String, Object> taskParams,
                                      List<ParamInjectDef> paramInjectDefs, Function<ParamInjectDef, Object> paramInitStrategy, ScopeDataOperator scopeDataOperator) {
        if (!GlobalProperties.SERVICE_NODE_DEFINE_PARAMS) {
            return;
        }
        if (params == null || params.length == 0 || MapUtils.isEmpty(taskParams) || CollectionUtils.isEmpty(paramInjectDefs)) {
            return;
        }
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
                continue;
            }
            try {
                if (val instanceof String) {
                    params[i] = parseParamValue((String) val, scopeDataOperator, iDef.getParamType());
                    continue;
                }
                if (val instanceof Map) {
                    if (params[i] == null) {
                        params[i] = paramInitStrategy.apply(iDef);
                    }
                    AssertUtil.notNull(params[i]);
                    Map<String, ?> valMap = (Map<String, ?>) val;
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
                        if (v == null) {
                            PropertyUtil.setProperty(params[i], field.getName(), field.getType().isPrimitive() ? ElementParserUtil.initPrimitive(field.getType()) : null);
                            continue;
                        }
                        Object vObj = parseParamValue(v instanceof String ? (String) v : JSON.toJSONString(v), scopeDataOperator, field.getType());
                        PropertyUtil.setProperty(params[i], field.getName(), vObj);
                    }
                    continue;
                }
            } catch (JSONException e) {
                throw ExceptionUtil.buildException(e, ExceptionEnum.TYPE_TRANSFER_ERROR,
                        GlobalUtil.format("External specified parameter type conversion exception. index: {}, paramName: {}, message: {}", i, fName, e.getMessage()));
            }
            AssertUtil.isTrue(false, ExceptionEnum.SERVICE_PARAM_ERROR, "taskParams does not allow invalid value types to appear. taskParams: {}", taskParams);
        }
    }

    private static Object parseParamValue(String valStr, ScopeDataOperator scopeDataOperator, Class<?> type) {
        if (valStr.startsWith("@") && ElementParserUtil.isValidDataExpression(valStr.substring(1))) {
            return scopeDataOperator.getData(valStr.substring(1)).orElse(type.isPrimitive() ? ElementParserUtil.initPrimitive(type) : null);
        }
        return JSON.parseObject(valStr, type);
    }
}
