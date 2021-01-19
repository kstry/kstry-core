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
import cn.kstry.framework.core.bus.GlobalBus;
import cn.kstry.framework.core.engine.EventGroup;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.operator.TaskActionOperatorRole;
import cn.kstry.framework.core.route.TaskNode;
import cn.kstry.framework.core.route.RouteEventGroup;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.TaskActionUtil;
import org.apache.commons.lang3.BooleanUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author lykan
 */
public class TimeSlotComponent extends RouteEventGroup implements TimeSlotOperatorRole {

    @Override
    @SuppressWarnings("unchecked")
    public TaskResponse<?> invoke(TimeSlotInvokeRequest request) {

        GlobalBus globalBus = request.getGlobalBus();
        TaskRouter router = globalBus.getRouter();
        List<EventGroup> taskGroup = request.getTaskGroup();
        ResultMappingRepository resultMappingRepository = request.getResultMappingRepository();

        Object taskRequest = getFirstRequest(globalBus, router);
        try {
            for (Optional<TaskNode> nodeOptional = router.invokeRouteNode(); nodeOptional.isPresent(); nodeOptional = router.invokeRouteNode()) {
                TaskNode taskNode = nodeOptional.get();

                // 不支持 slot 里嵌套 slot 的情况
                AssertUtil.isTrue(taskNode.getEventGroupTypeEnum() != ComponentTypeEnum.TIME_SLOT, ExceptionEnum.TIME_SLOT_SUPERIMPOSED_EXECUTED);

                TaskActionOperatorRole actionOperator = TaskActionUtil.getTaskActionOperator(router, taskGroup);
                taskRequest = TaskActionUtil.getNextRequest(taskRequest, router, resultMappingRepository, globalBus, taskGroup);

                Object o = TaskActionUtil.invokeTarget(taskRequest, taskNode, actionOperator);
                if (o instanceof TaskResponse && !((TaskResponse<?>) o).isSuccess()) {
                    return (TaskResponse<?>) o;
                }
                TaskActionUtil.saveTaskResultToGlobalBus(globalBus, taskNode, (TaskResponse<Object>) o);
                TaskActionUtil.reRouteNodeMap(router);
                taskRequest = o;

                if (BooleanUtils.isTrue(taskNode.getInterruptTimeSlot())) {
                    break;
                }
            }
            return GlobalUtil.transfer(taskRequest, TaskResponse.class).orElse(null);
        } catch (Exception e) {
            TaskActionUtil.throwException(e);
            return null;
        }
    }

    /**
     * 获取 TimeSlot 中首个 Task 执行的参数，从 StoryEngine 中传入
     */
    private Object getFirstRequest(GlobalBus globalBus, TaskRouter router) {
        Optional<TaskNode> beforeInvokeRouteNodeOptional = router.beforeInvokeRouteNode();
        if (!beforeInvokeRouteNodeOptional.isPresent()) {
            return null;
        }

        TaskNode beforeInvokeTaskNode = beforeInvokeRouteNodeOptional.get();
        if (beforeInvokeTaskNode.getEventGroupTypeEnum() != ComponentTypeEnum.MAPPING) {
            return globalBus.getResultByRouteNode(beforeInvokeTaskNode);
        }
        return null;
    }

    @Override
    public String getEventGroupName() {
        return KSTRY_TIME_SLOT_TASK_NAME;
    }

    @Override
    public ComponentTypeEnum getEventGroupTypeEnum() {
        return ComponentTypeEnum.TIME_SLOT;
    }

    @Override
    public Class<? extends TaskActionOperatorRole> getTaskActionOperatorRoleClass() {
        return TimeSlotOperatorRole.class;
    }
}
