package org.software.open.source.vietnamese.citizen.service.entries.models.external.dto;

import java.util.List;

/**
 * Gom toàn bộ cấu trúc response của API /provinces vào 1 file.
 */
public final class ProvinceApiDto {

  private ProvinceApiDto() {
  }

  public record Response(
      boolean success,
      String code,
      String message,
      String timestamp,
      Data data,
      List<Object> errors) {
  }

  public record Data(
      List<Province> content,
      int pageNo,
      int pageSize,
      long totalElements,
      int totalPages) {
  }

  public record Province(
      String code,
      String name,
      String nameEn,
      String fullName,
      String fullNameEn,
      String codeName,
      String postalCodePrefix,
      int administrativeUnitId,
      String administrativeUnitsName,
      List<Ward> wards) {
  }

  public record Ward(
      String code,
      String name,
      String nameEn,
      String fullName,
      String fullNameEn,
      String codeName,
      String postalCode,
      String provinceCode,
      int administrativeUnitId,
      String administrativeUnitsName) {
  }
}
