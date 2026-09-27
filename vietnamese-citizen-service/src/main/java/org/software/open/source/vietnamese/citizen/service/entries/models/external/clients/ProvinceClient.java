package org.software.open.source.vietnamese.citizen.service.entries.models.external.clients;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.software.open.source.vietnamese.citizen.service.entries.models.external.dto.ProvinceApiDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProvinceClient { // <-- BỎ @RequiredArgsConstructor

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String baseUrl;

  private final AtomicReference<List<ProvinceApiDto.Province>> cache = new AtomicReference<>();

  public ProvinceClient(
      HttpClient httpClient,
      ObjectMapper objectMapper,
      @Value("${api.provinces.base-url:http://host.docker.internal:8082}") String baseUrl) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.baseUrl = baseUrl;
  }

  /**
   * Lấy danh sách tỉnh/thành kèm wards (có cache, chỉ gọi API 1 lần).
   */
  public List<ProvinceApiDto.Province> getProvincesWithWards() {
    List<ProvinceApiDto.Province> cached = cache.get();
    if (cached != null && !cached.isEmpty()) {
      return cached;
    }
    synchronized (this) {
      cached = cache.get();
      if (cached != null && !cached.isEmpty()) {
        return cached;
      }
      List<ProvinceApiDto.Province> loaded = fetchFromApi();
      if (!loaded.isEmpty()) {
        cache.set(loaded);
      }
      return loaded;
    }
  }

  private List<ProvinceApiDto.Province> fetchFromApi() {
    String url = baseUrl + "/provinces?page=0&size=100&sortBy=code&sortDir=asc&includeWard=true";

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Accept", "*/*")
        .timeout(Duration.ofSeconds(30))
        .GET()
        .build();

    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 202) {
        log.error("API provinces trả về status {}: {}", response.statusCode(), response.body());
        return List.of();
      }

      ProvinceApiDto.Response parsed = objectMapper.readValue(response.body(), ProvinceApiDto.Response.class);

      if (parsed == null || parsed.data() == null || parsed.data().content() == null) {
        log.warn("Response từ API provinces không chứa dữ liệu");
        return List.of();
      }

      List<ProvinceApiDto.Province> provinces = parsed.data().content().stream()
          .filter(p -> p.wards() != null && !p.wards().isEmpty())
          .toList();

      log.info("Đã tải và cache {} tỉnh/thành phố (kèm wards)", provinces.size());
      return provinces;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Bị gián đoạn khi gọi API provinces", e);
      return List.of();
    } catch (Exception e) {
      log.error("Lỗi khi gọi API provinces: {}", e.getMessage(), e);
      return List.of();
    }
  }

  public void clearCache() {
    cache.set(null);
  }
}