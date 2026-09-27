package org.software.open.source.vietnamese.provinces.service.utils;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class dùng để check page / size.
 * Giá trị min / max được nạp từ application.yaml qua bean Initializer.
 */
@Slf4j
public final class Helper {

  private static int minPage = 0;
  private static int minSize = 20;
  private static int maxSize = 100;

  /**
   * Constructor private để ngăn new Helper() từ bên ngoài.
   */
  private Helper() {
    throw new UnsupportedOperationException("Utility class, cannot instantiate");
  }

  /**
   * Khởi tạo giá trị page / size từ application.yaml.
   * Chỉ method static mới được ghi vào static field (SonarQube S2696).
   * Bean Initializer bên dưới sẽ gọi hàm này lúc Spring khởi động.
   */
  public static void init(int minPage, int minSize, int maxSize) {
    Helper.minPage = minPage;
    Helper.minSize = minSize;
    Helper.maxSize = maxSize;

    log.info("Helper initialized: min-page={}, min-size={}, max-size={}",
        minPage, minSize, maxSize);
  }

  public static int getMinPage() {
    return minPage;
  }

  public static int getMinSize() {
    return minSize;
  }

  public static int getMaxSize() {
    return maxSize;
  }

  public static boolean isValidPage(Integer page) {
    return page != null && page >= minPage;
  }

  public static boolean isValidSize(Integer size) {
    return size != null && size >= minSize && size <= maxSize;
  }

  public static boolean isValidPageAndSize(Integer page, Integer size) {
    boolean valid = isValidPage(page) && isValidSize(size);

    if (!valid) {
      log.warn("Invalid paging params: page={}, size={} (allowed: page >= {}, {} <= size <= {})",
          page, size, minPage, minSize, maxSize);
    }

    return valid;
  }

  /**
   * Bean duy nhất chịu trách nhiệm nạp giá trị từ application.yaml vào Helper.
   * Constructor injection nên không cần setter instance ghi vào static field.
   */
  @Component
  public static class Initializer {

    public Initializer(
        @Value("${responses.min-page:0}") int minPage,
        @Value("${responses.min-size:20}") int minSize,
        @Value("${responses.max-size:100}") int maxSize) {
      Helper.init(minPage, minSize, maxSize);
    }
  }

  /** Sort Helper */
  public static Sort buildSort(Class<?> entity, List<String> sortBy, List<String> sortDir) {
    if (sortBy == null || sortBy.isEmpty())
      return Sort.unsorted();

    return Sort.by(IntStream.range(0, sortBy.size()).mapToObj(i -> {
      String raw = sortBy.get(i);
      if (raw == null || raw.isBlank())
        throw new IllegalArgumentException("sortBy cannot be empty.");

      String field = camel(raw.trim());
      if (ReflectionUtils.findField(entity, field) == null)
        throw new IllegalArgumentException("Field invalid: " + raw);

      String dir = (sortDir != null && i < sortDir.size() && sortDir.get(i) != null) ? sortDir.get(i) : "asc";
      return new Sort.Order(Sort.Direction.fromString(dir), field);
    }).toList());
  }

  private static String camel(String s) {
    if (!s.contains("_"))
      return s;

    StringBuilder sb = new StringBuilder();
    boolean upper = false;

    for (char c : s.toLowerCase().toCharArray()) {
      if (c == '_') {
        upper = true;
      } else {
        sb.append(upper ? Character.toUpperCase(c) : c);
        upper = false;
      }
    }

    return sb.toString();
  }

}
