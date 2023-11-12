package cn.kstry.flux.demo.web;

import cn.kstry.flux.demo.dto.student.Student;
import cn.kstry.flux.demo.facade.R;
import cn.kstry.flux.demo.util.WebUtil;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/action")
public class HttpActionController {

    @Resource
    private StoryEngine storyEngine;

    @PostMapping("/queryStudent")
    public Mono<R<Student>> login() {
        StoryRequest<Student> fireRequest = ReqBuilder.returnType(Student.class).resultBuilder((res, query) -> {
            Optional<Student> data = query.getData("$.var.student.data");
            return data.orElse(null);
        }).recallStoryHook(WebUtil::recallStoryHook).trackingType(TrackingTypeEnum.SERVICE_DETAIL).timeout(10000).startId("http-action-test001").build();
        Mono<Student> fireAsync = storyEngine.fireAsync(fireRequest);
        return WebUtil.dataDecorate(null, fireAsync);
    }

    @PostMapping("/askGotoSchool")
    public Mono<R<Boolean>> askGotoSchool() {
        StoryRequest<Boolean> fireRequest = ReqBuilder.returnType(Boolean.class).recallStoryHook(WebUtil::recallStoryHook)
                .trackingType(TrackingTypeEnum.SERVICE_DETAIL).startId("intelligent-sop-flow-demo").build();
        Mono<Boolean> fireAsync = storyEngine.fireAsync(fireRequest);
        return WebUtil.dataDecorate(null, fireAsync);
    }
}
