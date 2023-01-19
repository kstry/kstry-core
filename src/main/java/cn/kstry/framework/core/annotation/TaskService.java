/*
 *
 *  * Copyright (c) 2020-2023, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.annotation;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 只能标注在方法上，被标注的方法被视作服务节点，服务节点只有在服务组件类中才会生效。定义在普通类中不会被容器解析执行
 *
 * @author lykan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface TaskService {

    /**
     * 服务名称，默认为修饰方法名称
     *
     * @return name
     */
    String name() default StringUtils.EMPTY;

    /**
     * 对服务节点的描述，配置文件指定服务节点名称时该字段会被覆盖不会生效
     *
     * @return description
     */
    String desc() default StringUtils.EMPTY;

    /**
     * 子能力
     *
     * @return ability
     */
    String ability() default StringUtils.EMPTY;

    /**
     * 指定 cn.kstry.framework.core.kv.KvAbility 从哪个域获取变量
     *
     * @return ksScope
     */
    String kvScope() default StringUtils.EMPTY;

    /**
     * 调用服务节点时的调用属性
     *
     * @return 调用属性
     */
    Invoke invoke() default @Invoke();
}
