package org.software.open.source.vietnamese.citizen.service.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AddressType {

  HOME(1, "Địa chỉ nhà"),
  WORK(2, "Địa chỉ nơi làm việc");

  private final int code;
  private final String description;

  /**
   * Tra cứu AddressType theo code.
   *
   * @param code mã số của loại địa chỉ (1, 2, ...)
   * @return AddressType tương ứng
   * @throws IllegalArgumentException nếu code không tồn tại
   */
  public static AddressType fromCode(int code) {
    for (AddressType type : values()) {
      if (type.code == code) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown AddressType code: " + code);
  }
}
