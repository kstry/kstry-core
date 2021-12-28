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

import cn.kstry.framework.core.bus.ScopeData;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.monitor.RecallStory;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 * @author lykan
 */
public class ReqBuilder<T> {

    private final StoryRequest<T> storyRequest = new StoryRequest<>();

    private ReqBuilder() {

    }

    public static <T> ReqBuilder<T> returnType(Class<T> returnType) {
        ReqBuilder<T> builder = new ReqBuilder<>();
        builder.result(returnType);
        return builder;
    }

    public StoryRequest<T> build() {
        AssertUtil.notBlank(this.storyRequest.getStartId());
        return this.storyRequest;
    }

    public ReqBuilder<T> startId(String startId) {
        AssertUtil.notBlank(startId);
        this.storyRequest.setStartId(startId);
        return this;
    }

    public ReqBuilder<T> request(Object request) {
        if (request != null) {
            this.storyRequest.setRequest(request);
        }
        return this;
    }
    public ReqBuilder<T> businessId(String businessId) {
        this.storyRequest.setBusinessId(businessId);
        return this;
    }


    public ReqBuilder<T> trackingType(TrackingTypeEnum trackingTypeEnum) {
        this.storyRequest.setTrackingType(trackingTypeEnum);
        return this;
    }

    public ReqBuilder<T> varScopeData(ScopeData varScopeData) {
        if (varScopeData != null) {
            AssertUtil.isTrue(varScopeData.getScopeDataEnum() == ScopeTypeEnum.VARIABLE, ExceptionEnum.PARAMS_ERROR);
            this.storyRequest.setVarScopeData(varScopeData);
        }
        return this;
    }

    public ReqBuilder<T> staScopeData(ScopeData staScopeData) {
        if (staScopeData != null) {
            AssertUtil.isTrue(staScopeData.getScopeDataEnum() == ScopeTypeEnum.STABLE, ExceptionEnum.PARAMS_ERROR);
            this.storyRequest.setStaScopeData(staScopeData);
        }
        return this;
    }

    public ReqBuilder<T> role(Role role) {
        if (role != null) {
            this.storyRequest.setRole(role);
        }
        return this;
    }

    public ReqBuilder<T> timeout(Integer timeout) {
        this.storyRequest.setTimeout(timeout);
        return this;
    }

    public ReqBuilder<T> monoTimeoutFallback(Supplier<? extends T> fallback) {
        this.storyRequest.setMonoTimeoutFallback(fallback);
        return this;
    }

    public ReqBuilder<T> recallStoryHook(Consumer<RecallStory> recallStoryHook) {
        this.storyRequest.setRecallStoryHook(recallStoryHook);
        return this;
    }

    public ReqBuilder<T> requestId(String requestId) {
        this.storyRequest.setRequestId(requestId);
        return this;
    }

    private void result(Class<?> returnType) {
        this.storyRequest.setReturnType(returnType);
    }
}
