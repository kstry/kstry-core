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

import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.bus.TaskNode;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.facade.TaskResponseBox;
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.route.EventGroup;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.TaskActionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * TimeSlot 异步任务
 *
 * @author lykan
 */
public class TimeSlotAsyncTask implements Callable<TaskResponse<Map<String, Object>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSlotAsyncTask.class);

    private final TimeSlotInvokeRequest timeSlotInvokeRequest;

    public TimeSlotAsyncTask(TimeSlotInvokeRequest timeSlotInvokeRequest) {
        AssertUtil.notNull(timeSlotInvokeRequest);
        this.timeSlotInvokeRequest = timeSlotInvokeRequest;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TaskResponse<Map<String, Object>> call() {

        try {
            StoryBus storyBus = timeSlotInvokeRequest.getStoryBus();
            TaskRouter taskRouter = storyBus.getRouter();
            List<EventGroup> taskGroup = timeSlotInvokeRequest.getTaskGroup();

            for (Optional<TaskNode> nodeOptional = taskRouter.invokeTaskNode(); nodeOptional.isPresent(); nodeOptional = taskRouter.invokeTaskNode()) {

                EventOperatorRole actionOperator = TaskActionUtil.getTaskActionOperator(taskRouter, taskGroup);
                Object taskRequest = TaskActionUtil.getNextRequest(taskRouter, storyBus, taskGroup);
                Object o = TaskActionUtil.invokeTarget(taskRequest, nodeOptional.get(), actionOperator);

                if (o instanceof TaskResponse && !((TaskResponse<?>) o).isSuccess()) {
                    storyBus.saveTimeSlotTaskResult(o);
                    return (TaskResponse<Map<String, Object>>) o;
                }
                storyBus.saveTaskResult(GlobalUtil.notEmpty(taskRouter.currentTaskNode()), o);
            }

            Map<String, Object> resultData = new HashMap<>();
            storyBus.loadResultData(resultData);
            return TaskResponseBox.buildSuccess(resultData);
        } catch (Exception e) {
            LOGGER.warn("[{}] time slot execution Failure! strategy name:[{}]",
                    ExceptionEnum.TIME_SLOT_EXECUTION_ERROR.getExceptionCode(), timeSlotInvokeRequest.getStrategyName(), e);
            Throwable throwable = GlobalUtil.notNull(TaskActionUtil.splitException(e));
            TaskResponse<Map<String, Object>> taskResponse;
            if (throwable instanceof KstryException) {
                KstryException kstryException = (KstryException) throwable;
                taskResponse = TaskResponseBox.buildError(kstryException.getErrorCode(), kstryException.getMessage());
            } else {
                taskResponse = TaskResponseBox.buildError(ExceptionEnum.TIME_SLOT_SYSTEM_ERROR.getExceptionCode(), throwable.getMessage());
            }
            taskResponse.setResultException(throwable);
            return taskResponse;
        }
    }

    public TimeSlotInvokeRequest getTimeSlotInvokeRequest() {
        return timeSlotInvokeRequest;
    }
}
