/*
 *
 *  * Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
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

import cn.kstry.framework.core.container.KstryContextResolver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启 Kstry 框架，通过该注解开启使用 Kstry 框架
 * 一个 Spring 容器只允许被 EnableKstry 标注一次
 *
 * @author lykan
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableScheduling
@Import({KstryContextResolver.class})
public @interface EnableKstry {

    /**
     * 加载 BPMN 配置文件路径
     *
     * @return BPMN 配置文件路径
     */
    String bpmnPath() default StringUtils.EMPTY;

    /**
     * 通过 cn.kstry.framework.core.kv.KvAbility 获取yml中定义的变量
     * 只支持yml、yaml 后缀名文件
     *
     * @return yml 配置文件
     */
    String propertiesPath() default StringUtils.EMPTY;
}

