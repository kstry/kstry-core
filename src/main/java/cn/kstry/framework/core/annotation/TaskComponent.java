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
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 只能标注在类上，被标注的类将被视作服务组件类。可以定义服务节点（被 cn.kstry.framework.core.annotation.TaskService 标注的方法 ）
 *
 * 被 cn.kstry.framework.core.annotation.TaskService 标注的方法被视作服务节点，服务节点只有在服务组件类中才会生效。定义在普通类中不会被容器解析执行
 *
 * @author lykan
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskComponent {

    /**
     * 服务组件类名称，默认为类名
     *
     * @return name
     */
    String name() default StringUtils.EMPTY;

    /**
     * 扫描父类文件
     *
     * @return 默认 true 除当前类文件，也会扫描父类中的服务节点。为 false 时只会扫描当前类中的服务节点
     */
    boolean scanSuper() default true;
}
