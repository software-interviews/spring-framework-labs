package org.software.open.source.vietnamese.citizen.service.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class sinh dữ liệu ngày sinh (birthday) giả lập.
 * Hỗ trợ sinh ngày ngẫu nhiên trong khoảng từ năm 1900 đến hôm nay,
 * hoặc trong khoảng tuổi cụ thể.
 */
public final class FakeBirthday {

  // Ngăn chặn khởi tạo instance (Utility class pattern)
  private FakeBirthday() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  // Mốc dưới: 01/01/1900
  private static final LocalDate MIN_DATE = LocalDate.of(1900, 1, 1);

  /**
   * Sinh một ngày sinh ngẫu nhiên trong khoảng từ 01/01/1900 đến hôm nay
   * (LocalDate.now()).
   *
   * @return LocalDate ngẫu nhiên
   */
  public static LocalDate getBirthday() {
    LocalDate today = LocalDate.now(ZoneId.systemDefault());
    return randomDateBetween(MIN_DATE, today);
  }

  /**
   * Sinh một ngày sinh ngẫu nhiên cho một người có độ tuổi nằm trong khoảng
   * [minAge, maxAge].
   * Rất hữu ích khi test các nghiệp vụ có ràng buộc độ tuổi (ví dụ: đủ 18 tuổi
   * làm CCCD,
   * độ tuổi nghỉ hưu 60-62, v.v.)
   *
   * @param minAge tuổi tối thiểu (inclusive)
   * @param maxAge tuổi tối đa (inclusive)
   * @return LocalDate ngẫu nhiên tương ứng với độ tuổi
   * @throws IllegalArgumentException nếu minAge > maxAge hoặc tuổi âm
   */
  public static LocalDate getBirthday(int minAge, int maxAge) {
    if (minAge < 0 || maxAge < 0) {
      throw new IllegalArgumentException("Age must be non-negative");
    }
    if (minAge > maxAge) {
      throw new IllegalArgumentException("minAge must be less than or equal to maxAge");
    }

    LocalDate today = LocalDate.now(ZoneId.systemDefault());
    LocalDate latestDate = today.minusYears(minAge); // Người trẻ nhất (ít tuổi nhất)
    LocalDate earliestDate = today.minusYears(maxAge); // Người già nhất (nhiều tuổi nhất)

    return randomDateBetween(earliestDate, latestDate);
  }

  /**
   * Sinh một ngày ngẫu nhiên trong khoảng [from, to] (inclusive cả 2 đầu).
   * Sử dụng epoch days để đảm bảo mọi ngày sinh ra đều hợp lệ (không bị lỗi 29/2
   * năm không nhuận).
   */
  private static LocalDate randomDateBetween(LocalDate from, LocalDate to) {
    long fromEpoch = from.toEpochDay();
    long toEpoch = to.toEpochDay();
    long randomEpoch = ThreadLocalRandom.current().nextLong(fromEpoch, toEpoch + 1);
    return LocalDate.ofEpochDay(randomEpoch);
  }

}
