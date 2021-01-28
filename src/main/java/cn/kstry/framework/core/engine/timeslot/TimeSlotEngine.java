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

import cn.kstry.framework.core.annotation.IgnoreEventNode;
import cn.kstry.framework.core.config.GlobalConstant;
import cn.kstry.framework.core.enums.KstryTypeEnum;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.facade.TaskResponse;
import cn.kstry.framework.core.operator.EventOperatorRole;
import cn.kstry.framework.core.route.RouteEventGroup;
import cn.kstry.framework.core.util.AssertUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * TimeSlot 任务执行 Engine
 * @author lykan
 */
public class TimeSlotEngine extends RouteEventGroup implements TimeSlotOperatorRole, ApplicationContextAware, ApplicationListener<ContextClosedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSlotEngine.class);

    /**
     * 执行 time slot 任务的线程池
     */
    private TimeSlotThreadPoolExecutor threadPoolExecutor;

    @Override
    public TaskResponse<Map<String, Object>> invoke(TimeSlotInvokeRequest request) {

        AssertUtil.notNull(request);
        TimeSlotAsyncTask timeSlotAsyncTask = new TimeSlotAsyncTask(request);
        if (!request.isAsync()) {
            TimeSlotTaskResponse taskResponse = new TimeSlotTaskResponse(null);
            TaskResponse<Map<String, Object>> syncTaskResponse = timeSlotAsyncTask.call();
            BeanUtils.copyProperties(syncTaskResponse, taskResponse);
            taskResponse.setStrategyName(request.getStrategyName());
            return taskResponse;
        }

        AssertUtil.notNull(threadPoolExecutor, ExceptionEnum.THREAD_POOL_COUNT_ERROR);
        Future<TaskResponse<Map<String, Object>>> futureTask = threadPoolExecutor.submit(timeSlotAsyncTask);
        TimeSlotTaskResponse taskResponse = new TimeSlotTaskResponse(futureTask);
        taskResponse.resultSuccess();
        taskResponse.setTimeout(request.getTimeout());
        taskResponse.setStrategyName(request.getStrategyName());
        return taskResponse;
    }

    @Override
    @IgnoreEventNode
    public void onApplicationEvent(ContextClosedEvent event) {
        LOGGER.info("begin shutdown time slot thread pool!");
        threadPoolExecutor.shutdown();
        try {
            TimeUnit.SECONDS.sleep(GlobalConstant.ENGINE_SHUTDOWN_SLEEP_SECONDS);
        } catch (Exception e) {
            LOGGER.warn("time slot thread pool close task are interrupted on shutdown!", e);
        }
        if (threadPoolExecutor.isShutdown() && threadPoolExecutor.getActiveCount() == 0) {
            LOGGER.info("[shutdown] interrupting tasks in the thread pool success! thread pool close success!");
            return;
        } else {
            LOGGER.info("interrupting tasks in the thread pool that have not yet finished! begin shutdownNow!");
            threadPoolExecutor.shutdownNow();
        }
        try {
            TimeUnit.SECONDS.sleep(GlobalConstant.ENGINE_SHUTDOWN_NOW_SLEEP_SECONDS);
        } catch (Exception e) {
            LOGGER.warn("time slot thread pool close task are interrupted on shutdown!", e);
        }
        if (threadPoolExecutor.isShutdown() && threadPoolExecutor.getActiveCount() == 0) {
            LOGGER.info("[shutdownNow] interrupting tasks in the thread pool success! thread pool close success!");
        } else {
            LOGGER.error("[shutdownNow] interrupting tasks in the thread pool error! thread pool close error!");
        }
    }

    @Override
    @IgnoreEventNode
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, TimeSlotThreadPoolExecutor> threadPoolExecutorMap = applicationContext.getBeansOfType(TimeSlotThreadPoolExecutor.class);
        if (MapUtils.isNotEmpty(threadPoolExecutorMap)) {
            AssertUtil.oneSize(threadPoolExecutorMap.keySet(), ExceptionEnum.THREAD_POOL_COUNT_ERROR);
            this.threadPoolExecutor = threadPoolExecutorMap.values().iterator().next();
            return;
        }

        this.threadPoolExecutor = TimeSlotThreadPoolExecutor.buildDefaultExecutor();
    }

    @Override
    public String getEventGroupName() {
        return TIME_SLOT_TASK_NAME;
    }

    @Override
    public KstryTypeEnum getEventGroupTypeEnum() {
        return KstryTypeEnum.TIME_SLOT;
    }

    @Override
    public Class<? extends EventOperatorRole> getOperatorRoleClass() {
        return TimeSlotOperatorRole.class;
    }
}
