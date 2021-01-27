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
package cn.kstry.framework.core.annotation;

import cn.kstry.framework.core.config.ConfigResolver;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.route.EventGroup;
import cn.kstry.framework.core.route.EventNode;
import cn.kstry.framework.core.route.GlobalMap;
import cn.kstry.framework.core.util.AssertUtil;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

/**
 *
 * @author lykan
 */
public class GlobalMapRegister implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    @Bean(name = "defaultGlobalMap")
    public GlobalMap getGlobalMap() {

        Object defaultEventGroupListBean = applicationContext.getBean("defaultEventGroupList");
        AssertUtil.notNull(defaultEventGroupListBean);
        List<EventGroup> defaultEventGroupList = (List<EventGroup>) defaultEventGroupListBean;
        Map<String, ConfigResolver> configResolverMap = applicationContext.getBeansOfType(ConfigResolver.class);
        AssertUtil.notEmpty(configResolverMap);
        AssertUtil.oneSize(configResolverMap.values(), ExceptionEnum.INVALID_SYSTEM_PARAMS, "ConfigResolver cannot be repeatedly defined!");
        ConfigResolver configResolver = configResolverMap.values().iterator().next();

        GlobalMap globalMap = new GlobalMap();
        Map<String, List<EventNode>> storyEventNode = configResolver.getStoryEventNode(defaultEventGroupList, EnableKstryRegister.getKstryConfigPath());
        if (MapUtils.isEmpty(storyEventNode)) {
            return globalMap;
        }
        storyEventNode.forEach(globalMap::addFirstEventNode);
        return globalMap;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
