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
package cn.kstry.framework.core.engine.facade;

import cn.kstry.framework.core.bus.ScopeData;
import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.component.bpmn.lambda.LambdaParam;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.monitor.RecallStory;
import cn.kstry.framework.core.role.Role;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.LambdaUtil;

import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author lykan
 */
public class ReqBuilder<T> {

    private final StoryRequest<T> storyRequest = new StoryRequest<>();

    private ReqBuilder() {

    }

    public static <T> ReqBuilder<T> resultType(Class<?> resultType) {
        ReqBuilder<T> reqBuilder = new ReqBuilder<>();
        reqBuilder.result(resultType);
        return reqBuilder;
    }

    public static <T> ReqBuilder<T> returnType(Class<T> returnType) {
        return resultType(returnType);
    }

    public static <T> ReqBuilder<T> returnType(T returnInstance) {
        AssertUtil.notNull(returnInstance);
        return resultType(returnInstance.getClass());
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

    public <Link> ReqBuilder<T> startProcess(LambdaParam.LambdaProcess<Link> process) {
        String startId = LambdaUtil.getProcessName(process);
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

    public ReqBuilder<T> recallStoryHook(Consumer<RecallStory> recallStoryHook) {
        this.storyRequest.setRecallStoryHook(recallStoryHook);
        return this;
    }

    public ReqBuilder<T> resultBuilder(BiFunction<Object, ScopeDataQuery, T> resultBuilder) {
        this.storyRequest.setResultBuilder(resultBuilder);
        return this;
    }

    public ReqBuilder<T> requestId(String requestId) {
        this.storyRequest.setRequestId(requestId);
        return this;
    }

    public ReqBuilder<T> storyExecutor(ExecutorService storyExecutor) {
        AssertUtil.notNull(storyExecutor);
        this.storyRequest.setStoryExecutor(storyExecutor);
        return this;
    }

    public ReqBuilder<T> midwayStartId(String midwayStartId) {
        AssertUtil.notBlank(midwayStartId);
        this.storyRequest.setMidwayStartId(midwayStartId);
        return this;
    }

    private void result(Class<?> returnType) {
        this.storyRequest.setReturnType(returnType);
    }
}
