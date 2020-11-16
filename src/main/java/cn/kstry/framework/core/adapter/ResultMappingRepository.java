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
package cn.kstry.framework.core.adapter;

import cn.kstry.framework.core.route.RouteNode;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lykan
 */
public class ResultMappingRepository {

    /**
     * node task 映射表
     */
    private final Map<RouteNode, Map<RouteNode, Map<String, String>>> globalRouteNodeMappingMap = new ConcurrentHashMap<>();

    public Map<String, String> getTaskResultMapping(RouteNode fromNode, RouteNode toNode) {
        if (fromNode == null || toNode == null) {
            return new HashMap<>();
        }

        Map<RouteNode, Map<String, String>> fromRouteNodeMappingMap = globalRouteNodeMappingMap.get(toNode);
        if (MapUtils.isEmpty(fromRouteNodeMappingMap)) {
            return new HashMap<>();
        }

        Map<String, String> routeNodeMappingMap = fromRouteNodeMappingMap.get(fromNode);
        return MapUtils.isEmpty(routeNodeMappingMap) ? new HashMap<>() : routeNodeMappingMap;
    }

    public void putMap(Map<RouteNode, Map<RouteNode, Map<String, String>>> map) {
        this.globalRouteNodeMappingMap.putAll(map);
    }
}
