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

import cn.kstry.framework.core.enums.CalculateEnum;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 定义 & 解析配置文件
 *
 * @author lykan
 */
public class TaskStoryDefConfig {

    public static final String STORY_DEF = "story_def";

    public static final String ROUTE_STRATEGY_DEF = "route_strategy_def";

    public static final String REQUEST_MAPPING_DEF = "request_mapping_def";

    public static final String NODE_DEF = "node_def";

    @JSONField(name = STORY_DEF)
    private StoryDef<String, StoryDefItem<StoryDefItemConfig>> storyDef;

    @JSONField(name = ROUTE_STRATEGY_DEF)
    private RouteStrategyDef<String, RouteStrategyDefItem<RouteStrategyDefItemConfig>> routeStrategyDef;

    @JSONField(name = REQUEST_MAPPING_DEF)
    private RequestMappingDef<String, RequestMappingDefItem<String, String>> requestMappingDef;

    @JSONField(name = NODE_DEF)
    private NodeDef<String, NodeDefItem<String, NodeDefItemConfig>> nodeDef;

    public StoryDef<String, StoryDefItem<StoryDefItemConfig>> getStoryDef() {
        return storyDef;
    }

    public void setStoryDef(StoryDef<String, StoryDefItem<StoryDefItemConfig>> storyDef) {
        this.storyDef = storyDef;
    }

    public RouteStrategyDef<String, RouteStrategyDefItem<RouteStrategyDefItemConfig>> getRouteStrategyDef() {
        return routeStrategyDef;
    }

    public void setRouteStrategyDef(RouteStrategyDef<String, RouteStrategyDefItem<RouteStrategyDefItemConfig>> routeStrategyDef) {
        this.routeStrategyDef = routeStrategyDef;
    }

    public RequestMappingDef<String, RequestMappingDefItem<String, String>> getRequestMappingDef() {
        return requestMappingDef;
    }

    public void setRequestMappingDef(RequestMappingDef<String, RequestMappingDefItem<String, String>> requestMappingDef) {
        this.requestMappingDef = requestMappingDef;
    }

    public NodeDef<String, NodeDefItem<String, NodeDefItemConfig>> getNodeDef() {
        return nodeDef;
    }

    public void setNodeDef(NodeDef<String, NodeDefItem<String, NodeDefItemConfig>> nodeDef) {
        this.nodeDef = nodeDef;
    }

    public static class StoryDef<String, StoryDefItem> extends HashMap<String, StoryDefItem> {

    }

    public static class StoryDefItem<StoryDefItemConfig> extends ArrayList<StoryDefItemConfig> {

    }

    public static class StoryDefItemConfig {

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

    public static class RouteStrategyDef<String, RouteStrategyDefItem> extends HashMap<String, RouteStrategyDefItem> {

    }

    public static class RouteStrategyDefItem<RouteStrategyDefItemConfig> extends ArrayList<RouteStrategyDefItemConfig> {

    }

    public static class RouteStrategyDefItemConfig {

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

    public static class NodeDef<String, NodeDefItem> extends HashMap<String, NodeDefItem> {

    }

    public static class NodeDefItem<String, NodeDefItemConfig> extends HashMap<String, NodeDefItemConfig> {

    }

    public static class NodeDefItemConfig {

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

    public static class RequestMappingDef<String, RequestMappingDefItem> extends HashMap<String, RequestMappingDefItem> {

    }

    public static class RequestMappingDefItem<String, String0> extends HashMap<String, String0> {

    }

