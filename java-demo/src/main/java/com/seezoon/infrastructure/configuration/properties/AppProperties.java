package com.seezoon.infrastructure.configuration.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * 业务应用配置
 *
 * @author huangdengfeng
 * @date 2023/9/8 16:29
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    @Valid
    @NotNull
    @NestedConfigurationProperty
    private CorsProperties cors = new CorsProperties();
}
