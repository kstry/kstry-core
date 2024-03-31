/*
 *  * Copyright (c) 2020-2024, xiongzhongwei (xiongzhongwei@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express   or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 */
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
 * @author xiongzhongwei
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
