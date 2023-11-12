package cn.kstry.framework.test.demo.bo;

import cn.kstry.framework.core.bus.ScopeData;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import lombok.Data;

@Data
public class RuleJudgeRequest implements ScopeData {

    private int a;

    private int b;

    private int c;

    @Override
    public ScopeTypeEnum getScopeDataEnum() {
        return ScopeTypeEnum.VARIABLE;
    }
}
