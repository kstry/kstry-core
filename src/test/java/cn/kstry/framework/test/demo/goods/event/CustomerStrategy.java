package cn.kstry.framework.test.demo.goods.event;

import cn.kstry.framework.core.route.calculate.StrategyRuleCalculator;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

/**
 *
 * @author lykan
 */
@Component
public class CustomerStrategy implements StrategyRuleCalculator {

    @Override
    public boolean calculate(Object source, Object expected) {
        return source != null && NumberUtils.toInt(source.toString(), 0) > 0;
    }

    @Override
    public boolean checkExpected(String expected) {
        return true;
    }

    @Override
    public String getCalculatorName() {
        return "typeCheck";
    }
}
