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

import cn.kstry.framework.core.bus.BusDataBox;
import cn.kstry.framework.core.bus.DefaultDataBox;
import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.bus.TaskNode;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.facade.TaskResponseBox;
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.route.EventGroup;
import cn.kstry.framework.core.route.EventNode;
import cn.kstry.framework.core.route.GlobalMap;
import cn.kstry.framework.core.route.TaskRouter;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.TaskActionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Optional;

public class StoryEngine implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoryEngine.class);

    /**
     * 代码中定义的事件组列表，实际参与任务执行
     */
    private List<EventGroup> taskGroup;

    /**
     * 全局地图
     */
    private GlobalMap globalMap;

    /**
     * story fire!
     *
     * @param request           request
     * @param storyName         指令
     * @param stableDataBox     不可变数据集承载对象
     * @param variableDataBox   可变数据集承载对象
     * @param resultClass       预期结果类型
     */
    @SuppressWarnings("unchecked")
    public <T> TaskResponse<T> fire(Object request, String storyName, BusDataBox stableDataBox, BusDataBox variableDataBox, Class<T> resultClass) {
        StoryBus storyBus = new StoryBus(request, stableDataBox, variableDataBox);
        EventNode firstEventNode = this.globalMap.locateFirstEventNode(storyBus, storyName);
        TaskRouter taskRouter = new TaskRouter(firstEventNode, storyBus);
        try {
            for (Optional<TaskNode> nodeOptional = taskRouter.invokeTaskNode(); nodeOptional.isPresent(); nodeOptional = taskRouter.invokeTaskNode()) {

                TaskNode taskNode = nodeOptional.get();
                EventOperatorRole actionOperator = TaskActionUtil.getTaskActionOperator(taskRouter, this.taskGroup);
                Object taskRequest = TaskActionUtil.getNextRequest(taskRouter, storyBus, taskGroup);
                Object o = TaskActionUtil.invokeTarget(taskRequest, taskNode, actionOperator);

                if (o instanceof TaskResponse && !((TaskResponse<?>) o).isSuccess()) {
                    storyBus.saveTimeSlotTaskResult(o);
                    LOGGER.debug("story engine execution result unsuccessful! result:{}", o);
                    return (TaskResponse<T>) o;
                }
                storyBus.saveTaskResult(GlobalUtil.notEmpty(taskRouter.currentTaskNode()), o);
                taskRouter.locateNextTaskNode();
            }

            Object result = storyBus.getResultByTaskNode(GlobalUtil.notEmpty(taskRouter.lastInvokeTaskNode()));
            if (result == null) {
                return TaskResponseBox.buildSuccess(null);
            }
            AssertUtil.isTrue(resultClass.isAssignableFrom(result.getClass()), ExceptionEnum.RESPONSE_ERROR,
                    "The result type of the final execution does not match the expected type! real result type:%s", result.getClass());
            return (TaskResponse<T>) TaskResponseBox.buildSuccess(result);
        } catch (Exception e) {
            LOGGER.warn("story engine execution exception occurred!", e);
            return (TaskResponse<T>) TaskActionUtil.getTaskResponseFromException(e, ExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * story fire!
     *
     * @param request           request
     * @param storyName         指令
     * @param resultClass       预期结果类型
     */
    public <T> TaskResponse<T> fire(Object request, String storyName, Class<T> resultClass) {
        return fire(request, storyName, new DefaultDataBox(), resultClass);
    }

    /**
     * story fire!
     *
     * @param request           request
     * @param storyName         指令(story Name)
     * @param stableDataBox     不可变数据集承载对象
     * @param resultClass       预期结果类型
     */
    public <T> TaskResponse<T> fire(Object request, String storyName, BusDataBox stableDataBox, Class<T> resultClass) {
        return fire(request, storyName, stableDataBox, new DefaultDataBox(), resultClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        GlobalMap globalMap = applicationContext.getBean(GlobalMap.class);
        AssertUtil.notNull(globalMap);
        this.globalMap = globalMap;

        Object eventGroupListBean = applicationContext.getBean("defaultEventGroupList");
        AssertUtil.notNull(eventGroupListBean);
        this.taskGroup = (List<EventGroup>) eventGroupListBean;
    }
}