package cn.kstry.framework.test.demo.config;

import cn.kstry.framework.core.component.expression.ExpressionAlias;
import cn.kstry.framework.core.component.expression.ExpressionAliasRegister;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class CustomExpressionFunction implements ExpressionAliasRegister {

    @Override
    public List<ExpressionAlias> registerAlias() {
        return Lists.newArrayList(new ExpressionAlias("eq", Objects.class, "equals"));
    }
}
