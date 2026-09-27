package org.software.open.source.vietnamese.provinces.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@RequiredArgsConstructor // <-- Lombok tự sinh constructor
@Import(TestcontainersConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL) // <-- Yêu cầu Spring tự động Autowired
class VietnameseProvincesSeviceApplicationTests {

  @Test
  void contextLoads() {
  }

}
