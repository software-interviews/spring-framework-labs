package org.software.open.source.vietnamese.citizen.service.entries.jobs;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.software.open.source.vietnamese.citizen.service.entries.models.external.clients.ProvinceClient;
import org.software.open.source.vietnamese.citizen.service.entries.models.external.dto.ProvinceApiDto;
import org.software.open.source.vietnamese.citizen.service.entries.models.requests.AddressRequest;
import org.software.open.source.vietnamese.citizen.service.entries.models.requests.CitizenRequest;
import org.software.open.source.vietnamese.citizen.service.services.CitizenService;
import org.software.open.source.vietnamese.citizen.service.utils.FakeBirthday;
import org.software.open.source.vietnamese.citizen.service.utils.FakeCitizen;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitizenSeederJob {

  private final CitizenService citizenService;
  private final ProvinceClient provinceClient;

  @Value("${app.seeder-batch.size:1000}")
  private int batchSize;

  @Value("${app.seeder-batch.enabled:false}")
  private boolean enabled;

  /**
   * Chạy mỗi 2 phút (120_000 ms).
   * Mỗi lần chạy spawn ra 10000 virtual thread để tạo citizen song song.
   */
  @Scheduled(fixedRate = 120_000)
  public void seedCitizens() {
    if (!enabled) {
      return;
    }

    List<ProvinceApiDto.Province> provinces = provinceClient.getProvincesWithWards();
    if (provinces.isEmpty()) {
      log.warn("[Seeder] Không có dữ liệu provinces, bỏ qua lượt này");
      return;
    }

    Instant start = Instant.now();
    log.info("  Bắt đầu seed {} citizen bằng VIRTUAL THREAD", batchSize);

    AtomicInteger success = new AtomicInteger();
    AtomicInteger failed = new AtomicInteger();

    // Virtual Thread Executor: mỗi task được chạy trên 1 virtual thread riêng
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

      for (int i = 0; i < batchSize; i++) {
        executor.submit(() -> createOneCitizen(provinces, success, failed));
      }

    } // try-with-resources tự động gọi executor.close() → đợi TẤT CẢ virtual thread
      // hoàn thành

    Duration elapsed = Duration.between(start, Instant.now());

    log.info("═══════════════════════════════════════════════════════════");
    log.info("  ✓ Hoàn tất seed citizen");
    log.info("    • Thành công : {}", success.get());
    log.info("    • Thất bại   : {}", failed.get());
    log.info("    • Thời gian  : {} ms ({} giây)",
        elapsed.toMillis(), elapsed.toSeconds());
    log.info("    • Throughput : ~{}/giây",
        elapsed.toSeconds() > 0 ? success.get() / elapsed.toSeconds() : success.get());
    log.info("═══════════════════════════════════════════════════════════");
  }

  /**
   * Logic tạo 1 citizen — chạy trong 1 virtual thread.
   */
  private void createOneCitizen(List<ProvinceApiDto.Province> provinces,
      AtomicInteger success,
      AtomicInteger failed) {
    try {
      CitizenRequest request = buildFakeCitizenRequest(provinces);
      citizenService.createCitizen(request);
      success.incrementAndGet();
    } catch (Exception e) {
      int currentFail = failed.incrementAndGet();
      // Chỉ log 10 lỗi đầu tiên để tránh spam console
      if (currentFail <= 10) {
        log.error("[Seeder] Lỗi khi tạo citizen: {}", e.getMessage());
      } else if (currentFail == 11) {
        log.warn("[Seeder] Các lỗi tiếp theo sẽ được im lặng để tránh spam...");
      }
    }
  }

  /**
   * Build 1 CitizenRequest giả lập với 1-3 addresses ngẫu nhiên.
   */
  private CitizenRequest buildFakeCitizenRequest(List<ProvinceApiDto.Province> provinces) {
    String firstName = FakeCitizen.getFirstName();
    String lastName = FakeCitizen.getLastName();
    LocalDate birthday = FakeBirthday.getBirthday(18, 80);

    int addressCount = ThreadLocalRandom.current().nextInt(1, 4); // 1-3 địa chỉ
    List<AddressRequest> addresses = new java.util.ArrayList<>(addressCount);
    for (int i = 0; i < addressCount; i++) {
      addresses.add(buildFakeAddress(provinces));
    }

    return CitizenRequest.builder()
        .firstName(firstName)
        .lastName(lastName)
        // fullName sẽ được CitizenMapper tự ghép: lastName + " " + firstName
        .birthday(birthday)
        .addresses(addresses)
        .build();
  }

  /**
   * Build 1 AddressRequest giả lập từ province + ward random.
   */
  private AddressRequest buildFakeAddress(List<ProvinceApiDto.Province> provinces) {
    ProvinceApiDto.Province province = provinces.get(ThreadLocalRandom.current().nextInt(provinces.size()));
    List<ProvinceApiDto.Ward> wards = province.wards();
    ProvinceApiDto.Ward ward = wards.get(ThreadLocalRandom.current().nextInt(wards.size()));

    String addressType = ThreadLocalRandom.current().nextBoolean() ? "HOME" : "WORK";

    return AddressRequest.builder()
        .citizenId(null) // Service sẽ tự sinh UUID và gán
        .detail("Số " + ThreadLocalRandom.current().nextInt(1, 999) + " Trường Sa")
        .ward(ward.name())
        .province(province.name())
        .addressType(addressType)
        .build();
  }
}
