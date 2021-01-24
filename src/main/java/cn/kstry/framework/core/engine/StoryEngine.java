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

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.facade.TaskResponseBox;
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.route.EventNode;
import cn.kstry.framework.core.route.GlobalMap;
import cn.kstry.framework.core.route.TaskNode;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.TaskActionUtil;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Optional;

public class StoryEngine {

    private final List<EventGroup> taskGroup;

    private final GlobalMap globalMap;

    public StoryEngine(@Qualifier("defaultGlobalMap") GlobalMap globalMap,
                       @Qualifier("defaultEventGroupList") List<EventGroup> eventActionGroupList) {
        this.globalMap = globalMap;
        this.taskGroup = eventActionGroupList;
    }

    /**
     * story fire!
     *
     * @param request     request
     * @param storyName      指令
     */
    @SuppressWarnings("unchecked")
    public <T> TaskResponse<T> fire(Object request, String storyName, Class<T> resultClass) {

        StoryBus storyBus = new StoryBus(request);
        EventNode firstEventNode = this.globalMap.locateFirstEventNode(storyBus, storyName);
        TaskRouter taskRouter = new TaskRouter(firstEventNode, storyBus);
        try {
            for (Optional<TaskNode> nodeOptional = taskRouter.invokeTaskNode(); nodeOptional.isPresent(); nodeOptional = taskRouter.invokeTaskNode()) {

                TaskNode taskNode = nodeOptional.get();
                if (TaskActionUtil.checkIfSkipCurrentNode(GlobalUtil.notEmpty(taskRouter.currentTaskNode()), storyBus)) {
                    continue;
                }
                EventOperatorRole actionOperator = TaskActionUtil.getTaskActionOperator(taskRouter, this.taskGroup);
                Object taskRequest = TaskActionUtil.getNextRequest(taskRouter, storyBus, taskGroup);
                Object o = TaskActionUtil.invokeTarget(taskRequest, taskNode, actionOperator);

                if (o instanceof TaskResponse && !((TaskResponse<?>) o).isSuccess()) {
                    return (TaskResponse<T>) o;
                }

                storyBus.saveTaskResult(GlobalUtil.notEmpty(taskRouter.currentTaskNode()), o);
                TaskActionUtil.reRouteNodeMap(taskRouter);
            }

            Object result = storyBus.getResultByTaskNode(GlobalUtil.notEmpty(taskRouter.lastInvokeTaskNode()));
            if (result == null) {
                return null;
            }
            AssertUtil.isTrue(resultClass.isAssignableFrom(result.getClass()), ExceptionEnum.TYPE_TRANSFER_ERROR);
            return (TaskResponse<T>) TaskResponseBox.buildSuccess(result);
        } catch (Exception e) {
            TaskActionUtil.throwException(e);
            return null;
        }
    }
}