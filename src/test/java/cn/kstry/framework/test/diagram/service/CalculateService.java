package cn.kstry.framework.test.diagram.service;

import cn.kstry.framework.core.annotation.*;
import cn.kstry.framework.core.bus.ScopeDataNotice;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.enums.ScopeTypeEnum;
import cn.kstry.framework.test.diagram.bo.CalculateServiceRequest;
import cn.kstry.framework.test.diagram.constants.SCS;
import com.google.common.collect.Lists;
import org.junit.Assert;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("unused")
@TaskComponent(name = SCS.F.CALCULATE_SERVICE)
public class CalculateService {

    @TaskService(name = SCS.CALCULATE_SERVICE.F.INCREASE_ONE)
    public void increaseOne(ScopeDataOperator operator) {
        ReentrantReadWriteLock.WriteLock writeLock = operator.writeLock();
        writeLock.lock();
        try {
            CalculateServiceRequest reqScope = operator.getReqScope();
            reqScope.setA(reqScope.getA() + 1);
        } finally {
            writeLock.unlock();
        }
    }

    @TaskService(name = SCS.CALCULATE_SERVICE.F.INCREASE_TIMEOUT)
    public void increaseTimeout(@ReqTaskParam(reqSelf = true) CalculateServiceRequest request) throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
    }

    @TaskService(name = SCS.CALCULATE_SERVICE.F.MULTIPLY_PLUS)
    public ScopeDataNotice multiply(@StaTaskParam("ai") int a, @StaTaskParam("bi") int b, @StaTaskParam("ci") int c) {
        return ScopeDataNotice.res((a * b) + c);
    }

    @TaskService(name = SCS.CALCULATE_SERVICE.F.INCREASE_ARRAY_ONE)
    public void increaseArrayOne(ScopeDataOperator operator) {
        Assert.assertEquals("test-prop", operator.getTaskProperty().orElse(null));
        ReentrantReadWriteLock.WriteLock writeLock = operator.writeLock();
        writeLock.lock();
        try {
            Optional<Integer> iterDataItem = operator.iterDataItem();
            iterDataItem.ifPresent(i ->
                    operator.computeIfAbsent(ScopeTypeEnum.RESULT.getKey(), Lists::newArrayList).ifPresent(list -> list.add(i + 1))
            );
        } finally {
            writeLock.unlock();
        }
    }

    @NoticeResult
    @TaskService(name = SCS.CALCULATE_SERVICE.F.CALCULATE_ERROR, invoke = @Invoke(demotion = "pr:" + SCS.F.CALCULATE_SERVICE + "@" + SCS.CALCULATE_SERVICE.F.CALCULATE_ERROR_DEMOTION))
    public int calculateError(@ReqTaskParam(reqSelf = true) CalculateServiceRequest request) {
        int i = 1 / request.getA();
        return 0;
    }


    @TaskService(name = SCS.CALCULATE_SERVICE.F.CALCULATE_ERROR_DEMOTION)
    public int calculateErrorDemotion(@ReqTaskParam("a") int a) {
        return -1;
    }
}
