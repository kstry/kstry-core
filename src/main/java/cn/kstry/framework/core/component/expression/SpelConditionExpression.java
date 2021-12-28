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
package cn.kstry.framework.core.component.expression;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.ExpressionException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 *
 * @author lykan
 */
public class SpelConditionExpression extends ConditionExpressionImpl implements ConditionExpression {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    public SpelConditionExpression() {
        super((scopeData, exp) -> {
            if (StringUtils.isBlank(exp) || scopeData == null) {
                return false;
            }
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext(scopeData);
            Boolean value;
            try {
                value = PARSER.parseExpression(exp).getValue(evaluationContext, Boolean.class);
            } catch (Exception e) {
                throw new ExpressionException(ExceptionEnum.EXPRESSION_INVOKE_ERROR, e);
            }
            return BooleanUtils.isTrue(value);
        });
    }

    @Override
    public boolean match(String expression) {
        return true;
    }
}
