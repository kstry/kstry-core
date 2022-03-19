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
package cn.kstry.framework.core.bus;

import cn.kstry.framework.core.bpmn.FlowElement;
import cn.kstry.framework.core.container.MethodWrapper;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.monitor.NoticeTracking;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.task.facade.TaskServiceDef;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.PropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    public BasicStoryBus(String businessId, Role role, MonitorTracking monitorTracking, Object reqScopeData, ScopeData varScopeData, ScopeData staScopeData) {
        this.role = role;
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
    public Object getResult() {
        return returnResult;
    }

    @Override
    public Optional<Object> getValue(ScopeTypeEnum scopeTypeEnum, String key) {
        if (scopeTypeEnum == null) {
            return Optional.empty();
        }

        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            if (scopeTypeEnum == ScopeTypeEnum.STABLE) {
                return PropertyUtil.getProperty(getSta(), key);
            } else if (scopeTypeEnum == ScopeTypeEnum.VARIABLE) {
                return PropertyUtil.getProperty(getVar(), key);
            } else if (scopeTypeEnum == ScopeTypeEnum.REQUEST) {
                return PropertyUtil.getProperty(getReq(), key);
            } else {
                KstryException.throwException(ExceptionEnum.STORY_ERROR);
            }
            return Optional.empty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Optional<Role> getRole() {
        return Optional.ofNullable(role);
    }

    @Override
    public void noticeResult(FlowElement flowElement, Object result, TaskServiceDef taskServiceDef) {
        if (result == null || taskServiceDef.getTaskComponentTarget().isCustomRole()) {
            return;
        }

        MethodWrapper.ReturnTypeNoticeDef returnTypeNoticeDef = taskServiceDef.getMethodWrapper().getReturnTypeNoticeDef();
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            doNoticeResult(flowElement, result, returnTypeNoticeDef.getNoticeStaDefSet(), ScopeTypeEnum.STABLE);
            doNoticeResult(flowElement, result, returnTypeNoticeDef.getNoticeVarDefSet(), ScopeTypeEnum.VARIABLE);
            if (returnTypeNoticeDef.getStoryResultDef() != null) {
                if (this.returnResult == null) {
                    this.returnResult = result;
                    monitorTracking.trackingNodeNotice(flowElement, () -> NoticeTracking.build(null, null, ScopeTypeEnum.RESULT, result));
                } else {
                    LOGGER.warn("[{}] returnResult has already been assigned once and is not allowed to be assigned repeatedly! taskName: {}",
                            ExceptionEnum.IMMUTABLE_SET_UPDATE.getExceptionCode(), taskServiceDef.getName());
                }
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
            KstryException.throwException(ExceptionEnum.STORY_ERROR);
            return;
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
                Optional<Object> oldResult =
                        PropertyUtil.getProperty(t, fieldNameSplit[fieldNameSplit.length - 1]).filter(p -> p != PropertyUtil.GET_PROPERTY_ERROR_SIGN);
                if (oldResult.isPresent()) {
                    LOGGER.warn("[{}] Existing values in the immutable union are not allowed to be set repeatedly! k:{}, oldV:{}",
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
            PropertyUtil.setProperty(t, fieldName, r);
            monitorTracking.trackingNodeNotice(flowElement, () -> NoticeTracking.build(def.getFieldName(), def.getTargetName(), dataEnum, r));
        });
    }

    @Override
    public ScopeData getVar() {
        return varScopeData;
    }

    @Override
    public ScopeData getSta() {
        return staScopeData;
    }
}
