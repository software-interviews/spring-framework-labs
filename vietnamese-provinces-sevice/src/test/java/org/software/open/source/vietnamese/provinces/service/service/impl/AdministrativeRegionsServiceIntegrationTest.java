package org.software.open.source.vietnamese.provinces.service.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.software.open.source.vietnamese.provinces.service.TestcontainersConfiguration;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.AdministrativeRegionsResponse;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.PageResponse;
import org.software.open.source.vietnamese.provinces.service.service.AdministrativeRegionsService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(properties = {
    // Test KHÔNG đọc file .env, nên phải truyền tay đúng các cấu hình Liquibase
    // giống .env
    "spring.liquibase.enabled=true",
    "spring.liquibase.change-log=classpath:/db/changelog/changelog.yaml",
    "spring.liquibase.contexts=dev" // khớp SPRING_LIQUIBASE_CONTEXTS → changeset insert data mới chạy
})
@RequiredArgsConstructor // <-- Lombok tự sinh constructor
@Import(TestcontainersConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL) // <-- Yêu cầu Spring tự động Autowired
class AdministrativeRegionsServiceIntegrationTest {

  private final AdministrativeRegionsService administrativeRegionsService;

  // KHÔNG cần @BeforeEach insert data nữa!
  // Lúc Spring context khởi động, Liquibase đã chạy:
  // 01_db_init.sql → tạo bảng
  // 02_insert_data.sql → seed 8 regions + 5 units + provinces
  // lên đúng con Postgres Testcontainers.

  @Test
  void whenSearchIsEmpty_shouldReturnAllSeededRegions() {
    // Act
    PageResponse<AdministrativeRegionsResponse> result = administrativeRegionsService.getAdministrativeRegions(null, 0,
        10, "id", "asc");

    // Assert: đúng 8 vùng miền trong 02_insert_data.sql
    assertThat(result.getContent()).hasSize(8);
    assertThat(result.getContent().get(0).getName()).isEqualTo("Đông Bắc Bộ"); // id = 1
  }

  @Test
  void whenSearchByName_shouldReturnOnlyMatchingRegions() {
    // Act: "Bắc" khớp 3 dòng INSERT có chứa chữ "Bắc" trong name
    PageResponse<AdministrativeRegionsResponse> result = administrativeRegionsService.getAdministrativeRegions("Bắc", 0,
        10, "id", "asc");

    // Assert: Đông Bắc Bộ, Tây Bắc Bộ, Bắc Trung Bộ
    assertThat(result.getContent()).hasSize(3);
    assertThat(result.getContent())
        .allSatisfy(region -> assertThat(region.getName()).contains("Bắc"));
  }

  @Test
  void whenSearchByNameEn_shouldReturnMatchingRegion() {
    // Act: search theo tên tiếng Anh có trong seed data
    PageResponse<AdministrativeRegionsResponse> result = administrativeRegionsService.getAdministrativeRegions("Mekong",
        0, 10, "id", "asc");

    // Assert: chỉ "Đồng bằng sông Cửu Long"
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("Đồng bằng sông Cửu Long");
  }

  @Test
  void whenPaging_shouldSliceSeededDataCorrectly() {
    // Act: 8 dòng seed, size = 3 → 3 trang (3 + 3 + 2)
    PageResponse<AdministrativeRegionsResponse> page0 = administrativeRegionsService.getAdministrativeRegions(null, 0,
        3, "id", "asc");
    PageResponse<AdministrativeRegionsResponse> page2 = administrativeRegionsService.getAdministrativeRegions(null, 2,
        3, "id", "asc");

    // Assert
    assertThat(page0.getContent()).hasSize(3);
    assertThat(page2.getContent()).hasSize(2);
  }

  @Test
  void whenSortDescById_shouldReturnReversedSeedOrder() {
    // Act
    PageResponse<AdministrativeRegionsResponse> result = administrativeRegionsService.getAdministrativeRegions(null, 0,
        10, "id", "desc");

    // Assert: id lớn nhất trong seed là 8
    assertThat(result.getContent().get(0).getName()).isEqualTo("Đồng bằng sông Cửu Long");
  }

  @Test
  void whenPageAndSizeInvalid_shouldFallbackAndStillReturnSeedData() {
    // Act: page/size âm → Helper chỉnh về min, không được crash
    PageResponse<AdministrativeRegionsResponse> result = administrativeRegionsService.getAdministrativeRegions(null, -5,
        -1, "id", "asc");

    // Assert
    assertThat(result.getContent()).isNotEmpty();

  }

}
