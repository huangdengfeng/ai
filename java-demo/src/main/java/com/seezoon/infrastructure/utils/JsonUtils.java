package com.seezoon.infrastructure.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * json 处理 spring 环境下使用
 *
 * @author huangdengfeng
 * @date 2023/1/5 10:37
 */
@Slf4j
public class JsonUtils {

    private static final JsonMapper JSON_MAPPER;

    static {
        JSON_MAPPER = JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
    }


    /**
     * java 对象转json
     *
     * @param o
     * @return
     */
    public static String toJson(Object o) {
        try {
            return JSON_MAPPER.writeValueAsString(o);
        } catch (JacksonException e) {
            log.error("to json error", e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            throw new IllegalArgumentException("json is empty");
        }
        try {
            return JSON_MAPPER.readValue(json, clazz);
        } catch (JacksonException e) {
            log.error("fromJson error", e);
            throw new RuntimeException(e);
        }
    }

}
