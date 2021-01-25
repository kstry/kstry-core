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

import cn.kstry.framework.core.route.EventGroup;
import cn.kstry.framework.core.enums.ComponentTypeEnum;
import cn.kstry.framework.core.operator.EventOperatorRole;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实现 cn.kstry.framework.core.route.EventGroup 定义的功能
 *
 * 正确 EventNode 的定义规则
 *  - 类实现 EventGroup 接口，或 使用 @EventGroupComponent 注解
 *  - 方法入参：无参数或者仅有一个参数
 *  - 方法出参：void 或者 实现了 TaskResponse 接口的类
 *
 * @author lykan
 * @see EventGroup
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface EventGroupComponent {

    /**
     * 继承 @Component
     */
    String value() default "";

    /**
     * event action group name
     */
    String eventGroupName();

    /**
     * event action group component type
     */
    ComponentTypeEnum eventGroupTypeEnum() default ComponentTypeEnum.TASK;

    /**
     * Operator 用来实际执行 EventGroup 中定义的 Event
     */
    Class<? extends EventOperatorRole> operatorRoleClass();
}
