package org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses;

import java.util.List;

import lombok.Data;

/**
 * Response/DTO class generated from ProvincesEntity
 */
@Data
public class ProvincesResponse {

  private String code;
  private String name;
  private String nameEn;
  private String fullName;
  private String fullNameEn;
  private String codeName;
  private String postalCodePrefix;
  private Integer administrativeUnitId;
  private String administrativeUnitsName;
  private List<WardsResponse> wards;

}
