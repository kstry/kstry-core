/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
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
import cn.kstry.framework.core.component.conversion.TypeConverterProcessor;
import cn.kstry.framework.core.engine.thread.InvokeMethodThreadLocal;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.monitor.NoticeTracking;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.PropertyUtil;
import cn.kstry.framework.core.util.ProxyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * @author lykan
 */
public class OperatorStoryBus extends BasicStoryBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperatorStoryBus.class);

    /**
     * StoryBus 中的数据操作接口
     */
    private EnhancedDataOperator enhancedDataOperator;

    public OperatorStoryBus(TypeConverterProcessor typeConverterProcessor, Class<?> returnType, int timeout, ExecutorService storyExecutor, String requestId,
                            String startEventId, String businessId, Role role, MonitorTracking monitorTracking, Object reqScopeData, ScopeData varScopeData, ScopeData staScopeData) {
        super(typeConverterProcessor, returnType, timeout, storyExecutor, requestId, startEventId, businessId, role, monitorTracking, reqScopeData, varScopeData, staScopeData);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ScopeDataOperator getScopeDataOperator() {
        if (enhancedDataOperator != null) {
            return enhancedDataOperator;
        }

        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            if (enhancedDataOperator != null) {
                return enhancedDataOperator;
            }
            this.enhancedDataOperator = new EnhancedDataOperator() {

                @Override
                public <T> Optional<T> getResult(String typeConverter) {
                    if (StringUtils.isBlank(typeConverter)) {
                        return getResult();
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(typeConverter, getResult()).getValue()).map(t -> (T) t);
                }

                @Override
                public <T> Optional<T> getReqData(String name, String typeConverter) {
                    if (StringUtils.isBlank(typeConverter)) {
                        return getReqData(name);
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(typeConverter, getReqData(name)).getValue()).map(t -> (T) t);
                }

                @Override
                public <T> Optional<T> getStaData(String name, String typeConverter) {
                    if (StringUtils.isBlank(typeConverter)) {
                        return getStaData(name);
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(typeConverter, getStaData(name)).getValue()).map(t -> (T) t);
                }

                @Override
                public <T> Optional<T> getVarData(String name, String typeConverter) {
                    if (StringUtils.isBlank(typeConverter)) {
                        return getVarData(name);
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(typeConverter, getVarData(name)).getValue()).map(t -> (T) t);
                }

                @Override
                public <T> Optional<T> getData(String expression, String typeConverter) {
                    if (StringUtils.isBlank(typeConverter)) {
                        return getData(expression);
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(typeConverter, getData(expression)).getValue()).map(t -> (T) t);
                }

                @Override
                public <T> Optional<T> getData(String expression, List<String> typeConverterList) {
                    Optional<T> dataOptional = getData(expression);
                    if (CollectionUtils.isEmpty(typeConverterList)) {
                        return dataOptional;
                    }
                    Object r = dataOptional;
                    for (String typeConverter : typeConverterList) {
                        r = typeConverterProcessor.convert(typeConverter, r).getValue();
                    }
                    return GlobalUtil.getResOptional(r).map(t -> (T) t);
                }

                @Override
                public <T> Optional<T> getTaskProperty(String typeConverter) {
                    if (StringUtils.isBlank(typeConverter)) {
                        return getTaskProperty().map(t -> (T) t);
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(typeConverter, getTaskProperty()).getValue()).map(t -> (T) t);
                }

                @Override
                public <T> Optional<T> iterDataItem(String typeConverter) {
                    if (StringUtils.isBlank(typeConverter)) {
                        return iterDataItem();
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(typeConverter, iterDataItem()).getValue()).map(t -> (T) t);
                }

                @Override
                public <T> Optional<T> getResult(Class<T> targetClass) {
                    if (targetClass == null) {
                        return getResult();
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(getResult(), targetClass).getValue());
                }

                @Override
                public <T> Optional<T> getReqData(String name, Class<T> targetClass) {
                    if (targetClass == null) {
                        return getReqData(name);
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(getReqData(name), targetClass).getValue());
                }

                @Override
                public <T> Optional<T> getStaData(String name, Class<T> targetClass) {
                    if (targetClass == null) {
                        return getStaData(name);
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(getStaData(name), targetClass)).map(t -> (T) t);
                }

                @Override
                public <T> Optional<T> getVarData(String name, Class<T> targetClass) {
                    if (targetClass == null) {
                        return getVarData(name);
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(getVarData(name), targetClass)).map(t -> (T) t);
                }

                @Override
                public <T> Optional<T> getData(String expression, Class<T> targetClass) {
                    if (targetClass == null) {
                        return getData(expression);
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(getData(expression), targetClass)).map(t -> (T) t);
                }

                @Override
                public <T> Optional<T> getTaskProperty(Class<T> targetClass) {
                    if (targetClass == null) {
                        return getTaskProperty().map(t -> (T) t);
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(getTaskProperty(), targetClass)).map(t -> (T) t);
                }

                @Override
                public <T> Optional<T> iterDataItem(Class<T> targetClass) {
                    if (targetClass == null) {
                        return iterDataItem();
                    }
                    return GlobalUtil.getResOptional(typeConverterProcessor.convert(iterDataItem(), targetClass)).map(t -> (T) t);
                }

                @Override
                public <T> T getReqScope() {
                    return (T) OperatorStoryBus.this.getReq();
                }

                @Override
                public <T extends ScopeData> T getStaScope() {
                    return (T) OperatorStoryBus.this.getSta();
                }

                @Override
                public <T extends ScopeData> T getVarScope() {
                    return (T) OperatorStoryBus.this.getVar();
                }

                @Override
                public <T> Optional<T> getResult() {
                    return OperatorStoryBus.this.getResult().map(r -> (T) r);
                }

                @Override
                public String getRequestId() {
                    return OperatorStoryBus.this.requestId;
                }

                @Override
                public String getStartId() {
                    return OperatorStoryBus.this.getStartId();
                }

                @Override
                public Optional<String> getBusinessId() {
                    return Optional.ofNullable(OperatorStoryBus.this.getBusinessId()).filter(StringUtils::isNotBlank);
                }

                @Override
                public <T> Optional<T> getReqData(String name) {
                    if (StringUtils.isBlank(name)) {
                        return Optional.empty();
                    }
                    return OperatorStoryBus.this.getValue(ScopeTypeEnum.REQUEST, name).map(r -> (T) r);
                }

                @Override
                public <T> Optional<T> getStaData(String name) {
                    if (StringUtils.isBlank(name)) {
                        return Optional.empty();
                    }
                    return OperatorStoryBus.this.getValue(ScopeTypeEnum.STABLE, name).map(r -> (T) r);
                }

                @Override
                public <T> Optional<T> getVarData(String name) {
                    if (StringUtils.isBlank(name)) {
                        return Optional.empty();
                    }
                    return OperatorStoryBus.this.getValue(ScopeTypeEnum.VARIABLE, name).map(r -> (T) r);
                }

                @Override
                public <T> Optional<T> getData(String expression) {
                    if (StringUtils.isBlank(expression)) {
                        return Optional.empty();
                    }
                    if (!ElementParserUtil.isValidDataExpression(expression)) {
                        return Optional.empty();
                    }
                    String[] expArr = PropertyUtil.switchPositionExpression(expression).split("\\.", 2);
                    String key = (expArr.length == 2) ? expArr[1] : null;
                    return ScopeTypeEnum.of(expArr[0])
                            .filter(e -> e == ScopeTypeEnum.RESULT || StringUtils.isNotBlank(key)).flatMap(scope -> OperatorStoryBus.this.getValue(scope, key).map(r -> (T) r));
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
                                return OperatorStoryBus.this.staScopeData;
                            }
                            if (e == ScopeTypeEnum.VARIABLE) {
                                return OperatorStoryBus.this.varScopeData;
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
                                return OperatorStoryBus.this.varScopeData;
                            }
                            if (e == ScopeTypeEnum.STABLE) {
                                return OperatorStoryBus.this.staScopeData;
                            }
                            return null;
                        }).map(scope -> doSetData(expArr[1], scope, target)).orElse(false);
                    } finally {
                        wLock.unlock();
                    }
                }

                @Override
                public <T> Optional<T> iterDataItem() {
                    return InvokeMethodThreadLocal.getDataItem().map(t -> (T) (t.isBatch() ? t.getDataList() : t.getData().orElse(null)));
                }

                @Override
                public Optional<String> getTaskProperty() {
                    return InvokeMethodThreadLocal.getTaskProperty();
                }

                @Override
                public boolean setStaData(String name, Object target) {
                    return doSetData(name, OperatorStoryBus.this.staScopeData, target);
                }

                @Override
                public boolean setVarData(String name, Object target) {
                    return doSetData(name, OperatorStoryBus.this.varScopeData, target);
                }

                @Override
                public boolean setResult(Object target) {
                    if (target == null) {
                        return false;
                    }
                    if (OperatorStoryBus.this.returnResult != null) {
                        return false;
                    }
                    ReentrantReadWriteLock.WriteLock wLock = this.writeLock();
                    wLock.lock();
                    try {
                        if (OperatorStoryBus.this.returnResult != null) {
                            return false;
                        }
                        Pair<String, ?> convert = typeConverterProcessor.convert(target, returnType);
                        OperatorStoryBus.this.returnResult = convert.getValue();
                        InvokeMethodThreadLocal.getServiceTask().ifPresent(element ->
                                monitorTracking.trackingNodeNotice(element, () ->
                                        NoticeTracking.build(null, null, ScopeTypeEnum.RESULT, convert.getValue(), target.getClass(), convert.getKey())
                                )
                        );
                        return true;
                    } finally {
                        wLock.unlock();
                    }
                }

                @Override
                public ReentrantReadWriteLock.ReadLock readLock() {
                    return OperatorStoryBus.this.readWriteLock.readLock();
                }

                @Override
                public ReentrantReadWriteLock.WriteLock writeLock() {
                    return OperatorStoryBus.this.readWriteLock.writeLock();
                }

                private boolean doSetData(String name, ScopeData scopeData, Object target) {
                    if (StringUtils.isBlank(name) || scopeData.getScopeDataEnum().isNotEdit()) {
                        return false;
                    }
                    ReentrantReadWriteLock.WriteLock wLock = this.writeLock();
                    wLock.lock();
                    try {
                        Object t = scopeData;
                        String childFieldName = null;
                        String[] fieldNameSplit = name.split("\\.");
                        for (int i = 0; i < fieldNameSplit.length - 1 && t != null; i++) {
                            t = PropertyUtil.getProperty(t, fieldNameSplit[i]).filter(p -> p != PropertyUtil.GET_PROPERTY_ERROR_SIGN).orElse(null);
                            childFieldName = fieldNameSplit[i];
                        }
                        if (t == null) {
                            LOGGER.info("ScopeDataOperator set {} fail. invalid field name: {}", name, childFieldName);
                            return false;
                        }
                        if (scopeData.getScopeDataEnum() == ScopeTypeEnum.STABLE) {
                            Optional<Object> oldResult = PropertyUtil.getProperty(t, fieldNameSplit[fieldNameSplit.length - 1]).filter(p -> p != PropertyUtil.GET_PROPERTY_ERROR_SIGN);
                            if (oldResult.isPresent()) {
                                LOGGER.warn("[{}] Existing values in the immutable union are not allowed to be set repeatedly! k: {}, oldV: {}, identity: {}",
                                        ExceptionEnum.IMMUTABLE_SET_UPDATE.getExceptionCode(), name, oldResult.get(), InvokeMethodThreadLocal.getServiceTask().map(FlowElement::identity).orElse(StringUtils.EMPTY));
                                return false;
                            }
                        }
                        Class<?> targetClass = t.getClass();
                        String fieldName = fieldNameSplit[fieldNameSplit.length - 1];
                        Field field = (t instanceof Map) ? null : ProxyUtil.getField(targetClass, fieldName).orElse(null);
                        Pair<String, ?> convert = typeConverterProcessor.convert(null, target, Optional.ofNullable(field).map(Field::getType).orElse(null), ProxyUtil.getCollGenericType(field).orElse(null));
                        boolean setRes = PropertyUtil.setProperty(t, fieldName, convert.getValue());
                        InvokeMethodThreadLocal.getServiceTask().filter(s -> setRes).ifPresent(element ->
                                monitorTracking.trackingNodeNotice(element, () ->
                                        NoticeTracking.build(null, name, scopeData.getScopeDataEnum(), convert.getValue(), targetClass, convert.getKey())
                                )
                        );
                        return setRes;
                    } finally {
                        wLock.unlock();
                    }
                }
            };
            return this.enhancedDataOperator;
        } finally {
            writeLock.unlock();
        }
    }
}
