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

import cn.kstry.framework.core.adapter.ResultMappingRepository;
import cn.kstry.framework.core.bus.GlobalBus;
import cn.kstry.framework.core.engine.TaskAction;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.operator.TaskActionOperatorRole;
import cn.kstry.framework.core.route.RouteNode;
import cn.kstry.framework.core.route.RouteTaskAction;
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
public class TaskTimeSlot extends RouteTaskAction implements TaskTimeSlotAction {

    @Override
    @SuppressWarnings("unchecked")
    public TaskResponse<?> invoke(TimeSlotInvokeRequest request) {

        GlobalBus globalBus = request.getGlobalBus();
        TaskRouter router = globalBus.getRouter();
        List<TaskAction> taskGroup = request.getTaskGroup();
        ResultMappingRepository resultMappingRepository = request.getResultMappingRepository();

        Object taskRequest = getFirstRequest(globalBus, router);
        try {
            for (Optional<RouteNode> nodeOptional = router.invokeRouteNode(); nodeOptional.isPresent(); nodeOptional = router.invokeRouteNode()) {
                RouteNode routeNode = nodeOptional.get();

                // 不支持 slot 里嵌套 slot 的情况
                AssertUtil.isTrue(routeNode.getComponentTypeEnum() != ComponentTypeEnum.TIME_SLOT, ExceptionEnum.TIME_SLOT_SUPERIMPOSED_EXECUTED);

                TaskActionOperatorRole actionOperator = TaskActionUtil.getTaskActionOperator(router, taskGroup);
                taskRequest = TaskActionUtil.getNextRequest(taskRequest, router, resultMappingRepository, globalBus, taskGroup);

                Object o = TaskActionUtil.invokeTarget(taskRequest, routeNode, actionOperator);
                if (o instanceof TaskResponse && !((TaskResponse<?>) o).isSuccess()) {
                    return (TaskResponse<?>) o;
                }
                TaskActionUtil.saveTaskResultToGlobalBus(globalBus, routeNode, (TaskResponse<Object>) o);
                TaskActionUtil.reRouteNodeMap(router);
                taskRequest = o;

                if (BooleanUtils.isTrue(routeNode.getInterruptTimeSlot())) {
                    break;
                }
            }
            return GlobalUtil.transfer(taskRequest, TaskResponse.class).orElse(null);
        } catch (Exception e) {
            KstryException.throwException(e);
            return null;
        }
    }

    /**
     * 获取 TimeSlot 中首个 Task 执行的参数，从 StoryEngine 中传入
     */
    private Object getFirstRequest(GlobalBus globalBus, TaskRouter router) {
        Optional<RouteNode> beforeInvokeRouteNodeOptional = router.beforeInvokeRouteNode();
        if (!beforeInvokeRouteNodeOptional.isPresent()) {
            return null;
        }

        RouteNode beforeInvokeRouteNode = beforeInvokeRouteNodeOptional.get();
        if (beforeInvokeRouteNode.getComponentTypeEnum() != ComponentTypeEnum.MAPPING) {
            return globalBus.getResultByRouteNode(beforeInvokeRouteNode);
        }
        return null;
    }

    @Override
    public String getTaskActionName() {
        return KSTRY_TIME_SLOT_TASK_NAME;
    }

    @Override
    public ComponentTypeEnum getTaskActionTypeEnum() {
        return ComponentTypeEnum.TIME_SLOT;
    }

    @Override
    public Class<? extends TaskActionOperatorRole> getTaskActionOperatorRoleClass() {
        return TimeSlotOperatorRole.class;
    }
}
