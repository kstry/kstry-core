package cn.kstry.framework.test.demo.config;

import cn.kstry.framework.core.component.expression.Exp;
import cn.kstry.framework.core.util.GlobalUtil;

import java.util.function.Consumer;

/**
 * 扩展工具类
 */
public class Ex extends Exp<Ex> {

    public Ex customEquals(String left, String right) {
        this.expression = GlobalUtil.format("{}@eq({}, {})", this.expression, left, right);
        return this;
    }

    public static String bu(Consumer<Ex> builder) {
        return b(new Ex(), builder);
    }
}
