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
package cn.kstry.framework.core.engine.thread.hook;

import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.exception.ExceptionEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Objects;


public class ThreadSwitchLogHook implements ThreadSwitchHook<TraceLog> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadSwitchLogHook.class);

    @Override
    public TraceLog getPreviousData(ScopeDataQuery scopeDataQuery) {
        String mdcKeyName = GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME;
        TraceLog traceLog = new TraceLog();
        traceLog.setLogId(MDC.get(mdcKeyName));
        if (StringUtils.isBlank(traceLog.getLogId())) {
            LOGGER.warn("[{}] Failed to get logId during thread switch. mdcKeyName: {}, threadName:{}",
                    ExceptionEnum.THREAD_SWITCH_HOOK_ERROR.getExceptionCode(), mdcKeyName, Thread.currentThread().getName());
        }
        return traceLog;
    }

    @Override
    public void usePreviousData(TraceLog traceLog, ScopeDataQuery scopeDataQuery) {
        String mdcKeyName = GlobalProperties.KSTRY_STORY_REQUEST_ID_NAME;
        if (traceLog == null || StringUtils.isBlank(traceLog.getLogId())) {
            LOGGER.warn("[{}] Failed to use logId during thread switch. mdcKeyName: {}, threadName:{}, traceLog: {}",
                    ExceptionEnum.THREAD_SWITCH_HOOK_ERROR.getExceptionCode(), mdcKeyName, Thread.currentThread().getName(), traceLog);
            return;
        }
        if (!Objects.equals(traceLog.getLogId(), scopeDataQuery.getRequestId())) {
            LOGGER.warn("[{}] The logId saved in the MDC is not the same as the logId saved in the request. mdcKeyName: {}, mdcRequestId: {}, requestId:{}",
                    mdcKeyName, ExceptionEnum.THREAD_SWITCH_HOOK_ERROR.getExceptionCode(), traceLog.getLogId(), scopeDataQuery.getRequestId());
        }
        MDC.put(mdcKeyName, traceLog.getLogId());
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
