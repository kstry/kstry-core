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
import cn.kstry.framework.core.engine.EventGroup;
import cn.kstry.framework.core.engine.TaskActionMethod;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.route.EventNode;
import cn.kstry.framework.core.route.GlobalMap;
import cn.kstry.framework.core.route.RouteEventGroup;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ProxyUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lykan
 */
@Import(ConfigResolver.class)
public class StoryEnginePropertyRegister implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Bean("defaultEventGroupList")
    public List<EventGroup> getEventGroupList() {
        Map<String, EventGroup> eventGroupMap = applicationContext.getBeansOfType(EventGroup.class);
        List<EventGroup> eventGroupList = Lists.newArrayList(eventGroupMap.values());

        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(EventGroupComponent.class);
        List<AnnotationEventGroupProxy> annotationEventGroupList = beansWithAnnotation.values().stream().map(o -> {
            Class<?> targetClass = ProxyUtil.noneProxyClass(o);
            EventGroupComponent eventGroupComponent = targetClass.getAnnotation(EventGroupComponent.class);
            String eventGroupName = eventGroupComponent.eventGroupName();
            ComponentTypeEnum eventGroupTypeEnum = eventGroupComponent.eventGroupTypeEnum();
            Class<? extends EventOperatorRole> operatorRoleClass = eventGroupComponent.operatorRoleClass();

            AssertUtil.notNull(eventGroupTypeEnum, ExceptionEnum.NOT_ALLOW_EMPTY, "The eventGroupTypeEnum is not allowed to be empty!");
            AssertUtil.notBlank(eventGroupName, ExceptionEnum.NOT_ALLOW_EMPTY, "The eventGroupName is not allowed to be empty!");
            AssertUtil.notNull(operatorRoleClass, ExceptionEnum.NOT_ALLOW_EMPTY, "The operatorRoleClass is not allowed to be empty!");

            AnnotationEventGroupProxy annotationEventGroupProxy = new AnnotationEventGroupProxy(o);
            annotationEventGroupProxy.setEventGroupName(eventGroupName);
            annotationEventGroupProxy.setEventGroupTypeEnum(eventGroupTypeEnum);
            annotationEventGroupProxy.setOperatorRoleClass(operatorRoleClass);
            return annotationEventGroupProxy;
        }).collect(Collectors.toList());
        eventGroupList.addAll(annotationEventGroupList);

        long actionCount = eventGroupList.stream().map(a -> a.getEventGroupName() + "-" + a.getEventGroupTypeEnum()).distinct().count();
        AssertUtil.equals((long) eventGroupList.size(), actionCount, ExceptionEnum.TASK_IDENTIFY_DUPLICATE_DEFINITION);
        return eventGroupList;
    }

    @Bean("defaultGlobalMap")
    public GlobalMap getGlobalMap(@Qualifier("defaultEventGroupList") List<EventGroup> eventGroupList) {

        Map<String, ConfigResolver> configResolverMap = applicationContext.getBeansOfType(ConfigResolver.class);
        AssertUtil.notEmpty(configResolverMap);

        GlobalMap globalMap = new GlobalMap();
        ConfigResolver configResolver = configResolverMap.values().iterator().next();

        Map<String, List<EventNode>> storyEventNode = configResolver.getStoryEventNode(eventGroupList, EnableKstryRegister.getKstryConfigPath());
        if (MapUtils.isEmpty(storyEventNode)) {
            return globalMap;
        }
        storyEventNode.forEach(globalMap::addFirstEventNode);
        return globalMap;
    }

    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static class AnnotationEventGroupProxy extends RouteEventGroup implements EventGroup {

        private String eventGroupName;

        private ComponentTypeEnum eventGroupTypeEnum;

        private final Object eventGroup;

        private Class<? extends EventOperatorRole> operatorRoleClass;

        public AnnotationEventGroupProxy(Object eventGroup) {
            this.eventGroup = eventGroup;
        }

        public Object getEventGroup() {
            return eventGroup;
        }

        @Override
        public String getEventGroupName() {
            return eventGroupName;
        }

        public void setEventGroupName(String eventGroupName) {
            this.eventGroupName = eventGroupName;
        }

        @Override
        public ComponentTypeEnum getEventGroupTypeEnum() {
            return eventGroupTypeEnum;
        }

        public void setEventGroupTypeEnum(ComponentTypeEnum eventGroupTypeEnum) {
            this.eventGroupTypeEnum = eventGroupTypeEnum;
        }

        public Class<? extends EventOperatorRole> getOperatorRoleClass() {
            return operatorRoleClass;
        }

        public void setOperatorRoleClass(Class<? extends EventOperatorRole> operatorRoleClass) {
            this.operatorRoleClass = operatorRoleClass;
        }

        @Override
        public Map<String, TaskActionMethod> getActionMethodMap() {
            Class<?> targetClass = ProxyUtil.noneProxyClass(this.eventGroup);
            return super.getTaskActionMethodMap(targetClass);
        }
    }
}