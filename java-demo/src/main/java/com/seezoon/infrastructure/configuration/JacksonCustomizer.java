package com.seezoon.infrastructure.configuration;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.jdk.StringDeserializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * jackson configuration
 */
@Configuration
public class JacksonCustomizer {

    @Bean
    JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> {
            // 注册自定义字符串反序列化器（实现 trim 空格）
            SimpleModule trimModule = new SimpleModule();
            trimModule.addDeserializer(String.class, new TrimStringDeserializer());
            builder.addModule(trimModule);
        };
    }

    /**
     * 自定义字符串反序列化器：实现自动 trim 空格
     */
    static class TrimStringDeserializer extends StringDeserializer {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) {
            // 先调用父类获取原始字符串，再 trim 空格（null 时直接返回 null）
            String value = super.deserialize(p, ctxt);
            return value == null ? null : value.trim();
        }
    }
}
