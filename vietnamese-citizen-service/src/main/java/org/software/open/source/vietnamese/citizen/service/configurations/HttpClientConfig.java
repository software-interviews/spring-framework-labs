package org.software.open.source.vietnamese.citizen.service.configurations;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

  @Bean
  public HttpClient httpClient() {
    return HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1) // Hoặc HTTP_2 nếu server hỗ trợ
        .connectTimeout(Duration.ofSeconds(10))
        .build();
  }
}
