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

import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.route.RouteEventGroup;
import cn.kstry.framework.core.util.AssertUtil;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author lykan
 */
public class TimeSlotEngine extends RouteEventGroup implements TimeSlotOperatorRole, ApplicationListener {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public TaskResponse<Map<String, Object>> invoke(TimeSlotInvokeRequest request) {

        AssertUtil.notNull(request);
        TimeSlotAsyncTask timeSlotAsyncTask = new TimeSlotAsyncTask(request);
        if (!request.isAsync()) {
            return timeSlotAsyncTask.call();
        }

        Future<TaskResponse<Map<String, Object>>> submit = executorService.submit(timeSlotAsyncTask);
        try {
            TaskResponse<Map<String, Object>> mapTaskResponse = submit.get(request.getTimeout(), TimeUnit.MILLISECONDS);
            return mapTaskResponse;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextClosedEvent) {
            System.out.println("shutdown!");
            executorService.shutdown();
        }
    }

    @Override
    public String getEventGroupName() {
        return TIME_SLOT_TASK_NAME;
    }

    @Override
    public ComponentTypeEnum getEventGroupTypeEnum() {
        return ComponentTypeEnum.TIME_SLOT;
    }

    @Override
    public Class<? extends EventOperatorRole> getOperatorRoleClass() {
        return TimeSlotOperatorRole.class;
    }
}
