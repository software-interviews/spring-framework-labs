package org.software.open.source.vietnamese.provinces.service.utils.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.software.open.source.vietnamese.provinces.service.io.entities.AdministrativeUnitsEntity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum biểu diễn 5 dòng dữ liệu CỐ ĐỊNH của bảng {@code administrative_units}.
 *
 * <p>
 * Vì dataset này không bao giờ thay đổi cấu trúc (chỉ có 5 loại đơn vị hành
 * chính),
 * nên dùng enum giúp tránh phải JOIN bảng {@code administrative_units} khi cần
 * hiển thị tên loại đơn vị, đồng thời đảm bảo type-safety cho business logic.
 * </p>
 */
@Getter
@RequiredArgsConstructor
public enum AdministrativeUnits {

  /** id = 1 */
  MUNICIPALITY(
      1,
      "Thành phố trực thuộc trung ương", "Municipality",
      "Thành phố", "City",
      "thanh_pho_truc_thuoc_trung_uong", "municipality"),

  /** id = 2 */
  PROVINCE(
      2,
      "Tỉnh", "Province",
      "Tỉnh", "Province",
      "tinh", "province"),

  /** id = 3 */
  WARD(
      3,
      "Phường", "Ward",
      "Phường", "Ward",
      "phuong", "ward"),

  /** id = 4 */
  COMMUNE(
      4,
      "Xã", "Commune",
      "Xã", "Commune",
      "xa", "commune"),

  /** id = 5 */
  SPECIAL_ADMINISTRATIVE_REGION(
      5,
      "Đặc khu tại hải đảo", "Special administrative region",
      "Đặc khu", "Special administrative region",
      "dac_khu", "special_administrative_region");

  // ===== Fields map 1-1 với các cột trong bảng administrative_units =====
  private final int id;
  private final String fullName;
  private final String fullNameEn;
  private final String shortName;
  private final String shortNameEn;
  private final String codeName;
  private final String codeNameEn;

  // ===== Cache lookup O(1) =====
  private static final Map<Integer, AdministrativeUnits> BY_ID;
  private static final Map<String, AdministrativeUnits> BY_CODE_NAME;
  private static final Map<String, AdministrativeUnits> BY_CODE_NAME_EN;

  static {
    Map<Integer, AdministrativeUnits> byId = new HashMap<>();
    Map<String, AdministrativeUnits> byCodeName = new HashMap<>();
    Map<String, AdministrativeUnits> byCodeNameEn = new HashMap<>();

    for (AdministrativeUnits unit : values()) {
      byId.put(unit.getId(), unit);
      byCodeName.put(unit.getCodeName(), unit);
      byCodeNameEn.put(unit.getCodeNameEn(), unit);
    }

    BY_ID = Collections.unmodifiableMap(byId);
    BY_CODE_NAME = Collections.unmodifiableMap(byCodeName);
    BY_CODE_NAME_EN = Collections.unmodifiableMap(byCodeNameEn);
  }

  // ===== Phương thức tra cứu =====

  /** Tra cứu an toàn theo id, trả về Optional nếu không tồn tại */
  public static Optional<AdministrativeUnits> findById(Integer id) {
    return id == null ? Optional.empty() : Optional.ofNullable(BY_ID.get(id));
  }

  /** Tra cứu theo id, throw exception nếu id không hợp lệ */
  public static AdministrativeUnits requireById(Integer id) {
    return findById(id).orElseThrow(() -> new IllegalArgumentException(
        "Không tồn tại administrative unit với id = " + id));
  }

  /**
   * Tra cứu theo code_name tiếng Việt (vd: "tinh",
   * "thanh_pho_truc_thuoc_trung_uong")
   */
  public static Optional<AdministrativeUnits> findByCodeName(String codeName) {
    return codeName == null ? Optional.empty() : Optional.ofNullable(BY_CODE_NAME.get(codeName));
  }

  /** Tra cứu theo code_name_en (vd: "province", "municipality") */
  public static Optional<AdministrativeUnits> findByCodeNameEn(String codeNameEn) {
    return codeNameEn == null ? Optional.empty() : Optional.ofNullable(BY_CODE_NAME_EN.get(codeNameEn));
  }

  /** Map từ entity đọc dưới DB lên enum */
  public static Optional<AdministrativeUnits> fromEntity(AdministrativeUnitsEntity entity) {
    return entity == null ? Optional.empty() : findById(entity.getId());
  }

  // ===== Helper business logic =====

  /** Kiểm tra entity truyền vào có thuộc loại unit này không */
  public boolean matches(Integer administrativeUnitId) {
    return administrativeUnitId != null && this.id == administrativeUnitId;
  }

  /** Cấp tỉnh: Thành phố trực thuộc trung ương (1) hoặc Tỉnh (2) */
  public boolean isProvinceLevel() {
    return this == MUNICIPALITY || this == PROVINCE;
  }

  /** Cấp xã: Phường (3), Xã (4) hoặc Đặc khu tại hải đảo (5) */
  public boolean isWardLevel() {
    return this == WARD || this == COMMUNE || this == SPECIAL_ADMINISTRATIVE_REGION;
  }

}
