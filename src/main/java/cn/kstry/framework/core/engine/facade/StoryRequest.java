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
package cn.kstry.framework.core.engine.facade;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.kstry.framework.core.bus.ScopeData;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.core.monitor.RecallStory;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;

/**
 *
 * @author lykan
 */
public class StoryRequest<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoryEngine.class);

    /**
     * 开始节点ID
     */
    private String startId;

    /**
     * 请求 ID 用来区分不同请求
     */
    private String requestId;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * Story 返回结果类型，不返回时传 null
     */
    private Class<?> returnType;

    /**
     * 请求参数
     */
    private Object request;

    /**
     * 指定对象承载 var 域变量，默认会使用 map 结构
     */
    private ScopeData varScopeData;

    /**
     * 指定对象承载 sta 域变量，默认会使用 map 结构
     */
    private ScopeData staScopeData;

    /**
     * 角色
     */
    private Role role;

    /**
     * 任务超时时间，为空时使用全局默认超时时间
     */
    private Integer timeout;

    /**
     * 使用 storyEngine.fireAsync 时，如果任务执行超时，可通过指定该 Supplier 获取默认的返回结果
     */
    private Supplier<? extends T> monoTimeoutFallback;

    /**
     * 链路追踪级别，未指定时使用全局默认配置的级别
     */
    private TrackingTypeEnum trackingType;

    /**
     * 任务执行成功之后，会回调这个方法，传入 RecallStory
     * 通过 RecallStory 可获取：各个作用域变量、角色、最终返回结果、链路追踪器等，可以做如下的事情：
     *  - 校验最终结果是否正确
     *  - 判断节点与节点执行关系是否正确，比如：A 节点执行之后 B 节点一定执行。 A、B 节点不能同时执行等
     *  - 判断角色与节点执行关系是否正确，比如：RA 角色出现时，节点 A 一定会出现执行
     *  - 判断过滤耗时高的节点，进行报警
     */
    private Consumer<RecallStory> recallStoryHook;

    public String getStartId() {
        return startId;
    }

    public void setStartId(String startId) {
        this.startId = startId;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        if (returnType != null && returnType.isAssignableFrom(Void.class)) {
            returnType = null;
        }
        this.returnType = returnType;
    }

    public Object getRequest() {
        return request;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public ScopeData getVarScopeData() {
        return varScopeData;
    }

    public void setVarScopeData(ScopeData varScopeData) {
        AssertUtil.equals(staScopeData.getScopeDataEnum(), ScopeTypeEnum.VARIABLE);
        this.varScopeData = varScopeData;
    }

    public ScopeData getStaScopeData() {
        return staScopeData;
    }

    public void setStaScopeData(ScopeData staScopeData) {
        AssertUtil.equals(staScopeData.getScopeDataEnum(), ScopeTypeEnum.STABLE);
        this.staScopeData = staScopeData;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Supplier<? extends T> getMonoTimeoutFallback() {
        return monoTimeoutFallback;
    }

    public void setMonoTimeoutFallback(Supplier<? extends T> monoTimeoutFallback) {
        this.monoTimeoutFallback = monoTimeoutFallback;
    }

    public TrackingTypeEnum getTrackingType() {
        return trackingType;
    }

    public void setTrackingType(TrackingTypeEnum trackingType) {
        AssertUtil.notNull(trackingType);
        this.trackingType = trackingType;
    }

    public Consumer<RecallStory> getRecallStoryHook() {
        return recallStoryHook;
    }

    public void setRecallStoryHook(Consumer<RecallStory> recallStoryHook) {
        if (recallStoryHook != null) {
            this.recallStoryHook = recallStory -> {
                try {
                    recallStoryHook.accept(recallStory);
                } catch (Throwable exception) {
                    LOGGER.warn(exception.getMessage(), exception);
                }
            };
        }
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }
}
