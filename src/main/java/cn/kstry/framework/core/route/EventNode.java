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
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.LocateBehavior;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lykan
 */
public class EventNode {

    /**
     * 事件执行态存在的节点 与 EventNode 一一对应，TaskNode 是 EventNode 在执行期间的存在形式
     */
    private final TaskNode taskNode;

    /**
     * 当前节点中保存的接下来，可以允许被执行的 EventNode
     */
    private final List<EventNode> nextEventNodeList = new ArrayList<>();

    /**
     * 前一个 EventNode
     */
    private EventNode prevEventNode;

    private RequestMappingGroup requestMappingGroup;

    private List<StrategyRule> filterStrategyRuleList;

    private List<StrategyRule> matchStrategyRuleList;

    private int nodeLevel;

    public EventNode(TaskNode taskNode) {
        AssertUtil.notNull(taskNode);
        this.taskNode = taskNode;
        AssertUtil.isNull(taskNode.getEventNode());
        taskNode.setEventNode(this);
    }

    public void addNextEventNode(EventNode eventNode) {
        AssertUtil.notNull(eventNode);
        if (nextEventNodeList.contains(eventNode)) {
            return;
        }
        nextEventNodeList.add(eventNode);
        eventNode.setPrevEventNode(this);
    }

    public Optional<EventNode> locateNextEventNode(LocateBehavior<TaskNode> locateBehavior) {
        if (CollectionUtils.isEmpty(nextEventNodeList)) {
            return Optional.empty();
        }

        if (locateBehavior == null) {
            return Optional.ofNullable(nextEventNodeList.get(0));
        }

        List<EventNode> resultList = nextEventNodeList.stream().filter(map -> locateBehavior.locate(map.getTaskNode())).collect(Collectors.toList());
        AssertUtil.oneSize(resultList, ExceptionEnum.INVALID_POSITIONING_BEHAVIOR);
        return Optional.of(resultList.get(0));
    }

    public Optional<EventNode> locateNextEventNode() {
        return this.locateNextEventNode(null);
    }

    public TaskNode getTaskNode() {
        return taskNode;
    }

    public EventNode getPrevEventNode() {
        return prevEventNode;
    }

    public List<EventNode> getNextEventNodeList() {
        return nextEventNodeList;
    }

    private void setPrevEventNode(EventNode prevEventNode) {
        this.prevEventNode = prevEventNode;
    }

    public int nextEventNodeSize() {
        if (CollectionUtils.isEmpty(this.nextEventNodeList)) {
            return 0;
        }
        return this.nextEventNodeList.size();
    }

    public RequestMappingGroup getRequestMappingGroup() {
        return requestMappingGroup;
    }

    public void setRequestMappingGroup(RequestMappingGroup requestMappingGroup) {
        this.requestMappingGroup = requestMappingGroup;
    }

    public List<StrategyRule> getFilterStrategyRuleList() {
        return filterStrategyRuleList;
    }

    public void setFilterStrategyRuleList(List<StrategyRule> filterStrategyRuleList) {
        this.filterStrategyRuleList = filterStrategyRuleList;
    }

    public int getNodeLevel() {
        return nodeLevel;
    }

    public void setNodeLevel(int nodeLevel) {
        this.nodeLevel = nodeLevel;
    }

    public List<StrategyRule> getMatchStrategyRuleList() {
        return matchStrategyRuleList;
    }

    public void setMatchStrategyRuleList(List<StrategyRule> matchStrategyRuleList) {
        this.matchStrategyRuleList = matchStrategyRuleList;
    }
}
