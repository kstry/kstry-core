/*
 *
 *  *  Copyright (c) 2020-2021, Lykan (jiashuomeng@gmail.com).
 *  *  <p>
 *  *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *  <p>
 *  * https://www.gnu.org/licenses/lgpl.html
 *  *  <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.engine.timeslot;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * time slot thread pool executor
 *
 * @author lykan
 */
public class TimeSlotThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSlotThreadPoolExecutor.class);

    @SuppressWarnings("unused")
    public TimeSlotThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @SuppressWarnings("unused")
    public TimeSlotThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    @SuppressWarnings("unused")
    public TimeSlotThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                      RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    @SuppressWarnings("unused")
    public TimeSlotThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                                      RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public static TimeSlotThreadPoolExecutor buildDefaultExecutor() {
        return new TimeSlotThreadPoolExecutor(
                5,
                10,
                10, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(5000),
                new ThreadFactoryBuilder().setNameFormat("timeslot-thread-pool-%d").build(),
                (r, executor) -> {
                    if (!(r instanceof TimeSlotAsyncTask)) {
                        LOGGER.error("[{}] Thread pool container queue is full, discard task!", ExceptionEnum.CONTAINER_QUEUE_FULL_ERROR.getExceptionCode());
                        return;
                    }
                    TimeSlotAsyncTask asyncTask = (TimeSlotAsyncTask) r;
                    TimeSlotInvokeRequest timeSlotInvokeRequest = GlobalUtil.notNull(asyncTask.getTimeSlotInvokeRequest());
                    LOGGER.error("[{}] Thread pool container queue is full, discard task! strategyName:{}",
                            ExceptionEnum.CONTAINER_QUEUE_FULL_ERROR.getExceptionCode(), timeSlotInvokeRequest.getStrategyName());
                }
        );
    }
}
