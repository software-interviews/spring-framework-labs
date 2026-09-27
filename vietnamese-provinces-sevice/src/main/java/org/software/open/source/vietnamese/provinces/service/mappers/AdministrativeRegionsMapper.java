package org.software.open.source.vietnamese.provinces.service.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.AdministrativeRegionsResponse;
import org.software.open.source.vietnamese.provinces.service.io.entities.AdministrativeRegionsEntity;

@Mapper(componentModel = "spring")
public interface AdministrativeRegionsMapper {

  // MapStruct tự động gọi constructor của record để map
  AdministrativeRegionsResponse toResponse(AdministrativeRegionsEntity entity);

  List<AdministrativeRegionsResponse> toResponseList(List<AdministrativeRegionsEntity> entities);

}