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
@SuppressWarnings({"unchecked", "unused"})
public class Exp<T extends Exp<?>> {

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

    public static <E extends Exp<?>> String b(Consumer<E> builder) {
        return b((E) new Exp<>(), builder);
    }

    protected static <E extends Exp<?>> String b(E instance, Consumer<E> builder) {
        builder.accept(instance);
        return instance.build();
    }

    public T order(int order) {
        this.order = order;
        return (T) this;
    }

    public T and() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " && ";
        return (T) this;
    }

    public T and(String exp) {
        AssertUtil.notBlank(exp);
        this.expression = GlobalUtil.format("({}) && ({})", this.expression, exp);
        return (T) this;
    }

    public T or() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " || ";
        return (T) this;
    }

    public T or(String exp) {
        AssertUtil.notBlank(exp);
        this.expression = GlobalUtil.format("({}) || ({})", this.expression, exp);
        return (T) this;
    }

    public T not(String exp) {
        validExpression(exp);
        this.expression = this.expression + "!" + exp;
        return (T) this;
    }

    public T lt() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " < ";
        return (T) this;
    }

    public T le() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " <= ";
        return (T) this;
    }

    public T gt() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " > ";
        return (T) this;
    }

    public T ge() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " >= ";
        return (T) this;
    }

    public T eq() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " == ";
        return (T) this;
    }

    public T ne() {
        AssertUtil.notBlank(this.expression);
        this.expression = this.expression + " != ";
        return (T) this;
    }

    public T sta(String... keys) {
        AssertUtil.notNull(keys);
        AssertUtil.anyNotBlank(keys);
        this.expression = this.expression + KeyUtil.sta(keys);
        return (T) this;
    }

    public T var(String... keys) {
        AssertUtil.notNull(keys);
        AssertUtil.anyNotBlank(keys);
        this.expression = this.expression + KeyUtil.var(keys);
        return (T) this;
    }

    public T req(String... keys) {
        AssertUtil.notNull(keys);
        AssertUtil.anyNotBlank(keys);
        this.expression = this.expression + KeyUtil.req(keys);
        return (T) this;
    }

    public T res(String... keys) {
        AssertUtil.anyNotBlank(keys);
        this.expression = this.expression + KeyUtil.res(keys);
        return (T) this;
    }

    public T value(Object value) {
        AssertUtil.notNull(value);
        this.expression = this.expression + value.toString();
        return (T) this;
    }

    public T equals(String exp, String key) {
        validExpression(exp);
        AssertUtil.notBlank(key);
        this.expression = GlobalUtil.format("{}@{}({}, {})", this.expression, equals, exp, key);
        return (T) this;
    }

    public T notEquals(String exp, String key) {
        validExpression(exp);
        AssertUtil.notBlank(key);
        this.expression = GlobalUtil.format("{}@{}({}, {})", this.expression, notEquals, exp, key);
        return (T) this;
    }

    public T isNull(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, isNull, exp);
        return (T) this;
    }

    public T isNull(ScopeTypeEnum type, String... keys) {
        return isNull(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T notNull(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, notNull, exp);
        return (T) this;
    }

    public T notNull(ScopeTypeEnum type, String... keys) {
        return notNull(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T anyNull(String... exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, anyNull, String.join(", ", exp));
        return (T) this;
    }

    public T noneNull(String... exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, noneNull, String.join(", ", exp));
        return (T) this;
    }

    public T isBlank(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, isBlank, exp);
        return (T) this;
    }

    public T isBlank(ScopeTypeEnum type, String... keys) {
        return isBlank(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T notBlank(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, notBlank, exp);
        return (T) this;
    }

    public T notBlank(ScopeTypeEnum type, String... keys) {
        return notBlank(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T noneBlank(String... exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, noneBlank, String.join(", ", exp));
        return (T) this;
    }

    public T allBlank(String... exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, allBlank, String.join(", ", exp));
        return (T) this;
    }

    public T anyBlank(String... exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, anyBlank, String.join(", ", exp));
        return (T) this;
    }

    public T isNumber(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, isNumber, exp);
        return (T) this;
    }

    public T isNumber(ScopeTypeEnum type, String... keys) {
        return isNumber(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T toInt(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toInt, exp);
        return (T) this;
    }


    public T toInt(ScopeTypeEnum type, String... keys) {
        return toInt(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T toLong(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toLong, exp);
        return (T) this;
    }

    public T toLong(ScopeTypeEnum type, String... keys) {
        return toLong(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T toDouble(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toDouble, exp);
        return (T) this;
    }

    public T toDouble(ScopeTypeEnum type, String... keys) {
        return toDouble(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T toFloat(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toFloat, exp);
        return (T) this;
    }

    public T toFloat(ScopeTypeEnum type, String... keys) {
        return toFloat(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T toByte(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toByte, exp);
        return (T) this;
    }

    public T toByte(ScopeTypeEnum type, String... keys) {
        return toByte(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T toShort(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toShort, exp);
        return (T) this;
    }

    public T toShort(ScopeTypeEnum type, String... keys) {
        return toShort(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T toBool(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, toBool, exp);
        return (T) this;
    }

    public T toBool(ScopeTypeEnum type, String... keys) {
        return toBool(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T isTrue(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, isTrue, exp);
        return (T) this;
    }

    public T isTrue(ScopeTypeEnum type, String... keys) {
        return isTrue(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T isFalse(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, isFalse, exp);
        return (T) this;
    }

    public T isFalse(ScopeTypeEnum type, String... keys) {
        return isFalse(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T notFalse(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, notFalse, exp);
        return (T) this;
    }

    public T notFalse(ScopeTypeEnum type, String... keys) {
        return notFalse(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T notTrue(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, notTrue, exp);
        return (T) this;
    }

    public T notTrue(ScopeTypeEnum type, String... keys) {
        return notTrue(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T max(String... exp) {
        AssertUtil.anyNotBlank(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, max, String.join(", ", exp));
        return (T) this;
    }

    public T min(String... exp) {
        AssertUtil.anyNotBlank(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, min, String.join(", ", exp));
        return (T) this;
    }

    public T isEmpty(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, isEmpty, exp);
        return (T) this;
    }

    public T isEmpty(ScopeTypeEnum type, String... keys) {
        return isEmpty(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T notEmpty(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, notEmpty, exp);
        return (T) this;
    }

    public T notEmpty(ScopeTypeEnum type, String... keys) {
        return notEmpty(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T contains(String coll, String key) {
        AssertUtil.anyNotBlank(coll, key);
        this.expression = GlobalUtil.format("{}@{}({}, {})", this.expression, contains, coll, key);
        return (T) this;
    }

    public T size(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, size, exp);
        return (T) this;
    }

    public T size(ScopeTypeEnum type, String... keys) {
        return size(KeyUtil.scopeKeyAppend(type, keys));
    }

    public T validId(String exp) {
        validExpression(exp);
        this.expression = GlobalUtil.format("{}@{}({})", this.expression, validId, exp);
        return (T) this;
    }

    public T validId(ScopeTypeEnum type, String... keys) {
        return validId(KeyUtil.scopeKeyAppend(type, keys));
    }

    protected void validExpression(String... expressions) {
        AssertUtil.isTrue(expressions != null && expressions.length > 0, ExceptionEnum.COMPONENT_PARAMS_ERROR, "Empty fetch expressions.");
        assert expressions != null;
        for (String exp : expressions) {
            AssertUtil.isTrue(ElementParserUtil.isValidDataExpression(exp), ExceptionEnum.COMPONENT_PARAMS_ERROR, "Invalid fetch expressions: {}", exp);
        }
    }

    protected String build() {
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
