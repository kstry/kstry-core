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
package cn.kstry.framework.core.config;

import cn.kstry.framework.core.enums.ComponentTypeEnum;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 定义 & 获取配置信息
 *
 * @author lykan
 */
public interface TaskStoryDefConfig {

    String STORY_DEF = "story_def";

    String ROUTE_STRATEGY_DEF = "route_strategy_def";

    String REQUEST_MAPPING_DEF = "request_mapping_def";

    String NODE_DEF = "node_def";

    /**
     * 获取 story 定义
     */
    StoryDef<String, StoryDefItem<StoryDefItemConfig>> getStoryDef();

    /**
     * 获取路由策略
     */
    RouteStrategyDef<String, RouteStrategyDefItem<RouteStrategyDefItemConfig>> getRouteStrategyDef();

    /**
     * 获取参数映射规则
     */
    RequestMappingDef<String, RequestMappingDefItem<String, String>> getRequestMappingDef();

    /**
     * 获取 node 定义
     */
    NodeDef<String, NodeDefItem<String, NodeDefItemConfig>> getNodeDef();

    class StoryDef<String, StoryDefItem> extends HashMap<String, StoryDefItem> {

    }

    class StoryDefItem<StoryDefItemConfig> extends ArrayList<StoryDefItemConfig> {

    }

    class StoryDefItemConfig {

        /**
         * 执行 NODE 名称
         */
        @JSONField(name = "node_name")
        private String nodeName;

        /**
         * 请求映射规则
         */
        @JSONField(name = "request_mapping")
        private String requestMapping;

        /**
         * 匹配规则
         */
        @JSONField(name = "route_strategy")
        private String routeStrategy;

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getRequestMapping() {
            return requestMapping;
        }

        public void setRequestMapping(String requestMapping) {
            this.requestMapping = requestMapping;
        }

        public String getRouteStrategy() {
            return routeStrategy;
        }

        public void setRouteStrategy(String routeStrategy) {
            this.routeStrategy = routeStrategy;
        }
    }

    class RouteStrategyDef<String, RouteStrategyDefItem> extends HashMap<String, RouteStrategyDefItem> {

    }

    class RouteStrategyDefItem<RouteStrategyDefItemConfig> extends ArrayList<RouteStrategyDefItemConfig> {

    }

    class RouteStrategyDefItemConfig {

        /**
         * 下一个，待执行的 story 列表
         */
        @JSONField(name = "next_story")
        private String nextStory;

        /**
         * 全部匹配，执行该 item 指定的 story
         */
        @JSONField(name = "match_strategy")
        private Map<String, String> matchStrategy;

        /**
         * 全部匹配，执行该 item 指定的 story，否则跳过该 item 指定的 story
         */
        @JSONField(name = "filter_strategy")
        private Map<String, String> filterStrategy;

        public String getNextStory() {
            return nextStory;
        }

        public void setNextStory(String nextStory) {
            this.nextStory = nextStory;
        }

        public Map<String, String> getMatchStrategy() {
            return matchStrategy;
        }

        public void setMatchStrategy(Map<String, String> matchStrategy) {
            this.matchStrategy = matchStrategy;
        }

        public Map<String, String> getFilterStrategy() {
            return filterStrategy;
        }

        public void setFilterStrategy(Map<String, String> filterStrategy) {
            this.filterStrategy = filterStrategy;
        }
    }

    class NodeDef<String, NodeDefItem> extends HashMap<String, NodeDefItem> {

    }

    class NodeDefItem<String, NodeDefItemConfig> extends HashMap<String, NodeDefItemConfig> {

    }

    class NodeDefItemConfig {

        /**
         * @see ComponentTypeEnum
         */
        private String type;

        /**
         * Node Task 对应的方法
         */
        private String method;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }

    class RequestMappingDef<String, RequestMappingDefItem> extends HashMap<String, RequestMappingDefItem> {

    }

    class RequestMappingDefItem<String, String0> extends HashMap<String, String0> {

    }
}
