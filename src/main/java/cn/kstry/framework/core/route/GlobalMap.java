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

import cn.kstry.framework.core.adapter.ResultMappingRepository;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局地图
 *
 * @author lykan
 */
public class GlobalMap {

    /**
     * 整个业务地图的始发点列表
     */
    private final Map<String, List<EventNode>> FIRST_MAP_NODES = new ConcurrentHashMap<>();

    /**
     * mapping 映射表
     */
    private ResultMappingRepository resultMappingRepository;

    public void addFirstEventNode(String actionName, EventNode eventNode) {
        AssertUtil.notNull(eventNode);
        AssertUtil.notBlank(actionName);

        List<EventNode> eventNodeList = FIRST_MAP_NODES.get(actionName);
        if (CollectionUtils.isEmpty(eventNodeList)) {
            eventNodeList = Lists.newArrayList();
        }
        if (eventNodeList.contains(eventNode)) {
            return;
        }
        eventNodeList.add(eventNode);
    }

    public EventNode locateFirstEventNode(String actionName) {
//        AssertUtil.notBlank(actionName);
//        EventNode eventNode = firstEventNodes.get(actionName);
//        AssertUtil.notNull(eventNode);
        return null;
    }

    public ResultMappingRepository getResultMappingRepository() {
        return resultMappingRepository;
    }

    public void setResultMappingRepository(ResultMappingRepository resultMappingRepository) {
        this.resultMappingRepository = resultMappingRepository;
    }

}
