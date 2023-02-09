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

import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.ElementParserUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import cn.kstry.framework.core.util.KeyUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

/**
 *
 * @author lykan
 */
public class Exp {

    public static String equals = "equals";

    public static String notEquals = "notEquals";

    public static String isNull = "isNull";

    public static String notNull = "notNull";

    public static String anyNull = "anyNull";

    public static String noneNull = "noneNull";

    public static String isBlank = "isBlank";

    public static String notBlank = "notBlank";

    public static String noneBlank = "noneBlank";

    public static String allBlank = "allBlank";

    public static String anyBlank = "anyBlank";

    public static String isNumber = "isNumber";

    public static String toInt = "toInt";

    public static String toLong = "toLong";

    public static String toDouble = "toDouble";

    public static String toFloat = "toFloat";

    public static String toByte = "toByte";

    public static String toShort = "toShort";

    public static String toBool = "toBool";

    public static String isTrue = "isTrue";

    public static String isFalse = "isFalse";

    public static String notFalse = "notFalse";

    public static String notTrue = "notTrue";

    public static String max = "max";

    public static String min = "min";

    public static String isEmpty = "isEmpty";

    public static String notEmpty = "notEmpty";

    public static String contains = "contains";

    public static String size = "size";

    public static String validId = "validId";

    private Integer order;

    protected String expression = StringUtils.EMPTY;

    public static String b(Consumer<Exp> builder) {
        Exp exp = new Exp();
        builder.accept(exp);
        return exp.build();
    }

    public Exp order(int order) {
        this.order = order;
        return this;
    }

