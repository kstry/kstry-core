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
package cn.kstry.framework.core.bus;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.component.conversion.TypeConverterProcessor;
import cn.kstry.framework.core.container.component.MethodWrapper;
import cn.kstry.framework.core.container.component.TaskServiceDef;
import cn.kstry.framework.core.engine.thread.InvokeMethodThreadLocal;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.monitor.NoticeTracking;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.*;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.kstry.framework.core.monitor.MonitorTracking.BAD_TARGET;
import static cn.kstry.framework.core.monitor.MonitorTracking.BAD_VALUE;

/**
 * BasicStoryBus
 *
 * @author lykan
 */
public abstract class BasicStoryBus implements StoryBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicStoryBus.class);

    /**
     * StoryBus 创建时间作为流程开始时间
     */
    final long beginTimeMillis = System.currentTimeMillis();

    /**
     * 请求的总超时时间
     */
    final long timeoutMillis;

    /**
     * 请求 ID 用来区分不同请求
     */
    final String requestId;

    /**
     * 开始事件ID
     */
    final String startEventId;

    /**
     * 业务ID
     */
    final String businessId;

    /**
     * req 域
     */
    final Object reqScopeData;

    /**
     * var 域
     */
    final ScopeData varScopeData;

    /**
     * sta 域
     */
    final ScopeData staScopeData;

    /**
     * return result
     */
    volatile Object returnResult = null;

    /**
     * 角色
     */
    final Role role;

    /**
     * 链路追踪器
     */
    final MonitorTracking monitorTracking;

    /**
     * Bus 读写锁
     */
    final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * 指定当前任务使用的任务执行器
     */
    final ThreadPoolExecutor storyExecutor;

    /**
     * 类型转换处理器
     */
    final TypeConverterProcessor typeConverterProcessor;

    /**
     * Story 返回值类型
     */
    final Class<?> returnType;

    final boolean setReqScope;

    public BasicStoryBus(TypeConverterProcessor typeConverterProcessor, Class<?> returnType, int timeout, ThreadPoolExecutor storyExecutor, String requestId,
                         String startEventId, String businessId, Role role, MonitorTracking monitorTracking, Object reqScopeData, ScopeData varScopeData, ScopeData staScopeData) {
        this.role = role;
        this.requestId = requestId;
        this.timeoutMillis = timeout;
        this.returnType = returnType;
        this.startEventId = startEventId;
        this.businessId = businessId;
        this.storyExecutor = storyExecutor;
        this.monitorTracking = monitorTracking;
        this.setReqScope = reqScopeData != null;
        this.reqScopeData = reqScopeData == null ? new InScopeData(ScopeTypeEnum.REQUEST) : reqScopeData;
        this.varScopeData = varScopeData == null ? new InScopeData(ScopeTypeEnum.VARIABLE) : varScopeData;
        this.staScopeData = staScopeData == null ? new InScopeData(ScopeTypeEnum.STABLE) : staScopeData;
        this.typeConverterProcessor = typeConverterProcessor;
    }

    public boolean isSetReqScope() {
        return setReqScope;
    }

    @Override
    public Object getReq() {
        return reqScopeData;
    }

    @Override
    public ScopeData getVar() {
        return varScopeData;
    }

    @Override
    public ScopeData getSta() {
        return staScopeData;
    }

    @Override
    public Optional<Object> getResult() {
        return Optional.ofNullable(returnResult);
    }

    @Override
    public Optional<Object> getValue(ScopeTypeEnum scopeTypeEnum, String key) {
        if (scopeTypeEnum == null) {
            return Optional.empty();
        }
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            if (scopeTypeEnum == ScopeTypeEnum.RESULT) {
                return getResult();
            }
            if (scopeTypeEnum == ScopeTypeEnum.STABLE) {
                return PropertyUtil.getProperty(getSta(), key);
            } else if (scopeTypeEnum == ScopeTypeEnum.VARIABLE) {
                return PropertyUtil.getProperty(getVar(), key);
            } else if (scopeTypeEnum == ScopeTypeEnum.REQUEST) {
                return PropertyUtil.getProperty(getReq(), key);
            } else {
                throw ExceptionUtil.buildException(null, ExceptionEnum.STORY_ERROR, null);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Role getRole() {
        return role;
    }

    @Override
    public void noticeResult(ServiceTask serviceTask, Object result, TaskServiceDef taskServiceDef) {
        if (result == null) {
            return;
        }
        if (result instanceof ScopeDataNotice) {
            InvokeMethodThreadLocal.setServiceTask(serviceTask);
            try {
                ScopeDataOperator scopeDataOperator = getScopeDataOperator();
                ScopeDataNotice scopeDataNotice = GlobalUtil.transferNotEmpty(result, ScopeDataNotice.class);
                scopeDataOperator.setResult(scopeDataNotice.getResult());
                if (scopeDataNotice.isNoticeStaScope()) {
                    scopeDataNotice.forEach((k, v) -> {
                        if (scopeDataNotice.getExcludeKeyStaSet().contains(k)) {
                            return;
                        }
                        scopeDataOperator.setStaData(k, v);
                    });
                }
                if (scopeDataNotice.isNoticeVarScope()) {
                    scopeDataNotice.forEach((k, v) -> {
                        if (scopeDataNotice.getExcludeKeyVarSet().contains(k)) {
                            return;
                        }
                        scopeDataOperator.setVarData(k, v);
                    });
                }
                return;
            } finally {
                InvokeMethodThreadLocal.clearServiceTask();
            }
        }
        MethodWrapper.ReturnTypeNoticeDef returnTypeNoticeDef = taskServiceDef.getMethodWrapper().getReturnTypeNoticeDef();
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            doNoticeResult(serviceTask, result, returnTypeNoticeDef.getNoticeStaDefSet(), ScopeTypeEnum.STABLE);
            doNoticeResult(serviceTask, result, returnTypeNoticeDef.getNoticeVarDefSet(), ScopeTypeEnum.VARIABLE);
            if (returnTypeNoticeDef.getStoryResultDef() == null) {
                return;
            }
            MethodWrapper.NoticeFieldItem srDef = returnTypeNoticeDef.getStoryResultDef();
            Object r;
            if (srDef.isResultSelf()) {
                r = result;
            } else {
                r = PropertyUtil.getProperty(result, srDef.getFieldName()).filter(p -> p != PropertyUtil.GET_PROPERTY_ERROR_SIGN).orElse(null);
            }
            if (this.returnResult == null) {
                Pair<String, ?> convertPair = typeConverterProcessor.convert(srDef.getConverter(), r, returnType);
                r = convertPair.getValue();
                this.returnResult = r;
                monitorTracking.trackingNodeNotice(serviceTask, () -> NoticeTracking.build(
                        null, null, ScopeTypeEnum.RESULT, this.returnResult, Optional.ofNullable(this.returnResult).map(Object::getClass).orElse(null), convertPair.getKey())
                );
            } else {
                LOGGER.warn("[{}] returnResult has already been assigned once and is not allowed to be assigned repeatedly! taskName: {}, identity: {}",
                        ExceptionEnum.IMMUTABLE_SET_UPDATE.getExceptionCode(), taskServiceDef.getName(), serviceTask.identity());
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public MonitorTracking getMonitorTracking() {
        AssertUtil.notNull(monitorTracking);
        return monitorTracking;
    }

    @Override
    public String getBusinessId() {
        return businessId;
    }

    @Override
    public String getStartId() {
        return startEventId;
    }

    @Override
    public ThreadPoolExecutor getStoryExecutor() {
        return storyExecutor;
    }

    @Override
    public int remainTimeMillis() {
        int t = (int) (timeoutMillis - (System.currentTimeMillis() - beginTimeMillis));
        return Math.max(t, 0);
    }

    private void doNoticeResult(FlowElement flowElement, Object result, Set<MethodWrapper.NoticeFieldItem> noticeStaDefSet, ScopeTypeEnum dataEnum) {
        if (CollectionUtils.isEmpty(noticeStaDefSet)) {
            return;
        }

        MonitorTracking monitorTracking = getMonitorTracking();
        ScopeData data;
        if (ScopeTypeEnum.STABLE == dataEnum) {
            data = staScopeData;
        } else if (ScopeTypeEnum.VARIABLE == dataEnum) {
            data = varScopeData;
        } else {
            throw ExceptionUtil.buildException(null, ExceptionEnum.STORY_ERROR, null);
        }

        noticeStaDefSet.forEach(def -> {
            Object t = data;
            String[] fieldNameSplit = def.getTargetName().split("\\.");
            for (int i = 0; i < fieldNameSplit.length - 1 && t != null; i++) {
                t = PropertyUtil.getProperty(t, fieldNameSplit[i]).filter(p -> p != PropertyUtil.GET_PROPERTY_ERROR_SIGN).orElse(null);
            }
            if (t == null) {
                monitorTracking.trackingNodeNotice(flowElement, () -> NoticeTracking.build(def.getFieldName(), def.getTargetName(), dataEnum, BAD_TARGET, data.getClass(), null));
                return;
            }

            if (ScopeTypeEnum.STABLE == dataEnum) {
                Optional<Object> oldResult = PropertyUtil.getProperty(t, fieldNameSplit[fieldNameSplit.length - 1]).filter(p -> p != PropertyUtil.GET_PROPERTY_ERROR_SIGN);
                if (oldResult.isPresent()) {
                    LOGGER.warn("[{}] Existing values in the immutable union are not allowed to be set repeatedly! k: {}, oldV: {}, identity: {}",
                            ExceptionEnum.IMMUTABLE_SET_UPDATE.getExceptionCode(), def.getTargetName(), oldResult.get(), flowElement.identity());
                    return;
                }
            }

            Object r;
            Class<?> targetClass = t.getClass();
            if (def.isResultSelf()) {
                r = result;
            } else {
                r = PropertyUtil.getProperty(result, def.getFieldName()).orElse(null);
                if (r == PropertyUtil.GET_PROPERTY_ERROR_SIGN) {
                    monitorTracking.trackingNodeNotice(flowElement, () -> NoticeTracking.build(def.getFieldName(), def.getTargetName(), dataEnum, BAD_VALUE, targetClass, null));
                    return;
                }
            }

            Pair<String, ?> convertPair;
            String fieldName = fieldNameSplit[fieldNameSplit.length - 1];
            if (t instanceof Map) {
                convertPair = typeConverterProcessor.convert(def.getConverter(), r);
                r = convertPair.getValue();
            } else {
                Field field = ProxyUtil.getField(t.getClass(), fieldName).orElse(null);
                convertPair = typeConverterProcessor.convert(def.getConverter(), r, Optional.ofNullable(field).map(Field::getType).orElse(null), ProxyUtil.getCollGenericType(field).orElse(null));
                r = convertPair.getValue();
                Object rFinal = r;
                AssertUtil.isTrue(r == null || (field != null && ElementParserUtil.isAssignable(field.getType(), r.getClass())),
                        ExceptionEnum.TYPE_TRANSFER_ERROR, "Return value result notification exceptions. fieldName: {}, expect: {}, actual: {}, identity: {}",
                        () -> {
                            String actual = (rFinal == null) ? "null" : rFinal.getClass().getName();
                            String expect = Optional.ofNullable(field).map(Field::getType).map(Class::getName).orElse(StringUtils.EMPTY);
                            return Lists.newArrayList(def.getTargetName(), expect, actual, flowElement.identity());
                        });
            }
            Object rFinal = r;
            boolean setSuccess = PropertyUtil.setProperty(t, fieldName, rFinal);
            if (setSuccess) {
                monitorTracking.trackingNodeNotice(flowElement, () -> NoticeTracking.build(def.getFieldName(), def.getTargetName(), dataEnum, rFinal, targetClass, convertPair.getKey()));
            }
        });
    }
}
