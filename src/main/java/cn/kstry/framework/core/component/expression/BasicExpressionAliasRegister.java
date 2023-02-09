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
package cn.kstry.framework.core.component.expression;

import cn.kstry.framework.core.util.ExpressionAliasUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.Objects;


/**
 * 注册基础别名
 *
 * @author lykan
 */
public class BasicExpressionAliasRegister implements ExpressionAliasRegister {

    @Override
    public List<ExpressionAlias> registerAlias() {
        return Lists.newArrayList(
                // object util
                new ExpressionAlias(Exp.equals, Objects.class, "equals"),
                new ExpressionAlias(Exp.notEquals, ExpressionAliasUtil.class, "notEquals"),
                new ExpressionAlias(Exp.isNull, Objects.class, "isNull"),
                new ExpressionAlias(Exp.notNull, Objects.class, "nonNull"),
                new ExpressionAlias(Exp.anyNull, ObjectUtils.class, "anyNull"),
                new ExpressionAlias(Exp.noneNull, ObjectUtils.class, "allNotNull"),

                // string util
                new ExpressionAlias(Exp.isBlank, StringUtils.class, "isBlank"),
                new ExpressionAlias(Exp.notBlank, StringUtils.class, "isNotBlank"),
                new ExpressionAlias(Exp.noneBlank, StringUtils.class, "isNoneBlank"),
                new ExpressionAlias(Exp.allBlank, StringUtils.class, "isAllBlank"),
                new ExpressionAlias(Exp.anyBlank, StringUtils.class, "isAnyBlank"),

                // number util
                new ExpressionAlias(Exp.isNumber, NumberUtils.class, "isCreatable"),
                new ExpressionAlias(Exp.toInt, NumberUtils.class, "toInt"),
                new ExpressionAlias(Exp.toLong, NumberUtils.class, "toLong"),
                new ExpressionAlias(Exp.toDouble, NumberUtils.class, "toDouble"),
                new ExpressionAlias(Exp.toFloat, NumberUtils.class, "toFloat"),
                new ExpressionAlias(Exp.toByte, NumberUtils.class, "toByte"),
                new ExpressionAlias(Exp.toShort, NumberUtils.class, "toShort"),

                // bool util
                new ExpressionAlias(Exp.toBool, BooleanUtils.class, "toBoolean"),
                new ExpressionAlias(Exp.isTrue, BooleanUtils.class, "isTrue"),
                new ExpressionAlias(Exp.isFalse, BooleanUtils.class, "isFalse"),
                new ExpressionAlias(Exp.notFalse, BooleanUtils.class, "isNotFalse"),
                new ExpressionAlias(Exp.notTrue, BooleanUtils.class, "isNotTrue"),

                // common util
                new ExpressionAlias(Exp.max, ObjectUtils.class, "max"),
                new ExpressionAlias(Exp.min, ObjectUtils.class, "min"),
                new ExpressionAlias(Exp.isEmpty, ExpressionAliasUtil.class, "isEmpty"),
                new ExpressionAlias(Exp.notEmpty, ExpressionAliasUtil.class, "notEmpty"),
                new ExpressionAlias(Exp.contains, ExpressionAliasUtil.class, "contains"),
                new ExpressionAlias(Exp.size, ExpressionAliasUtil.class, "size"),
                new ExpressionAlias(Exp.validId, ExpressionAliasUtil.class, "validId")
        );
    }
}
