package com.bwd4.mootd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class SchedulerConfig {
    @Bean
    public Scheduler asyncScheduler() {
        return Schedulers.boundedElastic(); // 백그라운드에서 실행될 스레드 풀
    }
}
