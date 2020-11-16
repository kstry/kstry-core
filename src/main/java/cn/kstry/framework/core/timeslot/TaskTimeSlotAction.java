/*
 *
 *  *  Copyright (c) 2020-2020, Lykan (jiashuomeng@gmail.com).
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

import cn.kstry.framework.core.engine.TaskAction;
import cn.kstry.framework.core.facade.TaskResponse;

/**
 * 时间片执行器
 *
 * @author lykan
 */
public interface TaskTimeSlotAction extends TaskAction {

    /**
     * time slot task name
     */
    String KSTRY_TIME_SLOT_TASK_NAME = "KSTRY_TIME_SLOT";

    /**
     * 时间片执行入口
     *
     * @param request 第一个task 的 request
     * @return 最后一个 task的返回结果
     */
    TaskResponse<?> invoke(TimeSlotInvokeRequest request);

}
