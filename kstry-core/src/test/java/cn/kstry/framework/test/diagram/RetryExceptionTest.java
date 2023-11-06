package cn.kstry.framework.test.diagram;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.test.diagram.config.RetryExceptionDiagramConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.atomic.AtomicInteger;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DiagramCaseTestContextConfiguration.class)
public class RetryExceptionTest {

    @Autowired
    private StoryEngine storyEngine;

    @Test
    public void testDiagram001() {
        AtomicInteger req = new AtomicInteger();
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).request(req)
                .trackingType(TrackingTypeEnum.SERVICE).startProcess(RetryExceptionDiagramConfiguration::retryExceptionProcess1).build();
        storyEngine.fire(fireRequest);
        Assert.assertEquals(4, req.get());
    }

    @Test
    public void testDiagram002() {
        AtomicInteger req = new AtomicInteger();
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).request(req)
                .trackingType(TrackingTypeEnum.SERVICE).startProcess(RetryExceptionDiagramConfiguration::retryExceptionProcess2).build();
        storyEngine.fire(fireRequest);
        Assert.assertEquals(4, req.get());
    }

    @Test
    public void testDiagram003() {
        AtomicInteger req = new AtomicInteger();
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).request(req)
                .trackingType(TrackingTypeEnum.SERVICE).startProcess(RetryExceptionDiagramConfiguration::retryExceptionProcess3).build();
        storyEngine.fire(fireRequest);
        Assert.assertEquals(1, req.get());
    }

    @Test
    public void testDiagram004() {
        AtomicInteger req = new AtomicInteger();
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).request(req)
                .trackingType(TrackingTypeEnum.SERVICE).startProcess(RetryExceptionDiagramConfiguration::retryExceptionProcess4).build();
        storyEngine.fire(fireRequest);
        Assert.assertEquals(1, req.get());
    }

    @Test
    public void testDiagram005() {
        AtomicInteger req = new AtomicInteger();
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).request(req)
                .trackingType(TrackingTypeEnum.SERVICE).startProcess(RetryExceptionDiagramConfiguration::retryExceptionProcess5).build();
        storyEngine.fire(fireRequest);
        Assert.assertEquals(1, req.get());
    }

    @Test
    public void testDiagram006() {
        AtomicInteger req = new AtomicInteger();
        StoryRequest<Void> fireRequest = ReqBuilder.returnType(Void.class).request(req).timeout(3000)
                .trackingType(TrackingTypeEnum.SERVICE).startProcess(RetryExceptionDiagramConfiguration::retryExceptionProcess6).build();
        storyEngine.fire(fireRequest);
        Assert.assertEquals(4, req.get());
    }
}
