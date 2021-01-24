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

import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.engine.EventGroup;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.facade.TaskResponseBox;
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.route.RouteEventGroup;
import cn.kstry.framework.core.route.TaskNode;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.TaskActionUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author lykan
 */
public class TimeSlotEngine extends RouteEventGroup implements TimeSlotOperatorRole {

    @Override
    public TaskResponse<Map<String, Object>> invoke(TimeSlotInvokeRequest request) {

        StoryBus storyBus = request.getStoryBus();
        TaskRouter taskRouter = storyBus.getRouter();
        List<EventGroup> taskGroup = request.getTaskGroup();
        try {
            for (Optional<TaskNode> nodeOptional = taskRouter.invokeTaskNode(); nodeOptional.isPresent(); nodeOptional = taskRouter.invokeTaskNode()) {
                TaskNode taskNode = nodeOptional.get();
                if (TaskActionUtil.checkIfSkipCurrentNode(GlobalUtil.notEmpty(taskRouter.currentTaskNode()), storyBus)) {
                    continue;
                }
                EventOperatorRole actionOperator = TaskActionUtil.getTaskActionOperator(taskRouter, taskGroup);
                Object taskRequest = TaskActionUtil.getNextRequest(taskRouter, storyBus, taskGroup);
                Object o = TaskActionUtil.invokeTarget(taskRequest, taskNode, actionOperator);

                storyBus.saveTaskResult(GlobalUtil.notEmpty(taskRouter.currentTaskNode()), o);
                TaskActionUtil.reRouteNodeMap(taskRouter);
            }

            Map<String, Object> resultData = new HashMap<>();
            storyBus.loadResultData(resultData);
            return TaskResponseBox.buildSuccess(resultData);
        } catch (Exception e) {
            TaskActionUtil.throwException(e);
            return null;
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
