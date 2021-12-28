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
import cn.kstry.framework.core.engine.AsyncTaskCell;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author lykan
 */
public class ContextStoryBus {

    /**
     * 上下文
     */
    private final Context context = new Context();

    /**
     * StoryBus
     */
    private final StoryBus storyBus;

    /**
     * 保存将要到达执行的入度
     */
    private ConcurrentHashMap<FlowElement, List<FlowElement>> joinGatewayComingMap;

    /**
     * 上一个执行的节点
     */
    private FlowElement prevElement;

    /**
     * AsyncTaskCell
     */
    private AsyncTaskCell asyncTaskCell;

    public ContextStoryBus(StoryBus storyBus) {
        this.storyBus = storyBus;
    }

    /**
     * 获取当前有效出度数量
     *
     * @return 有效出度数量
     */
    public int getPeekCount() {
        return this.context.peekCount;
    }

    /**
     * 当前有效出度数量 +1
     */
    public void incrPeekCount() {
        this.context.peekCount++;
    }

    public void setJoinGatewayComingMap(ConcurrentHashMap<FlowElement, List<FlowElement>> joinGatewayComingMap) {
        this.joinGatewayComingMap = joinGatewayComingMap;
    }

    public ConcurrentHashMap<FlowElement, List<FlowElement>> getJoinGatewayComingMap() {
        return joinGatewayComingMap;
    }

    public FlowElement getPrevElement() {
        return prevElement;
    }

    public void setPrevElement(FlowElement prevElement) {
        this.prevElement = prevElement;
    }

    public StoryBus getScopeData() {
        return storyBus;
    }

    public AsyncTaskCell getAsyncTaskCell() {
        return asyncTaskCell;
    }

    public void setAsyncTaskCell(AsyncTaskCell asyncTaskCell) {
        this.asyncTaskCell = asyncTaskCell;
    }

    public static class Context {

        private int peekCount = 0;
    }
}
