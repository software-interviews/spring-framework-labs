package org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses;

import lombok.Data;

@Data
public class AdministrativeRegionsResponse {

  private Integer id;
  private String name;
  private String nameEn;
  private String codeName;
  private String codeNameEn;

}