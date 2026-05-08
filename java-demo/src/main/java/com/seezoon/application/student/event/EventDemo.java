package com.seezoon.application.student.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Spring 应用事件 Demo。
 *
 * <p>展示三种典型监听方式：
 * <ul>
 *     <li>{@link EventListener}：同步监听，与 publish 在同一线程，发布方会等待</li>
 *     <li>{@link EventListener} + {@link Async}：异步监听，需要全局开启 {@code @EnableAsync}（已在 MainApplication 开启）</li>
 * </ul>
 *
 * <p>调用方示例：在业务代码里注入 {@link EventDemo}，调用 {@link #publish(String)} 即可触发；
 * 也可直接注入 {@link ApplicationEventPublisher} 发布 {@link DemoEvent}，本类的监听器同样会被触发。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventDemo {

    private final ApplicationEventPublisher publisher;

    /**
     * 发布事件。
     */
    public void publish(String message) {
        DemoEvent event = DemoEvent.of(message);
        log.info("publish event:{}", event);
        publisher.publishEvent(event);
    }

    @EventListener
    public void onEventSync(DemoEvent event) {
        log.info("[sync] received event:{}, thread:{}", event, Thread.currentThread().getName());
    }

    @Async
    @EventListener
    public void onEventAsync(DemoEvent event) {
        log.info("[async] received event:{}, thread:{}", event, Thread.currentThread().getName());
    }

    /**
     * 事件载荷。Spring 4.2+ 已无需继承 {@code ApplicationEvent}，任意 POJO 即可作为事件。
     */
    public record DemoEvent(String message, long timestamp) {

        public static DemoEvent of(String message) {
            return new DemoEvent(message, System.currentTimeMillis());
        }
    }


}
