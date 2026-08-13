package kr.co.dearbloom.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 실행 설정. 현재는 푸시 발송만 쓴다.
 *
 * <p>푸시 전용 풀을 따로 두는 이유는 <b>격리</b>다. 공용 풀을 쓰면 FCM 이 느려질 때 다른 비동기 작업까지
 * 함께 막힌다. 큐가 가득 차면 {@link ThreadPoolExecutor.CallerRunsPolicy} 로 호출 스레드에서 실행하는 대신
 * 버린다({@link ThreadPoolExecutor.DiscardPolicy}) — 알림 하나를 놓치는 편이 비즈니스 스레드를 붙잡는 것보다 낫다.
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {
    public static final String PUSH_EXECUTOR = "pushTaskExecutor";

    @Bean(name = PUSH_EXECUTOR)
    public Executor pushTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("push-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        // 종료 시 발송 중인 건은 마무리하되, 배포가 무한정 지연되지 않게 상한을 둔다.
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();

        return executor;
    }
}
