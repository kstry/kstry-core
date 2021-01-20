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
    private final LinkedList<TaskNode> toInvokeTaskNodeList = new LinkedList<>();

    /**
     * 已执行的调用链
     */
    private final LinkedList<TaskNode> alreadyInvokeTaskNodeList = new LinkedList<>();

    /**
     * 单次请求 router 关联 globalBus 数据
     */
    private GlobalBus globalBus;

    public TaskRouter(GlobalMap globalMap, String actionName) {
        EventNode eventNode = globalMap.locateFirstEventNode(actionName);
        toInvokeTaskNodeList.offerFirst(eventNode.getTaskNode());

        for (Optional<EventNode> eventNodeOptional = eventNode.locateNextEventNode(); eventNodeOptional.isPresent(); eventNodeOptional = eventNodeOptional.get().locateNextEventNode()) {
            toInvokeTaskNodeList.offer(eventNodeOptional.get().getTaskNode());
        }
    }

    public void reRouteNodeMap(LocateBehavior<TaskNode> locateBehavior) {
        AssertUtil.notNull(locateBehavior);

        LinkedList<TaskNode> reToInvokeTaskNodeList = new LinkedList<>();
        EventNode eventNode = GlobalUtil.notEmpty(currentRouteNode()).getEventNode();

        Optional<EventNode> nextEventNodeOptional = eventNode.locateNextEventNode(locateBehavior);
        if (!nextEventNodeOptional.isPresent()) {
            return;
        }
        EventNode nextNode = nextEventNodeOptional.get();
        reToInvokeTaskNodeList.offerFirst(nextNode.getTaskNode());

        for (Optional<EventNode> eventNodeOptional = nextNode.locateNextEventNode(); eventNodeOptional.isPresent(); eventNodeOptional = eventNodeOptional.get().locateNextEventNode()) {
            reToInvokeTaskNodeList.offer(eventNodeOptional.get().getTaskNode());
        }

        toInvokeTaskNodeList.clear();
        toInvokeTaskNodeList.addAll(reToInvokeTaskNodeList);
    }

    public Optional<TaskNode> invokeRouteNode() {
        if (CollectionUtils.isEmpty(toInvokeTaskNodeList)) {
            return Optional.empty();
        }
        TaskNode node = toInvokeTaskNodeList.pollFirst();
        alreadyInvokeTaskNodeList.offerLast(node);
        return Optional.ofNullable(node);
    }

    public Optional<TaskNode> skipRouteNode() {
        if (CollectionUtils.isEmpty(toInvokeTaskNodeList)) {
            return Optional.empty();
        }
        TaskNode node = toInvokeTaskNodeList.pollFirst();
        return Optional.ofNullable(node);
    }

    public Optional<TaskNode> currentRouteNode() {
        return lastInvokeRouteNode();
    }

    public Optional<TaskNode> lastInvokeRouteNode() {
        if (CollectionUtils.isEmpty(alreadyInvokeTaskNodeList)) {
            return Optional.empty();
        }
        return Optional.ofNullable(alreadyInvokeTaskNodeList.peekLast());
    }

    public Optional<TaskNode> nextRouteNode() {
        if (CollectionUtils.isEmpty(toInvokeTaskNodeList)) {
            return Optional.empty();
        }
        return Optional.ofNullable(toInvokeTaskNodeList.peekFirst());
    }

    public Optional<TaskNode> next2RouteNode() {
        if (CollectionUtils.isEmpty(toInvokeTaskNodeList) || toInvokeTaskNodeList.size() < 2) {
            return Optional.empty();
        }
        return Optional.ofNullable(toInvokeTaskNodeList.get(1));
    }

    public Optional<TaskNode> nextRouteNodeIgnoreTimeSlot() {
        Optional<TaskNode> nextRouteNodeOptional = this.nextRouteNode();
        if (nextRouteNodeOptional.isPresent() && nextRouteNodeOptional.get().getEventGroupTypeEnum() == ComponentTypeEnum.TIME_SLOT) {
            nextRouteNodeOptional = this.next2RouteNode();
        }
        nextRouteNodeOptional.ifPresent(routeNode -> AssertUtil.isTrue(routeNode.getEventGroupTypeEnum() != ComponentTypeEnum.TIME_SLOT,
                ExceptionEnum.TIME_SLOT_SUPERIMPOSED_EXECUTED));
        return nextRouteNodeOptional;
    }

    public Optional<TaskNode> beforeInvokeRouteNode() {
        if (CollectionUtils.isEmpty(alreadyInvokeTaskNodeList) || alreadyInvokeTaskNodeList.size() <= 1) {
            return Optional.empty();
        }
        return Optional.ofNullable(alreadyInvokeTaskNodeList.get(alreadyInvokeTaskNodeList.size() - 2));
    }

    public Optional<TaskNode> before2InvokeRouteNode() {
        if (CollectionUtils.isEmpty(alreadyInvokeTaskNodeList) || alreadyInvokeTaskNodeList.size() <= 2) {
            return Optional.empty();
        }
        return Optional.ofNullable(alreadyInvokeTaskNodeList.get(alreadyInvokeTaskNodeList.size() - 3));
    }

    public Optional<TaskNode> beforeInvokeRouteNodeIgnoreTimeSlot() {
        Optional<TaskNode> beforeInvokeRouteNodeOptional = this.beforeInvokeRouteNode();
        if (beforeInvokeRouteNodeOptional.isPresent() && beforeInvokeRouteNodeOptional.get().getEventGroupTypeEnum() == ComponentTypeEnum.TIME_SLOT) {
            beforeInvokeRouteNodeOptional = this.before2InvokeRouteNode();
        }
        beforeInvokeRouteNodeOptional.ifPresent(routeNode -> AssertUtil.isTrue(routeNode.getEventGroupTypeEnum() != ComponentTypeEnum.TIME_SLOT,
                ExceptionEnum.TIME_SLOT_SUPERIMPOSED_EXECUTED));
        return beforeInvokeRouteNodeOptional;
    }

    public GlobalBus getGlobalBus() {
        return globalBus;
    }

    public void setGlobalBus(GlobalBus globalBus) {
        AssertUtil.notNull(globalBus);
        this.globalBus = globalBus;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(currentRouteNode().map(TaskNode::getEventGroupName).orElse("INIT"));
    }

}
