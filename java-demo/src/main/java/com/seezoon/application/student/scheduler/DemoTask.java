package com.seezoon.application.student.scheduler;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DemoTask {

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    public void doTask() {
        try {
            log.info("start...");
            //
            log.info("end...");
        } catch (Throwable e) {
            log.error("do task error", e);
        }
    }
}
