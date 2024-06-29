/*
 *  * Copyright (c) 2020-2024, xiongzhongwei (xiongzhongwei@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express   or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 */
package cn.kstry.framework.core.component.event;

import cn.kstry.framework.core.monitor.NodeTracking;
import org.springframework.context.ApplicationEvent;

public class TrackingFinishEvent extends ApplicationEvent {

    private final String startId;

    private final String businessId;

    private final String requestId;

    private final Throwable exception;

    public TrackingFinishEvent(String startId, String businessId, String requestId, NodeTracking nodeTracking, Throwable exception) {
        super(nodeTracking);
        this.exception = exception;
        this.startId = startId;
        this.businessId = businessId;
        this.requestId = requestId;
    }

    public NodeTracking getNodeTracking() {
        return (NodeTracking) source;
    }

    public Throwable getException() {
        return exception;
    }

    public String getStartId() {
        return startId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getRequestId() {
        return requestId;
    }
}
