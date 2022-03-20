package cn.kstry.framework.test.flow.service;

import cn.kstry.framework.core.annotation.ReqTaskParam;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.task.TaskComponentRegister;
import cn.kstry.framework.test.flow.bo.Hospital;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@SuppressWarnings("unused")
public class HospitalService implements TaskComponentRegister {

    @TaskService(name = "get_hospital", targetType = Hospital.class)
    public Mono<Hospital> getHospital(@ReqTaskParam("hospitalId") Long id) {
        Hospital hospital = new Hospital();
        hospital.setId(id);
        hospital.setName("机构名称");
        System.out.println("thread ->" + Thread.currentThread().getName() + ", get hospital ->" + JSON.toJSONString(hospital));
        CompletableFuture<Hospital> hospitalCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return hospital;
        });
        return Mono.fromFuture(hospitalCompletableFuture);
    }

    @Override
    public String getName() {
        return "hospital_service";
    }
}
