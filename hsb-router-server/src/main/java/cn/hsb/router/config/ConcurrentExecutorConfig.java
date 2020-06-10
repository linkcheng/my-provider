package cn.hsb.router.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
@EnableConfigurationProperties({ExecutorProperties.class})
@ConditionalOnProperty(prefix = "executor.config", value = {"corePoolSize", "maxPoolSize", "keepAliveSeconds", "queueCapacity"})
public class ConcurrentExecutorConfig {
    private ExecutorProperties properties;

    @Autowired
    public void setProperties(ExecutorProperties properties) {
        this.properties = properties;
    }

    @Bean("taskConcurrentExecutor")
    public Executor taskConcurrentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程池大小
        executor.setCorePoolSize(properties.getCorePoolSize());
        //最大线程数
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        //队列容量
        executor.setQueueCapacity(properties.getQueueCapacity());
        //活跃时间
        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
        //线程名字前缀
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        // AbortPolicy：当任务添加到线程池中被拒绝时，它将抛出 RejectedExecutionException 异常
        // CallerRunsPolicy：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}