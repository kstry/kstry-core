package cn.kstry.framework.test.flow.service;

import cn.kstry.framework.core.annotation.ReqTaskParam;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskInstruct;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.test.flow.bo.CycleRequest;

@TaskComponent
@SuppressWarnings("unused")
public class CycleService {

    @TaskService
    public synchronized void step01(ScopeDataOperator dataOperator, @ReqTaskParam(reqSelf = true) CycleRequest request) {
        dataOperator.serialWrite(opt -> {
            request.setCount(request.getCount() + 1);
            return null;
        });
        dataOperator.setData("var.a", 1);
        assert dataOperator.removeData("var.a");
        assert !dataOperator.getVarData("a").isPresent();
//        System.out.println("step01 -> " + request.getCount());
    }

    @TaskService
    public synchronized void step02(ScopeDataOperator dataOperator, @ReqTaskParam(reqSelf = true) CycleRequest request) {
        dataOperator.serialWrite(opt -> {
            request.setCount(request.getCount() + 1);
            return null;
        });
//        System.out.println("step02 -> " + request.getCount());
    }

    @TaskService
    public synchronized void step03(ScopeDataOperator dataOperator, @ReqTaskParam(reqSelf = true) CycleRequest request) {
        dataOperator.serialWrite(opt -> {
            request.setCount(request.getCount() + 1);
            return null;
        });
//        System.out.println("step03 -> " + request.getCount());
    }

    @TaskService
    @TaskInstruct(name = "step-instruct")
    public synchronized void stepIns(ScopeDataOperator dataOperator, @ReqTaskParam(reqSelf = true) CycleRequest request) {
        dataOperator.serialWrite(opt -> {
            request.setCount(request.getCount() + 1);
            return null;
        });
//        System.out.println("instruct -> " + request.getCount());
    }
}
