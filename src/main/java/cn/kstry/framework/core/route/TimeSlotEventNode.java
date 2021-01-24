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
package cn.kstry.framework.core.route;

import java.util.List;

/**
 * 时间段（分支） 事件 Node
 *
 * @author lykan
 */
public class TimeSlotEventNode extends EventNode {

    private List<EventNode> firstTimeSlotEventNodeList;

    private String strategyName;

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
}
