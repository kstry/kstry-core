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
package cn.kstry.framework.core.annotation;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;

import java.util.List;

/**
 * 将 story engine 注册到 spring 容器
 *
 * @author lykan
 */
public class EnableKstryRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {

        MultiValueMap<String, Object> enableKstryAnnotation = importingClassMetadata.getAllAnnotationAttributes("cn.kstry.framework.core.annotation.EnableKstry");
        AssertUtil.notNull(enableKstryAnnotation);

        List<Object> storyEngineNameList = enableKstryAnnotation.get("storyEngineName");
        AssertUtil.oneSize(storyEngineNameList, ExceptionEnum.INVALID_SYSTEM_PARAMS);

        String storyEngineName = "storyEngine";
        if (StringUtils.isNotBlank(String.valueOf(storyEngineNameList.get(0)))) {
            storyEngineName = String.valueOf(storyEngineNameList.get(0));
        }

        BeanDefinition beanDefinition = new RootBeanDefinition(StoryEngine.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setLazyInit(false);
        registry.registerBeanDefinition(storyEngineName, beanDefinition);
    }
}
