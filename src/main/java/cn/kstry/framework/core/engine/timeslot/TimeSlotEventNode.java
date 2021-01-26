/*
 *
 *  *  Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  *  <p>
 *  *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *  <p>
 *  * https://www.gnu.org/licenses/lgpl.html
 *  *  <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.engine.timeslot;

import cn.kstry.framework.core.bus.TaskNode;
import cn.kstry.framework.core.route.EventNode;

import java.util.List;

/**
 * 时间段（分支） 事件 Node
 *
 * @author lykan
 */
public class TimeSlotEventNode extends EventNode {

    private List<EventNode> firstTimeSlotEventNodeList;

    private String strategyName;

    /**
     * 异步任务的超时时间，单位 ms
     * 默认值：cn.kstry.framework.core.config.GlobalConstant#DEFAULT_ASYNC_TIMEOUT
     */
    private int timeout;

    /**
     * true: 代表使用异步任务
     */
    private boolean async;

    /**
     * 被 TimeSlotEventNode 包装前的 原始的 TaskNode
     */
    private TaskNode originalTaskNode;

    public TimeSlotEventNode(TaskNode taskNode) {
        super(taskNode);
    }

    public List<EventNode> getFirstTimeSlotEventNodeList() {
        return firstTimeSlotEventNodeList;
    }

    public void setFirstTimeSlotEventNodeList(List<EventNode> firstTimeSlotEventNodeList) {
        this.firstTimeSlotEventNodeList = firstTimeSlotEventNodeList;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public TaskNode getOriginalTaskNode() {
        return originalTaskNode;
    }

    public void setOriginalTaskNode(TaskNode originalTaskNode) {
        this.originalTaskNode = originalTaskNode;
    }
}
