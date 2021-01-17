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
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.LocateBehavior;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<String, List<MapNode>> FIRST_MAP_NODES = new ConcurrentHashMap<>();

    /**
     * mapping 映射表
     */
    private ResultMappingRepository resultMappingRepository;

    public void addFirstMapNode(String actionName, MapNode mapNode) {
        AssertUtil.notNull(mapNode);
        AssertUtil.notBlank(actionName);

        List<MapNode> mapNodeList = FIRST_MAP_NODES.get(actionName);
        if (CollectionUtils.isEmpty(mapNodeList)) {
            mapNodeList = Lists.newArrayList();
        }
        if (mapNodeList.contains(mapNode)) {
            return;
        }
        mapNodeList.add(mapNode);
    }

    public MapNode locateFirstMapNode(String actionName) {
//        AssertUtil.notBlank(actionName);
//        MapNode mapNode = firstMapNodes.get(actionName);
//        AssertUtil.notNull(mapNode);
        return null;
    }

    public ResultMappingRepository getResultMappingRepository() {
        return resultMappingRepository;
    }

    public void setResultMappingRepository(ResultMappingRepository resultMappingRepository) {
        this.resultMappingRepository = resultMappingRepository;
    }

    public static class MapNode {

        /**
         * 当前节点中保存的接下来，可以允许被执行的 MpaNode
         */
        private final List<MapNode> nextMapNodeList = new ArrayList<>();

        /**
         * 前一个 MapNode
         */
        private MapNode prevMapNode;

        /**
         * 具体参与控制程序执行的路由 Node
         * MapNode 与 RouteNode 一一对应
         */
        private final RouteNode routeNode;

        public MapNode(RouteNode routeNode) {
            AssertUtil.notNull(routeNode);
            this.routeNode = routeNode;
            routeNode.setMapNode(this);
        }

        public void addNextMapNode(MapNode mapNode) {
            AssertUtil.notNull(mapNode);
            if (nextMapNodeList.contains(mapNode)) {
                return;
            }
            nextMapNodeList.add(mapNode);
            mapNode.setPrevMapNode(this);
        }

        public Optional<MapNode> locateNextMapNode(LocateBehavior<RouteNode> locateBehavior) {
            if (CollectionUtils.isEmpty(nextMapNodeList)) {
                return Optional.empty();
            }

            if (locateBehavior == null) {
                return Optional.ofNullable(nextMapNodeList.get(0));
            }

            List<MapNode> resultList = nextMapNodeList.stream().filter(map -> locateBehavior.locate(map.getRouteNode())).collect(Collectors.toList());
            AssertUtil.oneSize(resultList, ExceptionEnum.INVALID_POSITIONING_BEHAVIOR);
            return Optional.of(resultList.get(0));
        }

        public Optional<MapNode> locateNextMapNode() {
            return this.locateNextMapNode(null);
        }

        public RouteNode getRouteNode() {
            return routeNode;
        }

        public MapNode getPrevMapNode() {
            return prevMapNode;
        }

        public List<MapNode> getNextMapNodeList() {
            return nextMapNodeList;
        }

        public int nextMapNodeSize() {
            if (CollectionUtils.isEmpty(this.nextMapNodeList)) {
                return 0;
            }
            return this.nextMapNodeList.size();
        }

        private void setPrevMapNode(MapNode prevMapNode) {
            this.prevMapNode = prevMapNode;
        }

    }
}
