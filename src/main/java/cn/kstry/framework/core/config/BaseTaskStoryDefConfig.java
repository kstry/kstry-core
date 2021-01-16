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
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author lykan
 */
public abstract class BaseTaskStoryDefConfig implements TaskStoryDefConfig {

    @JSONField(name = STORY_DEF)
    private StoryDef<String, StoryDefItem<StoryDefItemConfig>> storyDef;

    @JSONField(name = ROUTE_STRATEGY_DEF)
    private RouteStrategyDef<String, RouteStrategyDefItem<RouteStrategyDefItemConfig>> routeStrategyDef;

    @JSONField(name = REQUEST_MAPPING_DEF)
    private RequestMappingDef<String, RequestMappingDefItem<String, String>> requestMappingDef;

    @JSONField(name = NODE_DEF)
    private NodeDef<String, NodeDefItem<String, NodeDefItemConfig>> nodeDef;

    @Override
    public StoryDef<String, StoryDefItem<StoryDefItemConfig>> getStoryDef() {
        return storyDef;
    }

    @Override
    public RouteStrategyDef<String, RouteStrategyDefItem<RouteStrategyDefItemConfig>> getRouteStrategyDef() {
        return routeStrategyDef;
    }

    @Override
    public RequestMappingDef<String, RequestMappingDefItem<String, String>> getRequestMappingDef() {
        return requestMappingDef;
    }

    @Override
    public NodeDef<String, NodeDefItem<String, NodeDefItemConfig>> getNodeDef() {
        return nodeDef;
    }

    protected void setStoryDef(StoryDef<String, StoryDefItem<StoryDefItemConfig>> storyDef) {
        this.storyDef = storyDef;
    }

    protected void setRouteStrategyDef(RouteStrategyDef<String, RouteStrategyDefItem<RouteStrategyDefItemConfig>> routeStrategyDef) {
        this.routeStrategyDef = routeStrategyDef;
    }

    protected void setRequestMappingDef(RequestMappingDef<String, RequestMappingDefItem<String, String>> requestMappingDef) {
        this.requestMappingDef = requestMappingDef;
    }

    protected void setNodeDef(NodeDef<String, NodeDefItem<String, NodeDefItemConfig>> nodeDef) {
        this.nodeDef = nodeDef;
    }

    protected static void checkRouteStrategyDef(TaskStoryDefConfig config) {
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

    protected static void checkStoryDef(TaskStoryDefConfig config) {
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

    protected static void checkRequestMappingDef(TaskStoryDefConfig config) {
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

    protected static void checkNodeDef(TaskStoryDefConfig config) {
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
