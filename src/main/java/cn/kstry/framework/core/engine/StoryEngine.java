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
package cn.kstry.framework.core.engine;

import cn.kstry.framework.core.bus.GlobalBus;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.facade.TaskResponseBox;
import cn.kstry.framework.core.operator.TaskActionOperatorRole;
import cn.kstry.framework.core.route.GlobalMap;
import cn.kstry.framework.core.route.RouteNode;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.TaskActionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Optional;

public class StoryEngine {

    private final List<TaskAction> taskGroup;

    private final GlobalMap globalMap;

    public StoryEngine(@Qualifier("defaultGlobalMap") GlobalMap globalMap,
                       @Qualifier("defaultTaskActionList") List<TaskAction> taskActionList) {
        this.globalMap = globalMap;
        this.taskGroup = taskActionList;
    }

    /**
     * 屏你的i
     *
     * @param request     request
     * @param actionName      指令
     */
    @SuppressWarnings("unchecked")
    public <T> TaskResponse<T> fire(Object request, String actionName, Class<T> resultClass) {
        TaskRouter taskRouter = new TaskRouter(globalMap, actionName);

        GlobalBus globalBus = new GlobalBus(taskRouter);
        globalBus.setDefaultParams(request);
        try {
            Object taskRequest = null;
            for (Optional<RouteNode> nodeOptional = taskRouter.invokeRouteNode(); nodeOptional.isPresent(); nodeOptional = taskRouter.invokeRouteNode()) {
                RouteNode routeNode = nodeOptional.get();

                TaskActionOperatorRole actionOperator = TaskActionUtil.getTaskActionOperator(taskRouter, taskGroup);

                taskRequest = TaskActionUtil.getNextRequest(taskRequest, taskRouter, globalMap.getResultMappingRepository(), globalBus, taskGroup);
                Object o = TaskActionUtil.invokeTarget(taskRequest, routeNode, actionOperator);
                if (o instanceof TaskResponse && !((TaskResponse<?>) o).isSuccess()) {
                    return (TaskResponse<T>) o;
                }

                ComponentTypeEnum componentEnum = routeNode.getComponentTypeEnum();
                if (componentEnum != ComponentTypeEnum.TIME_SLOT) {
                    TaskActionUtil.saveTaskResultToGlobalBus(globalBus, routeNode, (TaskResponse<Object>) o);
                }
                TaskActionUtil.reRouteNodeMap(taskRouter);

                taskRequest = o;
            }

            TaskResponse<?> resultResponse = globalBus.getResultByRouteNode(GlobalUtil.notEmpty(taskRouter.lastInvokeRouteNode()));
            if (resultResponse == null) {
                return null;
            }
            if (resultResponse.getResult() != null) {
                AssertUtil.isTrue(resultClass.isAssignableFrom(resultResponse.getResult().getClass()), ExceptionEnum.MUST_ONE_TASK_ACTION);
            }
            TaskResponse<T> response = new TaskResponseBox<>();
            BeanUtils.copyProperties(resultResponse, response);
            return response;
        } catch (Exception e) {
            Throwable exception = e;
            if (exception instanceof UndeclaredThrowableException) {
                exception = ((UndeclaredThrowableException) exception).getUndeclaredThrowable();
            }
            if (exception instanceof InvocationTargetException) {
                exception = ((InvocationTargetException) exception).getTargetException();
            }
            if (exception instanceof KstryException) {
                throw (KstryException) exception;
            }
            KstryException.throwException(exception);
            return null;
        }
    }
}