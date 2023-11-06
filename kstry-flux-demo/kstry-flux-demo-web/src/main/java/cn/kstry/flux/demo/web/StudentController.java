/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.flux.demo.web;

import cn.kstry.flux.demo.facade.R;
import cn.kstry.flux.demo.facade.student.QueryScoreRequest;
import cn.kstry.flux.demo.facade.student.QueryScoreResponse;
import cn.kstry.flux.demo.facade.student.QueryScoreVarScope;
import cn.kstry.flux.demo.process.DefProcess;
import cn.kstry.flux.demo.util.WebUtil;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


/**
 * @author lykan
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    @Resource
    private StoryEngine storyEngine;

    @PostMapping("/studentQuery")
    public R<QueryScoreResponse> studentQuery() {
        QueryScoreRequest request = new QueryScoreRequest();
        request.setStudentId(77L);
        request.setNeedScore(true);
        StoryRequest<QueryScoreResponse> fireRequest = ReqBuilder
                .returnType(QueryScoreResponse.class) // 指定返回类型
                .recallStoryHook(WebUtil::recallStoryHook) // 流程结束的回调
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL) // 指定监控类型
                .request(request) // 指定req域参数
                .varScopeData(new QueryScoreVarScope()) // 指定var域数据载体，可不指定使用默认值
                .startId("student-score-query-process") // 指定开始事件
                .build();
        TaskResponse<QueryScoreResponse> result = storyEngine.fire(fireRequest);
        return result.isSuccess() ? R.success(result.getResult()) : R.error(NumberUtils.toInt(result.getResultCode(), -1), result.getResultDesc());
    }

    @PostMapping("/scoreQuery")
    public Mono<R<QueryScoreResponse>> scoreQuery() {
        QueryScoreRequest request = new QueryScoreRequest();
        request.setStudentId(66L);
        request.setNeedScore(true);
        QueryScoreVarScope varScopeData = new QueryScoreVarScope();
        StoryRequest<QueryScoreResponse> fireRequest = ReqBuilder.returnType(QueryScoreResponse.class).recallStoryHook(WebUtil::recallStoryHook)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).varScopeData(varScopeData).startId("student-score-query-process").build();
        Mono<QueryScoreResponse> fireAsync = storyEngine.fireAsync(fireRequest);
        return WebUtil.dataDecorate(request, fireAsync);
    }

    @PostMapping("/scoreQuery2")
    public Mono<R<QueryScoreResponse>> scoreQuery2() {
        QueryScoreRequest request = new QueryScoreRequest();
        request.setStudentId(66L);
        request.setNeedScore(true);
        QueryScoreVarScope varScopeData = new QueryScoreVarScope();
        StoryRequest<QueryScoreResponse> fireRequest = ReqBuilder.returnType(QueryScoreResponse.class).recallStoryHook(WebUtil::recallStoryHook)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).varScopeData(varScopeData).startId("student-score-query-json-process").build();
        Mono<QueryScoreResponse> fireAsync = storyEngine.fireAsync(fireRequest);
        return WebUtil.dataDecorate(request, fireAsync);
    }

    @PostMapping("/scoreQuery3")
    public Mono<R<QueryScoreResponse>> scoreQuery3() {
        QueryScoreRequest request = new QueryScoreRequest();
        request.setStudentId(66L);
        request.setNeedScore(true);
        QueryScoreVarScope varScopeData = new QueryScoreVarScope();
        StoryRequest<QueryScoreResponse> fireRequest = ReqBuilder.returnType(QueryScoreResponse.class).recallStoryHook(WebUtil::recallStoryHook)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).request(request).varScopeData(varScopeData).startProcess(DefProcess::studentScoreQueryProcess).build();
        Mono<QueryScoreResponse> fireAsync = storyEngine.fireAsync(fireRequest);
        return WebUtil.dataDecorate(request, fireAsync);
    }
}
