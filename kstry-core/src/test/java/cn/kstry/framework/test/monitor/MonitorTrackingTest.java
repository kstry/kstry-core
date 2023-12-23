package cn.kstry.framework.test.monitor;

import cn.kstry.framework.core.bpmn.StartEvent;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import cn.kstry.framework.core.monitor.MonitorTracking;
import cn.kstry.framework.core.monitor.NodeTracking;
import cn.kstry.framework.core.monitor.SerializeTracking;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 链路监控测试
 *
 * @author xiongzhongwei<xiongzhongwei @ bytedance.com>
 * @date 12/21/2023 9:40 PM
 */
public class MonitorTrackingTest {


    /**
     * Test the node monitoring for success
     */
    @Test
    public void testTrackingLogSuccessfulLog() {
        // Mock FlowElement
        StartEvent startEvent = Mockito.mock(StartEvent.class);
        Mockito.when(startEvent.getId()).thenReturn("testId1");
        SerializeTracking serializeTracking = Mockito.mock(SerializeTracking.class);

        MonitorTracking monitorTracking = new MonitorTracking(
                startEvent, TrackingTypeEnum.ALL, serializeTracking
        );
        monitorTracking.buildNodeTracking(startEvent);
        // Act tracking next element
        monitorTracking.trackingNextElement(startEvent);

        // Act  finishTaskTracking
        monitorTracking.finishTaskTracking(startEvent, null);

        // Act trackingLog
        monitorTracking.trackingLog();

        NodeTracking nodeTracking = monitorTracking.getStoryTracking().get(0);

        Assert.assertNotNull(nodeTracking.getEndTime());

        Assert.assertNotSame(nodeTracking.getSpendTime(), monitorTracking.getSpendTime());

        Assert.assertTrue(nodeTracking.getSpendTime() <= monitorTracking.getSpendTime());
    }
}