    public static TaskStoryDefConfig parseTaskStoryConfig(String configStr) {
        AssertUtil.notBlank(configStr, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config file blank!");
        Map<String, String> configMap = JSON.parseObject(configStr, new TypeReference<HashMap<String, String>>() {
        });

        TaskStoryDefConfig config = new TaskStoryDefConfig();
        if (StringUtils.isNotBlank(configMap.get(NODE_DEF))) {
            config.setNodeDef(JSON.parseObject(configMap.get(NODE_DEF),
                    new TypeReference<NodeDef<String, NodeDefItem<String, NodeDefItemConfig>>>() {
                    })
            );
            checkNodeDef(config);
        }

        if (StringUtils.isNotBlank(configMap.get(REQUEST_MAPPING_DEF))) {
            config.setRequestMappingDef(JSON.parseObject(configMap.get(REQUEST_MAPPING_DEF),
                    new TypeReference<RequestMappingDef<String, RequestMappingDefItem<String, String>>>() {
                    })
            );
            checkRequestMappingDef(config);
        }

        if (StringUtils.isNotBlank(configMap.get(STORY_DEF))) {
            config.setStoryDef(JSON.parseObject(configMap.get(STORY_DEF),
                    new TypeReference<StoryDef<String, StoryDefItem<StoryDefItemConfig>>>() {
                    })
            );
            checkStoryDef(config);
        }

        if (StringUtils.isNotBlank(configMap.get(ROUTE_STRATEGY_DEF))) {
            config.setRouteStrategyDef(JSON.parseObject(configMap.get(ROUTE_STRATEGY_DEF),
                    new TypeReference<RouteStrategyDef<String, RouteStrategyDefItem<RouteStrategyDefItemConfig>>>() {
                    })
            );
            checkRouteStrategyDef(config);
        }
        return config;
    }


    private static void checkRouteStrategyDef(TaskStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getRouteStrategyDef())) {
            return;
        }

