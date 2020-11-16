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
package cn.kstry.framework.core.route;

import cn.kstry.framework.core.bus.GlobalBus;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.LocateBehavior;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;

import java.util.LinkedList;
import java.util.Optional;

public class TaskRouter {

    /**
     * 调用链
     */
    private final LinkedList<RouteNode> toInvokeRouteNodeList = new LinkedList<>();

    /**
     * 已执行的调用链
     */
    private final LinkedList<RouteNode> alreadyInvokeRouteNodeList = new LinkedList<>();

    /**
     * 单次请求 router 关联 globalBus 数据
     */
    private GlobalBus globalBus;

    public TaskRouter(GlobalMap globalMap, String actionName) {
        GlobalMap.MapNode mapNode = globalMap.locateFirstMapNode(actionName);
        toInvokeRouteNodeList.offerFirst(mapNode.getRouteNode());

        for (Optional<GlobalMap.MapNode> mapNodeOptional = mapNode.locateNextMapNode(); mapNodeOptional.isPresent(); mapNodeOptional = mapNodeOptional.get().locateNextMapNode()) {
            toInvokeRouteNodeList.offer(mapNodeOptional.get().getRouteNode());
        }
    }

    public void reRouteNodeMap(LocateBehavior<RouteNode> locateBehavior) {
        AssertUtil.notNull(locateBehavior);

        LinkedList<RouteNode> reToInvokeRouteNodeList = new LinkedList<>();
        GlobalMap.MapNode mapNode = GlobalUtil.notEmpty(currentRouteNode()).getMapNode();

        Optional<GlobalMap.MapNode> nextMapNodeOptional = mapNode.locateNextMapNode(locateBehavior);
        if (!nextMapNodeOptional.isPresent()) {
            return;
        }
        GlobalMap.MapNode nextNode = nextMapNodeOptional.get();
        reToInvokeRouteNodeList.offerFirst(nextNode.getRouteNode());

        for (Optional<GlobalMap.MapNode> mapNodeOptional = nextNode.locateNextMapNode(); mapNodeOptional.isPresent(); mapNodeOptional = mapNodeOptional.get().locateNextMapNode()) {
            reToInvokeRouteNodeList.offer(mapNodeOptional.get().getRouteNode());
        }

        toInvokeRouteNodeList.clear();
        toInvokeRouteNodeList.addAll(reToInvokeRouteNodeList);
    }

    public Optional<RouteNode> invokeRouteNode() {
        if (CollectionUtils.isEmpty(toInvokeRouteNodeList)) {
            return Optional.empty();
        }
        RouteNode node = toInvokeRouteNodeList.pollFirst();
        alreadyInvokeRouteNodeList.offerLast(node);
        return Optional.ofNullable(node);
    }

    public Optional<RouteNode> currentRouteNode() {
        return lastInvokeRouteNode();
    }

    public Optional<RouteNode> lastInvokeRouteNode() {
        if (CollectionUtils.isEmpty(alreadyInvokeRouteNodeList)) {
            return Optional.empty();
        }
        return Optional.ofNullable(alreadyInvokeRouteNodeList.peekLast());
    }

    public Optional<RouteNode> nextRouteNode() {
        if (CollectionUtils.isEmpty(toInvokeRouteNodeList)) {
            return Optional.empty();
        }
        return Optional.ofNullable(toInvokeRouteNodeList.peekFirst());
    }

    public Optional<RouteNode> next2RouteNode() {
        if (CollectionUtils.isEmpty(toInvokeRouteNodeList) || toInvokeRouteNodeList.size() < 2) {
            return Optional.empty();
        }
        return Optional.ofNullable(toInvokeRouteNodeList.get(1));
    }

    public Optional<RouteNode> nextRouteNodeIgnoreTimeSlot() {
        Optional<RouteNode> nextRouteNodeOptional = this.nextRouteNode();
        if (nextRouteNodeOptional.isPresent() && nextRouteNodeOptional.get().getComponentTypeEnum() == ComponentTypeEnum.TIME_SLOT) {
            nextRouteNodeOptional = this.next2RouteNode();
        }
        nextRouteNodeOptional.ifPresent(routeNode -> AssertUtil.isTrue(routeNode.getComponentTypeEnum() != ComponentTypeEnum.TIME_SLOT,
                ExceptionEnum.TIME_SLOT_SUPERIMPOSED_EXECUTED));
        return nextRouteNodeOptional;
    }

    public Optional<RouteNode> beforeInvokeRouteNode() {
        if (CollectionUtils.isEmpty(alreadyInvokeRouteNodeList) || alreadyInvokeRouteNodeList.size() <= 1) {
            return Optional.empty();
        }
        return Optional.ofNullable(alreadyInvokeRouteNodeList.get(alreadyInvokeRouteNodeList.size() - 2));
    }

    public Optional<RouteNode> before2InvokeRouteNode() {
        if (CollectionUtils.isEmpty(alreadyInvokeRouteNodeList) || alreadyInvokeRouteNodeList.size() <= 2) {
            return Optional.empty();
        }
        return Optional.ofNullable(alreadyInvokeRouteNodeList.get(alreadyInvokeRouteNodeList.size() - 3));
    }

    public Optional<RouteNode> beforeInvokeRouteNodeIgnoreTimeSlot() {
        Optional<RouteNode> beforeInvokeRouteNodeOptional = this.beforeInvokeRouteNode();
        if (beforeInvokeRouteNodeOptional.isPresent() && beforeInvokeRouteNodeOptional.get().getComponentTypeEnum() == ComponentTypeEnum.TIME_SLOT) {
            beforeInvokeRouteNodeOptional = this.before2InvokeRouteNode();
        }
        beforeInvokeRouteNodeOptional.ifPresent(routeNode -> AssertUtil.isTrue(routeNode.getComponentTypeEnum() != ComponentTypeEnum.TIME_SLOT,
                ExceptionEnum.TIME_SLOT_SUPERIMPOSED_EXECUTED));
        return beforeInvokeRouteNodeOptional;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(currentRouteNode().map(RouteNode::getNodeName).orElse("INIT"));
    }

    public GlobalBus getGlobalBus() {
        return globalBus;
    }

    public void setGlobalBus(GlobalBus globalBus) {
        AssertUtil.notNull(globalBus);
        this.globalBus = globalBus;
    }
}
