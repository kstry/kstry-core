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
public class RouteNode {

    /**
     * task方法名
     */
    private String actionName;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 组件的类型
     */
    private ComponentTypeEnum componentTypeEnum;

    /**
     * 节点执行，请求类型
     */
    private Class<? extends TaskRequest> requestClass;

    /**
     * 全局地图的一个节点
     */
    private GlobalMap.MapNode mapNode;

    /**
     * 拐点匹配列表。 定义表达式、预期值、匹配字段
     */
    private List<TaskRouterInflectionPoint> inflectionPointList;

    /**
     * 中断 timeSlot
     */
    private Boolean interruptTimeSlot;

    public RouteNode() {
    }

    public RouteNode(String actionName, String nodeName, ComponentTypeEnum componentTypeEnum) {
        this.actionName = actionName;
        this.nodeName = nodeName;
        this.componentTypeEnum = componentTypeEnum;
    }

    public String identity() {
        return this.componentTypeEnum.name() + "-" + this.nodeName + "-" + this.actionName;
    }

    public String identityStr() {
        return identity().replace("-", StringUtils.EMPTY).replace("_", StringUtils.EMPTY);
    }

    public RouteNode cloneRouteNode() {
        RouteNode routeNode = new RouteNode();
        BeanUtils.copyProperties(this, routeNode, "mapNode", "inflectionPointList", "interruptTimeSlot");
        return routeNode;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        AssertUtil.notBlank(actionName);
        this.actionName = actionName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        AssertUtil.notBlank(nodeName);
        this.nodeName = nodeName;
    }

    public ComponentTypeEnum getComponentTypeEnum() {
        return componentTypeEnum;
    }

    public void setComponentTypeEnum(ComponentTypeEnum componentTypeEnum) {
        AssertUtil.notNull(componentTypeEnum);
        this.componentTypeEnum = componentTypeEnum;
    }

    public Class<? extends TaskRequest> getRequestClass() {
        return requestClass;
    }

    public void setRequestClass(Class<? extends TaskRequest> requestClass) {
        this.requestClass = requestClass;
    }

    public GlobalMap.MapNode getMapNode() {
        return mapNode;
    }

    public void setMapNode(GlobalMap.MapNode mapNode) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteNode routeNode = (RouteNode) o;
        return Objects.equals(actionName, routeNode.actionName) &&
                Objects.equals(nodeName, routeNode.nodeName) &&
                componentTypeEnum == routeNode.componentTypeEnum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionName, nodeName, componentTypeEnum);
    }

    @Override
    public String toString() {
        return identity();
    }
}
