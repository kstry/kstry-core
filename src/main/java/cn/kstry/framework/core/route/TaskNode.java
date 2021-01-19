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
package cn.kstry.framework.core.route;

import cn.kstry.framework.core.adapter.RequestMappingGroup;
import cn.kstry.framework.core.engine.TaskActionMethod;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.facade.TaskRequest;
import cn.kstry.framework.core.util.AssertUtil;
import org.apache.commons.lang3.StringUtils;
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
    private ComponentTypeEnum eventGroupTypeEnum;

    /**
     * 节点执行，目标方法属性
     */
    private TaskActionMethod taskActionMethod;

    /**
     * 请求入参 解析映射表
     */
    private RequestMappingGroup requestMappingGroup;

    /**
     * 全局地图的一个节点
     */
    private EventNode mapNode;

    /**
     * 拐点匹配列表。 定义表达式、预期值、匹配字段
     */
    private List<TaskRouterInflectionPoint> inflectionPointList;

    /**
     * 匹配失败，跳过该节点
     */
    private List<TaskRouterInflectionPoint> filterInflectionPointList;

    /**
     * 中断 timeSlot
     */
    private Boolean interruptTimeSlot;

    public TaskNode() {
    }

    public TaskNode(String actionName, String eventGroupName, ComponentTypeEnum eventGroupTypeEnum) {
        this.actionName = actionName;
        this.eventGroupName = eventGroupName;
        this.eventGroupTypeEnum = eventGroupTypeEnum;
    }

    public String identity() {
        return this.eventGroupTypeEnum.name() + "-" + this.eventGroupName + "-" + this.actionName;
    }

    public String identityStr() {
        return identity().replace("-", StringUtils.EMPTY).replace("_", StringUtils.EMPTY);
    }

    public TaskNode cloneRouteNode() {
        TaskNode taskNode = new TaskNode();
        BeanUtils.copyProperties(this, taskNode, "mapNode", "inflectionPointList", "filterInflectionPointList", "interruptTimeSlot", "requestMappingGroup");
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

    public ComponentTypeEnum getEventGroupTypeEnum() {
        return eventGroupTypeEnum;
    }

    public void setEventGroupTypeEnum(ComponentTypeEnum eventGroupTypeEnum) {
        AssertUtil.notNull(eventGroupTypeEnum);
        this.eventGroupTypeEnum = eventGroupTypeEnum;
    }

    public Class<? extends TaskRequest> getRequestClass() {
        if (taskActionMethod == null) {
            return null;
        }
        return taskActionMethod.getRequestClass();
    }

    public void setTaskActionMethod(TaskActionMethod taskActionMethod) {
        AssertUtil.notNull(taskActionMethod);
        this.taskActionMethod = taskActionMethod;
    }

    public TaskActionMethod getTaskActionMethod() {
        return taskActionMethod;
    }

    public EventNode getMapNode() {
        return mapNode;
    }

    public void setMapNode(EventNode mapNode) {
        AssertUtil.notNull(mapNode);
        this.mapNode = mapNode;
    }

    public List<TaskRouterInflectionPoint> getInflectionPointList() {
        return inflectionPointList;
    }

    public void setInflectionPointList(List<TaskRouterInflectionPoint> inflectionPointList) {
        this.inflectionPointList = inflectionPointList;
    }

    public Boolean getInterruptTimeSlot() {
        return interruptTimeSlot;
    }

    public void setInterruptTimeSlot(Boolean interruptTimeSlot) {
        this.interruptTimeSlot = interruptTimeSlot;
    }

    public RequestMappingGroup getRequestMappingGroup() {
        return requestMappingGroup;
    }

    public void setRequestMappingGroup(RequestMappingGroup requestMappingGroup) {
        this.requestMappingGroup = requestMappingGroup;
    }

    public List<TaskRouterInflectionPoint> getFilterInflectionPointList() {
        return filterInflectionPointList;
    }

    public void setFilterInflectionPointList(List<TaskRouterInflectionPoint> filterInflectionPointList) {
        this.filterInflectionPointList = filterInflectionPointList;
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
