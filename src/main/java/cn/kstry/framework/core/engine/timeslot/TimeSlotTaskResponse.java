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

import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.facade.TaskResponseBox;

import java.util.Map;
import java.util.concurrent.Future;

/**
 *
 * @author lykan
 */
public class TimeSlotTaskResponse extends TaskResponseBox<Map<String, Object>> implements TaskResponse<Map<String, Object>> {

    /**
     * 超时时间
     */
    private long timeout;

    /**
     * time slot 执行 story 的 策略名称
     */
    private String strategyName;

    /**
     * 异步任务执行结果
     */
    private final Future<TaskResponse<Map<String, Object>>> futureTask;

    public TimeSlotTaskResponse(Future<TaskResponse<Map<String, Object>>> futureTask) {
        this.futureTask = futureTask;
    }

    public Future<TaskResponse<Map<String, Object>>> getFutureTask() {
        return futureTask;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }
}