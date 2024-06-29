package cn.kstry.framework.test.flow.config;

import cn.kstry.framework.core.component.event.TrackingBeginEvent;
import cn.kstry.framework.core.component.event.TrackingFinishEvent;
import com.alibaba.fastjson.JSON;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TrackingEventListener {

    @EventListener
    public void handleTrackingBeginEvent(TrackingBeginEvent event) {
        assert event.getNodeTracking() != null;
//        System.out.println("TrackingBeginEvent：" + JSON.toJSONString(event));
    }

    @EventListener
    public void handleTrackingFinishEvent(TrackingFinishEvent event) {
        assert event.getNodeTracking() != null;
//        System.out.println("TrackingFinishEvent：" + JSON.toJSONString(event));
    }
}
