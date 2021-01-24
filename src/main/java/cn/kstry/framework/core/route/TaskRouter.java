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

import cn.kstry.framework.core.bus.StoryBus;
import cn.kstry.framework.core.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;

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
    private final StoryBus storyBus;

    public TaskRouter(EventNode firstEventNode, StoryBus storyBus) {
        AssertUtil.notNull(storyBus);
        AssertUtil.notNull(firstEventNode);
        this.storyBus = storyBus;
        toInvokeTaskNodeList.offerFirst(firstEventNode.getTaskNode());

        for (Optional<EventNode> eventNodeOptional = firstEventNode.locateNextEventNode(); eventNodeOptional.isPresent(); eventNodeOptional =
                eventNodeOptional.get().locateNextEventNode()) {
            toInvokeTaskNodeList.offer(eventNodeOptional.get().getTaskNode());
        }
        storyBus.setRouter(this);
    }

    public void reTaskNodeMap(TaskNode nextNode) {
        AssertUtil.notNull(nextNode);

        LinkedList<TaskNode> reToInvokeTaskNodeList = new LinkedList<>();
        reToInvokeTaskNodeList.offerFirst(nextNode);
        for (Optional<EventNode> eventNodeOptional = nextNode.getEventNode().locateNextEventNode(); eventNodeOptional.isPresent();
             eventNodeOptional = eventNodeOptional.get().locateNextEventNode()) {
            reToInvokeTaskNodeList.offer(eventNodeOptional.get().getTaskNode());
        }
        toInvokeTaskNodeList.clear();
        toInvokeTaskNodeList.addAll(reToInvokeTaskNodeList);
    }

    public Optional<TaskNode> invokeTaskNode() {
        if (CollectionUtils.isEmpty(toInvokeTaskNodeList)) {
            return Optional.empty();
        }
        TaskNode node = toInvokeTaskNodeList.pollFirst();
        alreadyInvokeTaskNodeList.offerLast(node);
        return Optional.ofNullable(node);
    }

    public Optional<TaskNode> currentTaskNode() {
        return lastInvokeTaskNode();
    }

    public Optional<TaskNode> lastInvokeTaskNode() {
        if (CollectionUtils.isEmpty(alreadyInvokeTaskNodeList)) {
            return Optional.empty();
        }
        return Optional.ofNullable(alreadyInvokeTaskNodeList.peekLast());
    }

    public Optional<TaskNode> nextTaskNode() {
        if (CollectionUtils.isEmpty(toInvokeTaskNodeList)) {
            return Optional.empty();
        }
        return Optional.ofNullable(toInvokeTaskNodeList.peekFirst());
    }

    public StoryBus getStoryBus() {
        return storyBus;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(currentTaskNode().map(TaskNode::getEventGroupName).orElse("INIT"));
    }

}
