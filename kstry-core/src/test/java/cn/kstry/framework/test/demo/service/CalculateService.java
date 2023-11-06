package cn.kstry.framework.test.demo.service;

import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.core.bus.ScopeDataNotice;
import cn.kstry.framework.test.demo.facade.CommonFields;

@TaskComponent
public class CalculateService {

    @NoticeResult
    @TaskService
    public int plusCalculate(@VarTaskParam int a, @VarTaskParam int b) {
        return a + b;
    }

    @NoticeResult
    @TaskService
    public int multiplyCalculate(@VarTaskParam int a, @VarTaskParam int b) {
        return a * b;
    }

    @NoticeResult
    @TaskService(invoke = @Invoke(timeout = 1000))
    public int minusCalculate(@VarTaskParam int a, @VarTaskParam int b) {
        return a - b;
    }

    @TaskService
    public ScopeDataNotice setCalculateNumber(int a, int b) {
        return ScopeDataNotice.var().notice(CommonFields.F.a, a).notice(CommonFields.F.b, b);
    }

    @NoticeResult
    @TaskService(invoke = @Invoke(retry = 2, demotion = "pr:calculateService@calculateErrorDemotion"))
    public int calculateError() {
        return 1 / 0;
    }

    /**
     * 定义降级方法
     */
    @TaskService
    public int calculateErrorDemotion() {
        return 10;
    }
}
