package org.software.open.source.vietnamese.citizen.service;

import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import jakarta.annotation.PreDestroy;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

  private PostgreSQLContainer postgresContainer;

  @Bean
  PostgreSQLContainer postgresContainer() {
    if (postgresContainer == null) {
      postgresContainer = new PostgreSQLContainer("postgres:latest");
      postgresContainer.start();
    }
    return postgresContainer;
  }

  @Bean
  @Primary
  public DataSource dataSource(PostgreSQLContainer postgresContainer) {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
    dataSource.setUrl(postgresContainer.getJdbcUrl());
    dataSource.setUsername(postgresContainer.getUsername());
    dataSource.setPassword(postgresContainer.getPassword());
    return dataSource;
  }

  @PreDestroy // ← Spring tự gọi method này
  void cleanup() {
    if (postgresContainer != null && postgresContainer.isRunning()) {
      postgresContainer.stop();
    }
  }

}
