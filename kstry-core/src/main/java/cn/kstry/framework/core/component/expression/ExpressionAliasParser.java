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

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.Ordered;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表达式别名解析器
 *
 * @author lykan
 */
public class ExpressionAliasParser {

    private final static Pattern itemPattern = Pattern.compile("\\.[\\w-]+");

    /**
     * 表达式别名列表
     */
    private final List<ExpressionAlias> expressionAliasList;

    public ExpressionAliasParser(List<ExpressionAliasRegister> registerList) {
        List<ExpressionAlias> aList = Lists.newArrayList();
        for (ExpressionAliasRegister register : registerList) {
            List<ExpressionAlias> aliases = register.registerAlias();
            if (CollectionUtils.isEmpty(aliases)) {
                continue;
            }
            aliases.forEach(alias -> {
                for (ExpressionAlias expressionAlias : aList) {
                    if (!Objects.equals(alias.getAlias(), expressionAlias.getAlias())) {
                        continue;
                    }
                    AssertUtil.equals(alias.getStaticMethodSign(), expressionAlias.getStaticMethodSign(), ExceptionEnum.COMPONENT_PARAMS_ERROR,
                            "Expression aliases are not allowed to be registered repeatedly. methodSign1: {}, methodSign2: {}, alias: {}",
                            alias.getStaticMethodSign(), expressionAlias.getStaticMethodSign(), alias.getAlias());
                }
                aList.add(alias);
            });
        }
        this.expressionAliasList = Collections.unmodifiableList(aList);
    }

    public String parseExpression(String expression) {
        if (StringUtils.isBlank(expression)) {
            return expression;
        }
        String expFinal = expression;
        Matcher matcher = itemPattern.matcher(expFinal);
        while (matcher.find()) {
            String group = matcher.group();
            int endIndex = matcher.end();
            for (; endIndex <= expFinal.length(); endIndex++) {
                if (endIndex < expFinal.length() && expFinal.charAt(endIndex) == ' ') {
                    continue;
                }
                if (endIndex == expFinal.length() || !Objects.equals(expFinal.charAt(endIndex), '(')) {
                    expression = StringUtils.replaceOnce(expression, group, GlobalUtil.format("['{}']", group.substring(1)));
                }
                break;
            }
        }
        for (ExpressionAlias expressionAlias : expressionAliasList) {
            expression = expression.replaceAll("@" + expressionAlias.getAlias() + "\\s*\\(", expressionAlias.getStaticMethodSign() + "(");
        }
        return expression;
    }

    public static Pair<Integer, String> parseExpressionOrder(String expression) {
        String[] split = expression.split(":", 2);
        if (split.length == 1) {
            return ImmutablePair.of(Ordered.LOWEST_PRECEDENCE, expression);
        }
        String left = StringUtils.trim(split[0]);
        String right = StringUtils.trim(split[1]);
        char fChar = left.charAt(0);
        if (fChar != 'o' && fChar != 'O') {
            return ImmutablePair.of(Ordered.LOWEST_PRECEDENCE, expression);
        }
        left = StringUtils.trim(left.substring(1));
        if (!NumberUtils.isCreatable(left)) {
            return ImmutablePair.of(Ordered.LOWEST_PRECEDENCE, expression);
        }
        return ImmutablePair.of(NumberUtils.toInt(left), right);
    }
}
