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

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.TaskActionUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 全局地图
 *
 * @author lykan
 */
public class GlobalMap {

    /**
     * 整个业务地图的始发点列表
     */
    private final Map<String, List<EventNode>> firstEventNodes = new HashMap<>();

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    public void addFirstEventNode(String actionName, List<EventNode> eventNodeList) {

        AssertUtil.notEmpty(eventNodeList);
        AssertUtil.notBlank(actionName);

        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        try {
            List<EventNode> firstEventNodeList = firstEventNodes.get(actionName);
            if (CollectionUtils.isEmpty(firstEventNodeList)) {
                firstEventNodeList = Lists.newArrayList();
                firstEventNodes.put(actionName, firstEventNodeList);
            }

            for (EventNode eventNode : eventNodeList) {
                if (firstEventNodeList.contains(eventNode)) {
                    continue;
                }
                firstEventNodeList.add(eventNode);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public EventNode locateFirstEventNode(Object request, String storyName) {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        try {
            AssertUtil.notBlank(storyName, ExceptionEnum.PARAMS_ERROR, "storyName not allowed to be empty!");

            List<EventNode> eventNodeList = firstEventNodes.get(storyName);
            AssertUtil.notEmpty(eventNodeList, ExceptionEnum.PARAMS_ERROR, "Unable to match to an executable story! storyName:%s", storyName);
            if (eventNodeList.size() == 1) {
                return eventNodeList.get(0);
            }

            AssertUtil.notNull(request, ExceptionEnum.PARAMS_ERROR, "When there are multiple start event nodes, request is not allowed to be empty!");

            List<EventNode> collect = eventNodeList.stream()
                    .filter(eventNode -> TaskActionUtil.matchStrategyRule(eventNode.getMatchStrategyRuleList(), request))
                    .collect(Collectors.toList());


            return null;
        } finally {
            readLock.unlock();
        }
    }
}
