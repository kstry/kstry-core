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

import cn.kstry.framework.core.route.EventGroup;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.route.TaskRouter;

import java.util.List;

/**
 * time slot request
 *
 * @author lykan
 */
public class TimeSlotInvokeRequest {

    /**
     * 需要 time slot 执行的 task group
     */
    private List<EventGroup> taskGroup;

    /**
     * 全局 bus
     */
    private StoryBus storyBus;

    /**
     *
     */
    private TaskRouter taskRouter;

    /**
     * time slot 执行 story 的 策略名称
     */
    private String strategyName;

    public List<EventGroup> getTaskGroup() {
        return taskGroup;
    }

    public void setTaskGroup(List<EventGroup> taskGroup) {
        this.taskGroup = taskGroup;
    }

    public StoryBus getStoryBus() {
        return storyBus;
    }

    public void setStoryBus(StoryBus storyBus) {
        this.storyBus = storyBus;
    }

    public TaskRouter getTaskRouter() {
        return taskRouter;
    }

    public void setTaskRouter(TaskRouter taskRouter) {
        this.taskRouter = taskRouter;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }
}
