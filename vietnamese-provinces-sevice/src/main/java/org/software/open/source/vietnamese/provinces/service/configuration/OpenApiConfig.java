package org.software.open.source.vietnamese.provinces.service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI().info(new Info().title("Vietnamese Provinces Sevice").version("1.0.0")
        .description("API documentation for Vietnamese Provinces Sevice")
        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")));
  }

}
