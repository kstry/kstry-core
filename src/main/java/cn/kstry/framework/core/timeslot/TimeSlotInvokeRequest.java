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
package cn.kstry.framework.core.timeslot;

import cn.kstry.framework.core.adapter.ResultMappingRepository;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.facade.TaskRequest;
import cn.kstry.framework.core.engine.EventGroup;

import java.util.List;

/**
 * time slot request
 *
 * @author lykan
 */
public class TimeSlotInvokeRequest implements TaskRequest {


    private static final long serialVersionUID = 6960705481098414756L;


    /**
     * 需要 time slot 执行的 task group
     */
    private List<EventGroup> taskGroup;

    /**
     * 全局 bus
     */
    private StoryBus storyBus;

    private ResultMappingRepository resultMappingRepository;

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

    public ResultMappingRepository getResultMappingRepository() {
        return resultMappingRepository;
    }

    public void setResultMappingRepository(ResultMappingRepository resultMappingRepository) {
        this.resultMappingRepository = resultMappingRepository;
    }
}