        // NodeDef 为空时，RouteStrategyDef 不允许出现
        AssertUtil.isTrue(MapUtils.isNotEmpty(config.getNodeDef()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def is empty but route strategy not empty!");

        // StoryDef 为空时，RouteStrategyDef 不允许出现
        AssertUtil.isTrue(MapUtils.isNotEmpty(config.getStoryDef()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config story_def is empty but route strategy not empty!");

        List<String> storyNameList = MapUtils.isEmpty(config.getStoryDef()) ? Lists.newArrayList() : Lists.newArrayList(config.getStoryDef().keySet());
        for (String k : config.getRouteStrategyDef().keySet()) {
            AssertUtil.notBlank(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config route_strategy_def strategy key is blank!");

            RouteStrategyDefItem<RouteStrategyDefItemConfig> v = config.getRouteStrategyDef().get(k);
            if (CollectionUtils.isEmpty(v)) {
                continue;
            }
            v.forEach(strategy -> {
                AssertUtil.notBlank(strategy.getNextStory(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config route_strategy_def 'next_story' is blank!");
                AssertUtil.isTrue(storyNameList.contains(strategy.getNextStory()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                        "config route_strategy_def 'next_story' is error! next_story:%s", strategy.getNextStory());
                if (MapUtils.isNotEmpty(strategy.getFilterStrategy())) {
                    strategy.getFilterStrategy().keySet().forEach(kName -> {
                        AssertUtil.notBlank(kName, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config route_strategy_def filter_strategy key is blank!");
                        String[] split = kName.split("-");
                        AssertUtil.isTrue(split.length == 2, ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "config route_strategy_def filter_strategy key error! filter_strategy:%s", kName);
                        AssertUtil.isTrue(CalculateEnum.getCalculateEnumByName(split[0]).isPresent(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "config route_strategy_def filter_strategy key error! filter_strategy:%s", kName);
                    });
                }

                if (MapUtils.isNotEmpty(strategy.getMatchStrategy())) {
                    strategy.getMatchStrategy().keySet().forEach(kName -> {
                        AssertUtil.notBlank(kName, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config route_strategy_def match_strategy key is blank!");
                        String[] split = kName.split("-");
                        AssertUtil.isTrue(split.length == 2, ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "config route_strategy_def match_strategy key error! match_strategy:%s", kName);
                        AssertUtil.isTrue(CalculateEnum.getCalculateEnumByName(split[0]).isPresent(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                                "config route_strategy_def match_strategy key error! match_strategy:%s", kName);
                    });
                }
            });
        }

        for (StoryDefItem<StoryDefItemConfig> nodeList : config.getStoryDef().values()) {
            for (StoryDefItemConfig node : nodeList) {
                if (StringUtils.isNotBlank(node.getRouteStrategy())) {
                    String strategy = node.getRouteStrategy();
                    AssertUtil.notNull(config.getRouteStrategyDef().get(strategy),
                            ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config story_def 'route_strategy' error! route_strategy:%s", strategy);
                }
            }
        }
    }

    private static void checkStoryDef(TaskStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getStoryDef())) {
            return;
        }

        // NodeDef 为空时，StoryDef 不允许出现
        AssertUtil.isTrue(MapUtils.isNotEmpty(config.getNodeDef()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def is empty but story def not empty!");

        List<String> nodeNameList = new ArrayList<>();
        for (NodeDefItem<String, NodeDefItemConfig> stringNodeDefItemConfigNodeDefItem : config.getNodeDef().values()) {
            nodeNameList.addAll(stringNodeDefItemConfigNodeDefItem.keySet());
        }
        List<String> mappingNameList = MapUtils.isEmpty(config.getRequestMappingDef()) ? Lists.newArrayList() : Lists.newArrayList(config.getRequestMappingDef().keySet());
        for (String k : config.getStoryDef().keySet()) {
            AssertUtil.notBlank(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config story_def story key is blank!");

            StoryDefItem<StoryDefItemConfig> v = config.getStoryDef().get(k);
            if (CollectionUtils.isEmpty(v)) {
                continue;
            }
            v.forEach(storyItem -> {
                AssertUtil.isTrue(StringUtils.isNotBlank(storyItem.getNodeName()) || StringUtils.isNotBlank(storyItem.getRouteStrategy()),
                        ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config story_def node_name is blank!");
                if (StringUtils.isNotBlank(storyItem.getNodeName())) {
                    AssertUtil.isTrue(nodeNameList.contains(storyItem.getNodeName()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "config story_def node_name is error! node_name:%s", storyItem.getNodeName());
                }
                if (StringUtils.isNotBlank(storyItem.getRequestMapping())) {
                    AssertUtil.isTrue(mappingNameList.contains(storyItem.getRequestMapping()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                            "config story_def request_mapping is error! request_mapping:%s", storyItem.getRequestMapping());
                }
            });
        }
    }

    private static void checkRequestMappingDef(TaskStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getRequestMappingDef())) {
            return;
        }

        // NodeDef 为空时，RequestMappingDef 不允许出现
        AssertUtil.isTrue(MapUtils.isNotEmpty(config.getNodeDef()), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def is empty but request mapping not empty!");

        List<String> nodeNameList = new ArrayList<>();
        for (NodeDefItem<String, NodeDefItemConfig> stringNodeDefItemConfigNodeDefItem : config.getNodeDef().values()) {
            nodeNameList.addAll(stringNodeDefItemConfigNodeDefItem.keySet());
        }
        for (String k : config.getRequestMappingDef().keySet()) {
            AssertUtil.notBlank(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config request_mapping_def mapping key is blank!");

            RequestMappingDefItem<String, String> v = config.getRequestMappingDef().get(k);
            if (MapUtils.isEmpty(v)) {
                continue;
            }
            v.forEach((kIn, vIn) -> {
                AssertUtil.notBlank(kIn, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config request_mapping_def mapping item key is blank!");
                AssertUtil.notBlank(vIn, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config request_mapping_def mapping item value is blank!");

                boolean checkVin = vIn.startsWith("DEFAULT") || vIn.startsWith("#data") || nodeNameList.stream().anyMatch(vIn::startsWith);
                AssertUtil.isTrue(checkVin, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config request_mapping_def mapping item value error! value:%s", vIn);
            });
        }
    }

    private static void checkNodeDef(TaskStoryDefConfig config) {
        if (MapUtils.isEmpty(config.getNodeDef())) {
            return;
        }

        List<String> nodeNameList = new ArrayList<>();
        for (String k : config.getNodeDef().keySet()) {
            AssertUtil.notBlank(k, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def group key is blank!");

            NodeDefItem<String, NodeDefItemConfig> v = config.getNodeDef().get(k);
            if (MapUtils.isEmpty(v)) {
                continue;
            }

            v.forEach((kIn, vIn) -> {
                AssertUtil.notBlank(kIn, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def node key is blank!");
                AssertUtil.notBlank(vIn.getMethod(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def 'method' is blank!");
                AssertUtil.notBlank(vIn.getType(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def 'type' is blank!");
                AssertUtil.isTrue(ComponentTypeEnum.getComponentTypeEnumByName(vIn.getType()).isPresent(),
                        ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config node_def 'type' error! type:%s", vIn.getType());
                nodeNameList.add(kIn);
            });
        }
        AssertUtil.isTrue(nodeNameList.size() == new HashSet<>(nodeNameList).size(), ExceptionEnum.CONFIGURATION_PARSE_FAILURE,
                "config node_def node name duplicate definition!");
    }
}
