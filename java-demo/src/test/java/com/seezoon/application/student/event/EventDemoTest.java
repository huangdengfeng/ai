package com.seezoon.application.student.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * {@link EventDemo} 集成测试。
 *
 *
 * <ul>
 *     <li>{@code [sync]} —— 同步监听</li>
 *     <li>{@code [async]} —— 异步监听，必然出现在非主线程</li>
 * </ul>
 */
@SpringBootTest
class EventDemoTest {

    @Autowired
    private EventDemo eventDemo;

    @Test
    public void send() {
        eventDemo.publish("test");
    }
}
