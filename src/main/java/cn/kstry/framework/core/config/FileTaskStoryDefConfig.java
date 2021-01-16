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

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lykan
 */
public class FileTaskStoryDefConfig extends BaseTaskStoryDefConfig {

    public static TaskStoryDefConfig parseTaskStoryConfig(String configStr) {
        AssertUtil.notBlank(configStr, ExceptionEnum.CONFIGURATION_PARSE_FAILURE, "config file blank!");
        Map<String, String> configMap = JSON.parseObject(configStr, new TypeReference<HashMap<String, String>>() {
        });

        FileTaskStoryDefConfig config = new FileTaskStoryDefConfig();
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
}
