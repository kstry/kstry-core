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
package cn.kstry.framework.core.config;

import java.util.List;
import java.util.Map;

/**
 * @author lykan
 */
public class TaskRouteConfig {

    /**
     * NODE 定义
     */
    private Map<String, String> nodeDefinition;

    /**
     * params mapping
     */
    private Map<String, Map<String, String>> resultMapping;

    /**
     * 线路 路由 规划
     */
    private Map<String, List<MapPlanningNodeItem>> mapPlanning;

    public Map<String, String> getNodeDefinition() {
        return nodeDefinition;
    }

    public void setNodeDefinition(Map<String, String> nodeDefinition) {
        this.nodeDefinition = nodeDefinition;
    }

    public Map<String, Map<String, String>> getResultMapping() {
        return resultMapping;
    }

    public void setResultMapping(Map<String, Map<String, String>> resultMapping) {
        this.resultMapping = resultMapping;
    }

    public Map<String, List<MapPlanningNodeItem>> getMapPlanning() {
        return mapPlanning;
    }

    public void setMapPlanning(Map<String, List<MapPlanningNodeItem>> mapPlanning) {
        this.mapPlanning = mapPlanning;
    }

    public static class MapPlanningNodeItem {

        private String nodeName;

        private List<String> mapping;

        private Map<String, Map<String, String>> routeStrategy;

        private Map<String, List<MapPlanningNodeItem>> routeMap;

        /**
         * 中断 timeSlot
         */
        private Boolean interruptTimeSlot;

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public List<String> getMapping() {
            return mapping;
        }

        public void setMapping(List<String> mapping) {
            this.mapping = mapping;
        }

        public Map<String, Map<String, String>> getRouteStrategy() {
            return routeStrategy;
        }

        public void setRouteStrategy(Map<String, Map<String, String>> routeStrategy) {
            this.routeStrategy = routeStrategy;
        }

        public Map<String, List<MapPlanningNodeItem>> getRouteMap() {
            return routeMap;
        }

        public void setRouteMap(Map<String, List<MapPlanningNodeItem>> routeMap) {
            this.routeMap = routeMap;
        }

        public Boolean getInterruptTimeSlot() {
            return interruptTimeSlot;
        }

        public void setInterruptTimeSlot(Boolean interruptTimeSlot) {
            this.interruptTimeSlot = interruptTimeSlot;
        }
    }
}
