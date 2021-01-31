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
import cn.kstry.framework.core.bus.TaskNode;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.TaskActionUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Optional;

public class TaskRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRouter.class);

    /**
     * 调用链
     */
    private TaskNode nextInvokeTaskNode;

    /**
     * 已执行的调用链
     */
    private final LinkedList<TaskNode> alreadyInvokeTaskNodeList = new LinkedList<>();

    /**
     * 单次请求 router 关联 globalBus 数据
     */
    private final StoryBus storyBus;

    public StoryBus getStoryBus() {
        return storyBus;
    }

    public TaskRouter(EventNode firstEventNode, StoryBus storyBus) {
        AssertUtil.notNull(storyBus);
        AssertUtil.notNull(firstEventNode);
        this.storyBus = storyBus;
        this.nextInvokeTaskNode = firstEventNode.getTaskNode();
        storyBus.setRouter(this);
    }

    public Optional<TaskNode> invokeTaskNode() {

        AssertUtil.notNull(getStoryBus());
        AssertUtil.notTrue(Thread.currentThread().isInterrupted(), ExceptionEnum.TASK_INTERRUPTED_ERROR);
        if (this.nextInvokeTaskNode == null) {
            return Optional.empty();
        }

        TaskNode node = this.nextInvokeTaskNode;
        this.alreadyInvokeTaskNodeList.offerLast(node);
        if (CollectionUtils.isNotEmpty(node.getEventNode().getNextEventNodeList())) {
            this.nextInvokeTaskNode = node.getEventNode().getNextEventNodeList().get(0).getTaskNode();
        } else {
            this.nextInvokeTaskNode = null;
        }

        LOGGER.debug("Current execution node:{}", node.identity());
        // 判断是否需要跳过当前节点
        if (needSkipCurrentNode(node, getStoryBus())) {
            locateNextTaskNode();
            return invokeTaskNode();
        }
        return currentTaskNode();
    }

    public Optional<TaskNode> currentTaskNode() {
        return lastInvokeTaskNode();
    }

    public Optional<TaskNode> lastInvokeTaskNode() {
        if (CollectionUtils.isEmpty(this.alreadyInvokeTaskNodeList)) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.alreadyInvokeTaskNodeList.peekLast());
    }

    public void locateNextTaskNode() {
        if (!currentTaskNode().isPresent()) {
            this.nextInvokeTaskNode = null;
            return;
        }
        EventNode currentEventNode = GlobalUtil.notNull(currentTaskNode().get().getEventNode());
        if (CollectionUtils.isEmpty(currentEventNode.getNextEventNodeList())) {
            this.nextInvokeTaskNode = null;
            return;
        }
        if (currentEventNode.getNextEventNodeList().size() == 1) {
            this.nextInvokeTaskNode = GlobalUtil.notNull(currentEventNode.getNextEventNodeList().get(0).getTaskNode());
            return;
        }
        EventNode nextNode = TaskActionUtil.locateInvokeEventNode(getStoryBus(), currentEventNode.getNextEventNodeList());
        AssertUtil.notNull(nextNode);
        this.nextInvokeTaskNode = nextNode.getTaskNode();
    }

    private boolean needSkipCurrentNode(TaskNode taskNode, StoryBus storyBus) {

        AssertUtil.anyNotNull(taskNode, storyBus);
        if (CollectionUtils.isEmpty(taskNode.getFilterStrategyRuleList())) {
            return false;
        }
        return !TaskActionUtil.matchStrategyRule(taskNode.getFilterStrategyRuleList(), storyBus);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(currentTaskNode().map(TaskNode::getEventGroupName).orElse("INIT"));
    }
}
