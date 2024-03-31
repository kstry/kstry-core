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
import cn.kstry.framework.core.engine.thread.EndTaskPedometer;
import cn.kstry.framework.core.util.AssertUtil;

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
    private ConcurrentHashMap<FlowElement, List<ElementArriveRecord>> joinGatewayComingMap;

    /**
     * 上一个执行的节点
     */
    private FlowElement prevElement;

    /**
     * 结束任务计步器
     */
    private EndTaskPedometer endTaskPedometer;

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

    public void setJoinGatewayComingMap(ConcurrentHashMap<FlowElement, List<ElementArriveRecord>> joinGatewayComingMap) {
        this.joinGatewayComingMap = joinGatewayComingMap;
    }

    public ConcurrentHashMap<FlowElement, List<ElementArriveRecord>> getJoinGatewayComingMap() {
        return joinGatewayComingMap;
    }

    public FlowElement getPrevElement() {
        return prevElement;
    }

    public void setPrevElement(FlowElement prevElement) {
        this.prevElement = prevElement;
    }

    public StoryBus getStoryBus() {
        return storyBus;
    }

    public EndTaskPedometer getEndTaskPedometer() {
        return endTaskPedometer;
    }

    public void setEndTaskPedometer(EndTaskPedometer endTaskPedometer) {
        this.endTaskPedometer = endTaskPedometer;
    }

    public static class Context {

        private int peekCount = 0;
    }

    public static class ElementArriveRecord {

        /**
         * 元素
         */
        private final FlowElement flowElement;

        /**
         * 是否达到
         */
        private boolean arrive;

        /**
         * 实际到达过
         */
        private boolean actualArrive;

        public ElementArriveRecord(FlowElement flowElement) {
            AssertUtil.notNull(flowElement);
            this.flowElement = flowElement;
            this.arrive = false;
        }

        public boolean isArrive() {
            return arrive;
        }

        public boolean isActualArrive() {
            return actualArrive;
        }

        public FlowElement getFlowElement() {
            return flowElement;
        }

        public boolean elementArrive(FlowElement flowElement, boolean actualArrive) {
            if (flowElement != this.flowElement) {
                return false;
            }
            if (this.arrive) {
                return false;
            }
            this.arrive = true;
            this.actualArrive = actualArrive;
            return true;
        }
    }
}
