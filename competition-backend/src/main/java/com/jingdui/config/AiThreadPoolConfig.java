package com.jingdui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * AI 调用专用线程池 —— 避免阻塞主业务线程
 *
 * 核心 5 / 最大 20 / 队列 100 / CallerRunsPolicy
 * 线程名前缀：ai-
 * 空闲线程 60s 回收
 */
@Configuration
public class AiThreadPoolConfig {

    @Bean("aiExecutor")
    public ExecutorService aiExecutor() {
        return new ThreadPoolExecutor(
                5, 20,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private int count = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "ai-" + (++count));
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
