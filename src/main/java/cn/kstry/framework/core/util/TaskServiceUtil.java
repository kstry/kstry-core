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

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bus.BasicStoryBus;
import cn.kstry.framework.core.component.validator.RequestValidator;
import cn.kstry.framework.core.constant.GlobalConstant;
import cn.kstry.framework.core.container.MethodWrapper;
import cn.kstry.framework.core.engine.ParamLifecycle;
import cn.kstry.framework.core.enums.IdentityTypeEnum;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.monitor.ParamTracking;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.task.TaskServiceWrapper;
import cn.kstry.framework.core.task.impl.AbilityTaskServiceWrapper;
import cn.kstry.framework.core.task.impl.RootTaskServiceWrapper;
import cn.kstry.framework.core.task.impl.TaskComponentRegisterProxy;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
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
     * @param left service name
     * @param right ability name
     * @return name
     */
    public static String joinName(String left, String right) {
        AssertUtil.notBlank(left);
        if (StringUtils.isBlank(right)) {
            return left;
        }
        return left + "@" + right;
    }

    /**
     *
     * @param name service name
     * @param ability ability name
     * @return DefaultTaskServiceWrapper
     */
    public static AbilityTaskServiceWrapper buildTaskService(String name, String ability) {
        AssertUtil.notNull(name);
        if (StringUtils.isBlank(ability)) {
            return new RootTaskServiceWrapper(name);
        }
        return new AbilityTaskServiceWrapper(name, ability, IdentityTypeEnum.SERVICE_TASK_ABILITY);
    }

    /**
     * isTaskService
     */
    public static boolean isTaskService(TaskServiceWrapper taskServiceWrapper) {
        if (taskServiceWrapper == null) {
            return false;
        }
        if (StringUtils.isBlank(taskServiceWrapper.getName())) {
            return false;
        }
        return GlobalConstant.TASK_SERVICE_ENUM_LIST.contains(taskServiceWrapper.getIdentityType());
    }

    /**
     * 获取目标方法入参
     */
    public static Object[] getTaskParams(FlowElement flowElement, BasicStoryBus storyBus, Role role, TaskComponentRegisterProxy targetProxy,
                                         List<MethodWrapper.ParamInjectDef> paramInjectDefs, Function<MethodWrapper.ParamInjectDef, Object> paramInitStrategy) {
        AssertUtil.notNull(flowElement);
        MonitorTracking monitorTracking = storyBus.getMonitorTracking();
        Object[] params = new Object[paramInjectDefs.size()];
        for (int i = 0; i < paramInjectDefs.size(); i++) {

            // 默认 null
            params[i] = null;

            // 没有参数定义时，取默认值
            MethodWrapper.ParamInjectDef iDef = paramInjectDefs.get(i);
            if (iDef == null) {
                continue;
            }

            // 如果拿入参的 request 参数，直接赋值
            if (iDef.getScopeDataEnum() == ScopeTypeEnum.REQUEST && iDef.isInjectSelf()) {
                params[i] = storyBus.getReq();
                monitorTracking.trackingNodeParams(flowElement, () ->
                        ParamTracking.build(iDef.getFieldName(), storyBus.getReq(), ScopeTypeEnum.REQUEST, ScopeTypeEnum.REQUEST.name().toLowerCase()));
                continue;
            }

            // 如果目标类是 CustomRole 且方法入参需要 Role 时，直接透传 role
            if (targetProxy.isCustomRole() && Role.class.isAssignableFrom(iDef.getParamType())) {
                params[i] = role;
                monitorTracking.trackingNodeParams(flowElement, () -> ParamTracking.build(iDef.getFieldName(), null, ScopeTypeEnum.EMPTY, "role"));
                continue;
            }

            // 如果是基本数据类型，进行基本数据类型初始化
            boolean isPrimitive = iDef.getParamType().isPrimitive();
            if (isPrimitive) {
                params[i] = ElementParserUtil.initPrimitive(iDef.getParamType());
            }

            // 参数被 @TaskParam、@ReqTaskParam、@VarTaskParam、@StaTaskParam 注解修饰时，从 StoryBus 中直接获取变量并赋值给参数
            if (iDef.getScopeDataEnum() != null && StringUtils.isNotBlank(iDef.getTargetName())) {
                Object r = storyBus.getValue(iDef.getScopeDataEnum(), iDef.getTargetName()).orElse(null);
                if (r == PropertyUtil.GET_PROPERTY_ERROR_SIGN) {
                    monitorTracking.trackingNodeParams(flowElement, () ->
                            ParamTracking.build(iDef.getFieldName(), BAD_VALUE, iDef.getScopeDataEnum(), iDef.getTargetName()));
                    continue;
                }
                if (isPrimitive && r == null) {
                    Object primitiveFinalObj = params[i];
                    monitorTracking.trackingNodeParams(flowElement, () ->
                            ParamTracking.build(iDef.getFieldName(), primitiveFinalObj, iDef.getScopeDataEnum(), iDef.getTargetName()));
                    continue;
                }
                AssertUtil.isTrue(r == null || ElementParserUtil.isAssignable(iDef.getParamType(), r.getClass()),
                        ExceptionEnum.PARAMS_ERROR, "The actual type does not match the expected type! actual: {}, expected: {}", () -> {
                            String actual = (r == null) ? "null" : r.getClass().getName();
                            return Lists.newArrayList(actual, iDef.getParamType().getName());
                        });
                params[i] = r;
                monitorTracking.trackingNodeParams(flowElement, () -> ParamTracking.build(iDef.getFieldName(), r, iDef.getScopeDataEnum(),
                        iDef.getTargetName()));
                continue;
            }

            // case 1：参数 Bean 需要解析注入
            // case 2：参数需要 Spring 容器实例化
            // case 3：参数实现 ParamLifecycle 接口
            if (CollectionUtils.isNotEmpty(iDef.getFieldInjectDefList())
                    || iDef.isSpringInitialization() || ParamLifecycle.class.isAssignableFrom(iDef.getParamType())) {
                Object o = paramInitStrategy.apply(iDef);
                if (o instanceof ParamLifecycle) {
                    ((ParamLifecycle) o).before();
                }

                List<MethodWrapper.ParamInjectDef> fieldInjectDefList = iDef.getFieldInjectDefList();
                if (CollectionUtils.isNotEmpty(fieldInjectDefList)) {
                    fieldInjectDefList.forEach(def -> {
                        Object value = storyBus.getValue(def.getScopeDataEnum(), def.getTargetName()).orElse(null);
                        if (value == PropertyUtil.GET_PROPERTY_ERROR_SIGN) {
                            monitorTracking.trackingNodeParams(flowElement, () ->
                                    ParamTracking.build(
                                            iDef.getFieldName() + "." + def.getFieldName(), BAD_VALUE, def.getScopeDataEnum(), def.getTargetName()));
                            return;
                        }
                        AssertUtil.isTrue(value == null || ElementParserUtil.isAssignable(def.getParamType(), value.getClass()),
                                ExceptionEnum.PARAMS_ERROR, "The actual type does not match the expected type! actual: {}, expected: {}", () -> {
                                    String actual = (value == null) ? "null" : value.getClass().getName();
                                    return Lists.newArrayList(actual, def.getParamType().getName());
                                });
                        PropertyUtil.setProperty(o, def.getFieldName(), value);
                        monitorTracking.trackingNodeParams(flowElement, () ->
                                ParamTracking.build(
                                        iDef.getFieldName() + "." + def.getFieldName(), value, def.getScopeDataEnum(), def.getTargetName()));
                    });
                }

                if (o instanceof ParamLifecycle) {
                    ((ParamLifecycle) o).after();
                }
                if (GlobalUtil.supportValidate()) {
                    RequestValidator.validate(o);
                }
                params[i] = o;
            }
        }
        return params;
    }
}
