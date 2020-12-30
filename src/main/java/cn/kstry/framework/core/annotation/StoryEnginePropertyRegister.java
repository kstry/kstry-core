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

import cn.kstry.framework.core.adapter.ResultMappingRepository;
import cn.kstry.framework.core.config.ConfigResolver;
import cn.kstry.framework.core.engine.TaskAction;
import cn.kstry.framework.core.engine.TaskActionMethod;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.operator.TaskActionOperatorRole;
import cn.kstry.framework.core.route.GlobalMap;
import cn.kstry.framework.core.route.RouteNode;
import cn.kstry.framework.core.route.RouteTaskAction;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ProxyUtil;
import cn.kstry.framework.core.util.TaskActionUtil;
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

    @Bean("defaultTaskActionList")
    public List<TaskAction> getTaskActions() {
        Map<String, TaskAction> taskActionMap = applicationContext.getBeansOfType(TaskAction.class);
        List<TaskAction> taskActionList = Lists.newArrayList(taskActionMap.values());

        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(TaskActionComponent.class);
        List<AnnotationTaskActionProxy> annotationTaskActionList = beansWithAnnotation.values().stream().map(o -> {
            Class<?> targetClass = ProxyUtil.noneProxyClass(o);
            TaskActionComponent taskActionComponent = targetClass.getAnnotation(TaskActionComponent.class);
            String taskActionName = taskActionComponent.taskActionName();
            ComponentTypeEnum componentTypeEnum = taskActionComponent.taskActionTypeEnum();
            Class<? extends TaskActionOperatorRole> operatorRoleClass = taskActionComponent.operatorRoleClass();

            AssertUtil.notNull(componentTypeEnum);
            AssertUtil.notBlank(taskActionName);
            AssertUtil.notNull(operatorRoleClass);

            AnnotationTaskActionProxy annotationTaskActionProxy = new AnnotationTaskActionProxy(o);
            annotationTaskActionProxy.setTaskActionName(taskActionName);
            annotationTaskActionProxy.setTaskActionTypeEnum(componentTypeEnum);
            annotationTaskActionProxy.setTaskActionOperatorRoleClass(operatorRoleClass);
            return annotationTaskActionProxy;
        }).collect(Collectors.toList());
        taskActionList.addAll(annotationTaskActionList);

        long actionCount = taskActionList.stream().map(a -> a.getTaskActionName() + "-" + a.getTaskActionTypeEnum()).distinct().count();
        AssertUtil.equals((long) taskActionList.size(), actionCount, ExceptionEnum.TASK_IDENTIFY_DUPLICATE_DEFINITION);
        return taskActionList;
    }

    @Bean("defaultGlobalMap")
    public GlobalMap getGlobalMap(@Qualifier("defaultTaskActionList") List<TaskAction> taskActionList) {

        Map<String, ConfigResolver> configResolverMap = applicationContext.getBeansOfType(ConfigResolver.class);
        AssertUtil.notEmpty(configResolverMap);

        GlobalMap globalMap = new GlobalMap();
        ConfigResolver configResolver = configResolverMap.values().iterator().next();

        configResolver.setTaskRouteConfigName(EnableKstryRegister.getKstryConfigPath());
        configResolver.initNodeAndMapping();
        Map<String, RouteNode> routeNodeMap = configResolver.getRouteNodeMap();
        if (MapUtils.isEmpty(routeNodeMap)) {
            return globalMap;
        }

        routeNodeMap.values().forEach(node -> node.setTaskActionMethod(TaskActionUtil.getTaskActionMethod(taskActionList, node)));
        Map<String, GlobalMap.MapNode> storyDefinition = configResolver.initStoryDefinition();
        if (MapUtils.isEmpty(storyDefinition)) {
            return globalMap;
        }
        storyDefinition.forEach(globalMap::addFirstMapNode);

        ResultMappingRepository resultMappingRepository = new ResultMappingRepository();
        resultMappingRepository.putMap(configResolver.getRouteNodeMappingMap());
        globalMap.setResultMappingRepository(resultMappingRepository);
        return globalMap;
    }

    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static class AnnotationTaskActionProxy extends RouteTaskAction implements TaskAction {

        private String taskActionName;

        private ComponentTypeEnum componentTypeEnum;

        private final Object taskAction;

        private Class<? extends TaskActionOperatorRole> operatorRoleClass;

        public AnnotationTaskActionProxy(Object taskAction) {
            this.taskAction = taskAction;
        }

        public Object getTaskAction() {
            return taskAction;
        }

        @Override
        public String getTaskActionName() {
            return taskActionName;
        }

        public void setTaskActionName(String taskActionName) {
            this.taskActionName = taskActionName;
        }

        @Override
        public ComponentTypeEnum getTaskActionTypeEnum() {
            return componentTypeEnum;
        }

        public void setTaskActionTypeEnum(ComponentTypeEnum componentTypeEnum) {
            this.componentTypeEnum = componentTypeEnum;
        }

        @Override
        public Class<? extends TaskActionOperatorRole> getTaskActionOperatorRoleClass() {
            AssertUtil.notNull(this.operatorRoleClass);
            return this.operatorRoleClass;
        }

        public void setTaskActionOperatorRoleClass(Class<? extends TaskActionOperatorRole> operatorRoleClass) {
            this.operatorRoleClass = operatorRoleClass;
        }

        @Override
        public Map<String, TaskActionMethod> getActionMethodMap() {
            Class<?> targetClass = ProxyUtil.noneProxyClass(this.taskAction);
            return super.getTaskActionMethodMap(targetClass);
        }
    }
}