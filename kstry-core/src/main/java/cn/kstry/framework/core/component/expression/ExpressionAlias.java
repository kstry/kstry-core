/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
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
package cn.kstry.framework.core.component.expression;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 表达式别名
 *
 * @author lykan
 */
public class ExpressionAlias {

    /**
     * 表达式别名
     */
    private final String alias;

    /**
     * 静态方法签名
     */
    private final String staticMethodSign;

    public ExpressionAlias(String alias, Class<?> targetClass, String methodName) {
        AssertUtil.notNull(targetClass);
        AssertUtil.anyNotBlank(alias, methodName);
        AssertUtil.isTrue(Stream.of(targetClass.getMethods())
                        .filter(m -> Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())).anyMatch(m -> Objects.equals(m.getName(), methodName)),
                ExceptionEnum.COMPONENT_PARAMS_ERROR, "The target class must contain static target methods. targetClass: {}, methodName: {}", targetClass, methodName);
        this.alias = alias;
        this.staticMethodSign = GlobalUtil.format("T({}).{}", targetClass.getName(), methodName);
    }

    public String getAlias() {
        return alias;
    }

    public String getStaticMethodSign() {
        return staticMethodSign;
    }
}
