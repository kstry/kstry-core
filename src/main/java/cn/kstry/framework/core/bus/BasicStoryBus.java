/*
 *
 *  * Copyright (c) 2020-2022, Lykan (jiashuomeng@gmail.com).
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
import cn.kstry.framework.core.container.component.MethodWrapper;
import cn.kstry.framework.core.container.component.TaskServiceDef;
import cn.kstry.framework.core.engine.thread.InvokeMethodThreadLocal;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.monitor.NoticeTracking;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.ExceptionUtil;
import cn.kstry.framework.core.util.PropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static cn.kstry.framework.core.monitor.MonitorTracking.BAD_TARGET;
import static cn.kstry.framework.core.monitor.MonitorTracking.BAD_VALUE;

/**
 * BasicStoryBus
 *
 * @author lykan
 */
public class BasicStoryBus implements StoryBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicStoryBus.class);

    /**
     * 请求 ID 用来区分不同请求
     */
    private final String requestId;

    /**
     * 开始事件ID
     */
    private final String startEventId;

    /**
     * 业务ID
     */
    private final String businessId;

    /**
     * req 域
     */
    private final Object reqScopeData;

    /**
     * var 域
     */
    private final ScopeData varScopeData;

    /**
     * sta 域
     */
    private final ScopeData staScopeData;

    /**
     * return result
     */
    private volatile Object returnResult = null;

    /**
     * 角色
     */
    private final Role role;

    /**
     * 链路追踪器
     */
    private final MonitorTracking monitorTracking;

    /**
     * Bus 读写锁
     */
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * StoryBus 中的数据操作接口
     */
    private ScopeDataOperator scopeDataOperator;

    public BasicStoryBus(String requestId, String startEventId, String businessId,
                         Role role, MonitorTracking monitorTracking, Object reqScopeData, ScopeData varScopeData, ScopeData staScopeData) {
        this.role = role;
        this.requestId = requestId;
        this.startEventId = startEventId;
        this.businessId = businessId;
        this.monitorTracking = monitorTracking;
        this.reqScopeData = reqScopeData == null ? new InScopeData(ScopeTypeEnum.REQUEST) : reqScopeData;
        this.varScopeData = varScopeData == null ? new InScopeData(ScopeTypeEnum.VARIABLE) : varScopeData;
        this.staScopeData = staScopeData == null ? new InScopeData(ScopeTypeEnum.STABLE) : staScopeData;
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
    public void noticeResult(FlowElement flowElement, Object result, TaskServiceDef taskServiceDef) {
        if (result == null) {
            return;
        }

        MethodWrapper.ReturnTypeNoticeDef returnTypeNoticeDef = taskServiceDef.getMethodWrapper().getReturnTypeNoticeDef();
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            doNoticeResult(flowElement, result, returnTypeNoticeDef.getNoticeStaDefSet(), ScopeTypeEnum.STABLE);
            doNoticeResult(flowElement, result, returnTypeNoticeDef.getNoticeVarDefSet(), ScopeTypeEnum.VARIABLE);
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
                this.returnResult = r;
                monitorTracking.trackingNodeNotice(flowElement, () -> NoticeTracking.build(null, null, ScopeTypeEnum.RESULT, r));
            } else {
                LOGGER.warn("[{}] returnResult has already been assigned once and is not allowed to be assigned repeatedly! taskName: {}",
                        ExceptionEnum.IMMUTABLE_SET_UPDATE.getExceptionCode(), taskServiceDef.getName());
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

    @SuppressWarnings("unchecked")
    @Override
    public ScopeDataOperator getScopeDataOperator() {
        if (scopeDataOperator != null) {
            return scopeDataOperator;
        }
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            if (scopeDataOperator != null) {
                return scopeDataOperator;
            }
            this.scopeDataOperator = new ScopeDataOperator() {

                @Override
                public <T> T getReqScope() {
                    return (T) BasicStoryBus.this.getReq();
                }

                @Override
                public <T extends ScopeData> T getStaScope() {
                    return (T) BasicStoryBus.this.getSta();
                }

                @Override
                public <T extends ScopeData> T getVarScope() {
                    return (T) BasicStoryBus.this.getVar();
                }

                @Override
                public <T> Optional<T> getResult() {
                    return BasicStoryBus.this.getResult().map(r -> (T) r);
                }

                @Override
                public String getRequestId() {
                    return BasicStoryBus.this.requestId;
                }

                @Override
                public String getStartId() {
                    return BasicStoryBus.this.getStartId();
                }

                @Override
                public Optional<String> getBusinessId() {
                    return Optional.ofNullable(BasicStoryBus.this.getBusinessId()).filter(StringUtils::isNotBlank);
                }

                @Override
                public <T> Optional<T> getReqData(String name) {
                    if (StringUtils.isBlank(name)) {
                        return Optional.empty();
                    }
                    return BasicStoryBus.this.getValue(ScopeTypeEnum.REQUEST, name).map(r -> (T) r);
                }

                @Override
                public <T> Optional<T> getStaData(String name) {
                    if (StringUtils.isBlank(name)) {
                        return Optional.empty();
                    }
                    return BasicStoryBus.this.getValue(ScopeTypeEnum.STABLE, name).map(r -> (T) r);
                }

                @Override
                public <T> Optional<T> getVarData(String name) {
                    if (StringUtils.isBlank(name)) {
                        return Optional.empty();
                    }
                    return BasicStoryBus.this.getValue(ScopeTypeEnum.VARIABLE, name).map(r -> (T) r);
                }

                @Override
                public <T> Optional<T> getData(String expression) {
                    if (!ElementParserUtil.isValidDataExpression(expression)) {
                        return Optional.empty();
                    }
                    String[] expArr = expression.split("\\.", 2);
                    String key = (expArr.length == 2) ? expArr[1] : null;
                    return ScopeTypeEnum.of(expArr[0])
                            .filter(e -> e == ScopeTypeEnum.RESULT || StringUtils.isNotBlank(key)).flatMap(scope -> BasicStoryBus.this.getValue(scope, key).map(r -> (T) r));
                }

                @Override
                public <T> Optional<T> computeIfAbsent(String expression, Supplier<T> supplier) {
                    ReentrantReadWriteLock.WriteLock wLock = this.writeLock();
                    wLock.lock();
                    try {
                        Optional<Object> dataOptional = getData(expression);
                        if (dataOptional.isPresent()) {
                            return dataOptional.map(d -> (T) d);
                        }
                        if (!ElementParserUtil.isValidDataExpression(expression)) {
                            return Optional.empty();
                        }
                        if (Objects.equals(ScopeTypeEnum.RESULT.getKey(), expression)) {
                            T t = supplier.get();
                            if (setResult(t)) {
                                return Optional.of(t);
                            }
                            return Optional.empty();
                        }
                        String[] expArr = expression.split("\\.", 2);
                        return ScopeTypeEnum.of(expArr[0]).filter(e -> !e.isNotEdit()).map(e -> {
                            if (e == ScopeTypeEnum.STABLE) {
                                return BasicStoryBus.this.staScopeData;
                            }
                            if (e == ScopeTypeEnum.VARIABLE) {
                                return BasicStoryBus.this.varScopeData;
                            }
                            return null;
                        }).map(scope -> {
                            T t = supplier.get();
                            if (doSetData(expArr[1], scope, t)) {
                                return t;
                            }
                            return null;
                        });
                    } finally {
                        wLock.unlock();
                    }
                }

                @Override
                public boolean setData(String expression, Object target) {
                    ReentrantReadWriteLock.WriteLock wLock = this.writeLock();
                    wLock.lock();
                    try {
                        if (!ElementParserUtil.isValidDataExpression(expression)) {
                            return false;
                        }
                        if (Objects.equals(ScopeTypeEnum.RESULT.getKey(), expression)) {
                            return setResult(target);
                        }
                        String[] expArr = expression.split("\\.", 2);
                        return ScopeTypeEnum.of(expArr[0]).filter(e -> !e.isNotEdit()).map(e -> {
                            if (e == ScopeTypeEnum.VARIABLE) {
                                return BasicStoryBus.this.varScopeData;
                            }
                            if (e == ScopeTypeEnum.STABLE) {
                                return BasicStoryBus.this.staScopeData;
                            }
                            return null;
                        }).map(scope -> doSetData(expArr[1], scope, target)).orElse(false);
                    } finally {
                        wLock.unlock();
                    }
                }

                @Override
                public <T> Optional<T> iterDataItem() {
                    return InvokeMethodThreadLocal.getDataItem().map(t -> (T) t);
                }

                @Override
                public Optional<String> getTaskProperty() {
                    return InvokeMethodThreadLocal.getTaskProperty();
                }

                @Override
                public boolean setStaData(String name, Object target) {
                    return doSetData(name, BasicStoryBus.this.staScopeData, target);
                }

                @Override
                public boolean setVarData(String name, Object target) {
                    return doSetData(name, BasicStoryBus.this.varScopeData, target);
                }

                @Override
                public boolean setResult(Object target) {
                    if (target == null) {
                        return false;
                    }
                    if (BasicStoryBus.this.returnResult != null) {
                        return false;
                    }
                    ReentrantReadWriteLock.WriteLock wLock = this.writeLock();
                    wLock.lock();
                    try {
                        if (BasicStoryBus.this.returnResult != null) {
                            return false;
                        }
                        BasicStoryBus.this.returnResult = target;
                        return true;
                    } finally {
                        wLock.unlock();
                    }
                }

                @Override
                public ReentrantReadWriteLock.ReadLock readLock() {
                    return BasicStoryBus.this.readWriteLock.readLock();
                }

                @Override
                public ReentrantReadWriteLock.WriteLock writeLock() {
                    return BasicStoryBus.this.readWriteLock.writeLock();
                }

                private boolean doSetData(String name, ScopeData scopeData, Object target) {
                    if (StringUtils.isBlank(name) || scopeData.getScopeDataEnum().isNotEdit()) {
                        return false;
                    }
                    ReentrantReadWriteLock.WriteLock wLock = this.writeLock();
                    wLock.lock();
                    try {
                        Object t = scopeData;
                        String[] fieldNameSplit = name.split("\\.");
                        for (int i = 0; i < fieldNameSplit.length - 1 && t != null; i++) {
                            t = PropertyUtil.getProperty(t, fieldNameSplit[i]).filter(p -> p != PropertyUtil.GET_PROPERTY_ERROR_SIGN).orElse(null);
                        }
                        if (t == null) {
                            return false;
                        }
                        if (scopeData.getScopeDataEnum() == ScopeTypeEnum.STABLE) {
                            Optional<Object> oldResult = PropertyUtil.getProperty(t, fieldNameSplit[fieldNameSplit.length - 1]).filter(p -> p != PropertyUtil.GET_PROPERTY_ERROR_SIGN);
                            if (oldResult.isPresent()) {
                                return false;
                            }
                        }
                        String fieldName = fieldNameSplit[fieldNameSplit.length - 1];
                        return PropertyUtil.setProperty(t, fieldName, target);
                    } finally {
                        wLock.unlock();
                    }
                }
            };
            return this.scopeDataOperator;
        } finally {
            writeLock.unlock();
        }
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
                monitorTracking.trackingNodeNotice(flowElement, () -> NoticeTracking.build(def.getFieldName(), def.getTargetName(), dataEnum, BAD_TARGET));
                return;
            }

            if (ScopeTypeEnum.STABLE == dataEnum) {
                Optional<Object> oldResult = PropertyUtil.getProperty(t, fieldNameSplit[fieldNameSplit.length - 1]).filter(p -> p != PropertyUtil.GET_PROPERTY_ERROR_SIGN);
                if (oldResult.isPresent()) {
                    LOGGER.warn("[{}] Existing values in the immutable union are not allowed to be set repeatedly! k: {}, oldV: {}",
                            ExceptionEnum.IMMUTABLE_SET_UPDATE.getExceptionCode(), def.getTargetName(), oldResult.get());
                    return;
                }
            }

            Object r;
            if (def.isResultSelf()) {
                r = result;
            } else {
                r = PropertyUtil.getProperty(result, def.getFieldName()).orElse(null);
                if (r == PropertyUtil.GET_PROPERTY_ERROR_SIGN) {
                    monitorTracking.trackingNodeNotice(flowElement, () -> NoticeTracking.build(def.getFieldName(), def.getTargetName(), dataEnum, BAD_VALUE));
                    return;
                }
            }

            String fieldName = fieldNameSplit[fieldNameSplit.length - 1];
            if (!(t instanceof Map)) {
                Field field = FieldUtils.getField(t.getClass(), fieldName, true);
                AssertUtil.isTrue(field != null && ElementParserUtil.isAssignable(field.getType(), def.getFieldClass()), ExceptionEnum.TYPE_TRANSFER_ERROR,
                        "{} expect: {}, actual: {}", ExceptionEnum.TYPE_TRANSFER_ERROR.getDesc(),
                        Optional.ofNullable(field).map(Field::getType).map(Class::getName).orElse(StringUtils.EMPTY), def.getFieldClass().getName());
            }
            boolean setSuccess = PropertyUtil.setProperty(t, fieldName, r);
            if (setSuccess) {
                monitorTracking.trackingNodeNotice(flowElement, () -> NoticeTracking.build(def.getFieldName(), def.getTargetName(), dataEnum, r));
            }
        });
    }
}
