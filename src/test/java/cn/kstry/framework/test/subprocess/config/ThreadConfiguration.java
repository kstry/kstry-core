package cn.kstry.framework.test.subprocess.config;

import cn.kstry.framework.core.constant.GlobalProperties;
import cn.kstry.framework.core.engine.thread.Task;
import cn.kstry.framework.core.engine.thread.TaskThreadPoolExecutor;
import cn.kstry.framework.core.enums.ExecutorType;
import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.exception.KstryException;
import cn.kstry.framework.core.util.GlobalUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class ThreadConfiguration {

    @Bean
    public TaskThreadPoolExecutor taskThreadPoolMethodExecutor() {
        return TaskThreadPoolExecutor.buildTaskExecutor(
                ExecutorType.METHOD,
                "custom-thread-method-invoke",
                32,
                32,
                GlobalProperties.THREAD_POOL_KEEP_ALIVE_TIME, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(GlobalProperties.KSTRY_THREAD_POOL_QUEUE_SIZE),
                new ThreadFactoryBuilder().setNameFormat("custom-thread-method-invoke-%d").setPriority(Thread.MAX_PRIORITY).build(),
                (r, executor) -> {
                    KstryException kstryException = new KstryException(ExceptionEnum.ASYNC_QUEUE_OVERFLOW);
                    String taskName = r.getClass().getName();
                    if (r instanceof Task) {
                        Task<?> task = GlobalUtil.transferNotEmpty(r, Task.class);
                        taskName = task.getTaskName();
                    }
                    log.error(kstryException.getMessage() + " taskName: {}", taskName);
                }
        );
    }
}
