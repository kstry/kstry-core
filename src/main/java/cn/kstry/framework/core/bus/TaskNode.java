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
package cn.kstry.framework.core.bus;

import cn.kstry.framework.core.config.RequestMappingGroup;
import cn.kstry.framework.core.config.TaskActionMethod;
import cn.kstry.framework.core.enums.KstryTypeEnum;
import cn.kstry.framework.core.route.EventNode;
import cn.kstry.framework.core.route.StrategyRule;
import cn.kstry.framework.core.util.AssertUtil;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;

/**
 * 路由节点
 *
 * @author lykan
 */
public class TaskNode {

    /**
     * task方法名
     */
    private String actionName;

    /**
     * 节点名称
     */
    private String eventGroupName;

    /**
     * 组件的类型
     */
    private KstryTypeEnum eventGroupTypeEnum;

    /**
     * 节点执行，目标方法属性
     */
    private TaskActionMethod taskActionMethod;

    /**
     * 事件节点 与 TaskNode 一一对应，TaskNode 是 EventNode 在执行期间的存在形式
     */
    private EventNode eventNode;

    public TaskNode() {
    }

    public TaskNode(String actionName, String eventGroupName, KstryTypeEnum eventGroupTypeEnum) {
        this.actionName = actionName;
        this.eventGroupName = eventGroupName;
        this.eventGroupTypeEnum = eventGroupTypeEnum;
    }

    public String identity() {
        return this.eventGroupTypeEnum.name() + "-" + this.eventGroupName + "-" + this.actionName;
    }

    public TaskNode cloneTaskNode() {
        TaskNode taskNode = new TaskNode();
        BeanUtils.copyProperties(this, taskNode, "eventNode");
        return taskNode;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        AssertUtil.notBlank(actionName);
        this.actionName = actionName;
    }

    public String getEventGroupName() {
        return eventGroupName;
    }

    public void setEventGroupName(String eventGroupName) {
        AssertUtil.notBlank(eventGroupName);
        this.eventGroupName = eventGroupName;
    }

    public KstryTypeEnum getEventGroupTypeEnum() {
        return eventGroupTypeEnum;
    }

    public void setEventGroupTypeEnum(KstryTypeEnum eventGroupTypeEnum) {
        AssertUtil.notNull(eventGroupTypeEnum);
        this.eventGroupTypeEnum = eventGroupTypeEnum;
    }

    public TaskActionMethod getTaskActionMethod() {
        return taskActionMethod;
    }

    public void setTaskActionMethod(TaskActionMethod taskActionMethod) {
        AssertUtil.notNull(taskActionMethod);
        this.taskActionMethod = taskActionMethod;
    }

    public EventNode getEventNode() {
        return eventNode;
    }

    public void setEventNode(EventNode eventNode) {
        AssertUtil.notNull(eventNode);
        this.eventNode = eventNode;
    }

    public Class<?> getRequestClass() {
        if (taskActionMethod == null) {
            return null;
        }
        return taskActionMethod.getRequestClass();
    }

    public RequestMappingGroup getRequestMappingGroup() {
        if (this.eventNode == null) {
            return null;
        }
        return this.eventNode.getRequestMappingGroup();
    }

    public List<StrategyRule> getFilterStrategyRuleList() {
        if (this.eventNode == null) {
            return null;
        }
        return this.eventNode.getFilterStrategyRuleList();
    }

    public List<StrategyRule> getMatchStrategyRuleList() {
        if (this.eventNode == null) {
            return null;
        }
        return this.eventNode.getMatchStrategyRuleList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskNode taskNode = (TaskNode) o;
        return Objects.equals(actionName, taskNode.actionName) &&
                Objects.equals(eventGroupName, taskNode.eventGroupName) &&
                eventGroupTypeEnum == taskNode.eventGroupTypeEnum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionName, eventGroupName, eventGroupTypeEnum);
    }

    @Override
    public String toString() {
        return identity();
    }
}