    public Exp and() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " && ";
        return this;
    }

    public Exp and(String exp) {
        AssertUtil.notBlank(exp);
        this.expression = GlobalUtil.format("({}) && ({})", this.expression, exp);
        return this;
    }

    public Exp or() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " || ";
        return this;
    }

    public Exp or(String exp) {
        AssertUtil.notBlank(exp);
        this.expression = GlobalUtil.format("({}) || ({})", this.expression, exp);
        return this;
    }

    public Exp not(String exp) {
        validExpression(exp);
        this.expression = this.expression + "!" + exp;
        return this;
    }

    public Exp lt() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " < ";
        return this;
    }

    public Exp le() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " <= ";
        return this;
    }

    public Exp gt() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " > ";
        return this;
    }

    public Exp ge() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " >= ";
        return this;
    }

    public Exp eq() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " == ";
        return this;
    }

    public Exp ne() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " != ";
        return this;
    }

    public Exp sta(String... keys) {
        AssertUtil.notNull(keys);
        AssertUtil.anyNotBlank(keys);
        this.expression = this.expression + KeyUtil.sta(keys);
        return this;
    }

    public Exp var(String... keys) {
        AssertUtil.notNull(keys);
        AssertUtil.anyNotBlank(keys);
        this.expression = this.expression + KeyUtil.var(keys);
        return this;
    }

    public Exp req(String... keys) {
        AssertUtil.notNull(keys);
        AssertUtil.anyNotBlank(keys);
        this.expression = this.expression + KeyUtil.req(keys);
        return this;
    }

    public Exp res(String... keys) {
        AssertUtil.anyNotBlank(keys);
        this.expression = this.expression + KeyUtil.res(keys);
        return this;
    }

    public Exp value(Object value) {
        AssertUtil.notNull(value);
        this.expression = this.expression + value.toString();
        return this;
    }

    public Exp equals(String exp, String key) {
        validExpression(exp);
        AssertUtil.notBlank(key);
        this.expression = GlobalUtil.format("{}@{}({}, {})", this.expression, equals, exp, key);
        return this;
    }

    public Exp notEquals(String exp, String key) {
        validExpression(exp);
        AssertUtil.notBlank(key);
        this.expression = GlobalUtil.format("{}@{}({}, {})", this.expression, notEquals, exp, key);
        return this;
    }

    public Exp isNull(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, isNull, exp);
        return this;
    }

    public Exp isNull(ScopeTypeEnum type, String... keys) {
        return isNull(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp notNull(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, notNull, exp);
        return this;
    }

    public Exp notNull(ScopeTypeEnum type, String... keys) {
        return notNull(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp anyNull(String... exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, anyNull, String.join(", ", exp));
        return this;
    }

    public Exp noneNull(String... exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, noneNull, String.join(", ", exp));
        return this;
    }

    public Exp isBlank(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, isBlank, exp);
        return this;
    }

    public Exp isBlank(ScopeTypeEnum type, String... keys) {
        return isBlank(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp notBlank(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, notBlank, exp);
        return this;
    }

    public Exp notBlank(ScopeTypeEnum type, String... keys) {
        return notBlank(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp noneBlank(String... exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, noneBlank, String.join(", ", exp));
        return this;
    }

    public Exp allBlank(String... exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, allBlank, String.join(", ", exp));
        return this;
    }

    public Exp anyBlank(String... exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, anyBlank, String.join(", ", exp));
        return this;
    }

    public Exp isNumber(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, isNumber, exp);
        return this;
    }

    public Exp isNumber(ScopeTypeEnum type, String... keys) {
        return isNumber(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp toInt(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toInt, exp);
        return this;
    }


    public Exp toInt(ScopeTypeEnum type, String... keys) {
        return toInt(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp toLong(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toLong, exp);
        return this;
    }

    public Exp toLong(ScopeTypeEnum type, String... keys) {
        return toLong(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp toDouble(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toDouble, exp);
        return this;
    }

    public Exp toDouble(ScopeTypeEnum type, String... keys) {
        return toDouble(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp toFloat(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toFloat, exp);
        return this;
    }

    public Exp toFloat(ScopeTypeEnum type, String... keys) {
        return toFloat(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp toByte(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toByte, exp);
        return this;
    }

    public Exp toByte(ScopeTypeEnum type, String... keys) {
        return toByte(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp toShort(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toShort, exp);
        return this;
    }

    public Exp toShort(ScopeTypeEnum type, String... keys) {
        return toShort(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp toBool(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toBool, exp);
        return this;
    }

    public Exp toBool(ScopeTypeEnum type, String... keys) {
        return toBool(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp isTrue(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, isTrue, exp);
        return this;
    }

    public Exp isTrue(ScopeTypeEnum type, String... keys) {
        return isTrue(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp isFalse(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, isFalse, exp);
        return this;
    }

    public Exp isFalse(ScopeTypeEnum type, String... keys) {
        return isFalse(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp notFalse(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, notFalse, exp);
        return this;
    }

    public Exp notFalse(ScopeTypeEnum type, String... keys) {
        return notFalse(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp notTrue(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, notTrue, exp);
        return this;
    }

    public Exp notTrue(ScopeTypeEnum type, String... keys) {
        return notTrue(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp max(String... exp) {
        AssertUtil.anyNotBlank(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, max, String.join(", ", exp));
        return this;
    }

    public Exp min(String... exp) {
        AssertUtil.anyNotBlank(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, min, String.join(", ", exp));
        return this;
    }

    public Exp isEmpty(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, isEmpty, exp);
        return this;
    }

    public Exp isEmpty(ScopeTypeEnum type, String... keys) {
        return isEmpty(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp notEmpty(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, notEmpty, exp);
        return this;
    }

    public Exp notEmpty(ScopeTypeEnum type, String... keys) {
        return notEmpty(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp contains(String coll, String key) {
        AssertUtil.anyNotBlank(coll, key);
        this.expression = GlobalUtil.format("{}@{}({}, {})", this.expression, contains, coll, key);
        return this;
    }

    public Exp size(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, size, exp);
        return this;
    }

    public Exp size(ScopeTypeEnum type, String... keys) {
        return size(KeyUtil.scopeKeyAppend(type, keys));
    }

    public Exp validId(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, validId, exp);
        return this;
    }

    public Exp validId(ScopeTypeEnum type, String... keys) {
        return validId(KeyUtil.scopeKeyAppend(type, keys));
    }

    protected void validExpression(String... expressions) {
        AssertUtil.isTrue(expressions != null && expressions.length > 0, ExceptionEnum.COMPONENT_PARAMS_ERROR, "Empty fetch expressions.");
        assert expressions != null;
        for (String exp : expressions) {
            AssertUtil.isTrue(ElementParserUtil.isValidDataExpression(exp), ExceptionEnum.COMPONENT_PARAMS_ERROR, "Invalid fetch expressions: {}", exp);
        }
    }

    private String build() {
        AssertUtil.notBlank(this.expression);
        if (order != null) {
            return GlobalUtil.format("O{}: {}", order, this.expression);
        }
        return this.expression;
    }

    @Override
    public String toString() {
        return this.expression;
    }
}
