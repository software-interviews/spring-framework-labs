package org.software.open.source.vietnamese.provinces.service.mappers;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.ProvincesResponse;
import org.software.open.source.vietnamese.provinces.service.io.entities.ProvincesEntity;
import org.software.open.source.vietnamese.provinces.service.utils.enums.AdministrativeUnits;

@Mapper(componentModel = "spring")
public interface ProvinceMapper {

  @Mapping(target = "administrativeUnitsName", ignore = true)
  @Mapping(target = "wards", ignore = true)
  ProvincesResponse toResponse(ProvincesEntity entity);

  List<ProvincesResponse> toResponses(List<ProvincesEntity> entities);

  @AfterMapping
  default void fillAdministrativeUnitsName(ProvincesEntity entity, @MappingTarget ProvincesResponse response) {
    response.setAdministrativeUnitsName(
        AdministrativeUnits.findById(entity.getAdministrativeUnitId())
            .map(unit -> unit.getFullName()) // lambda thay vì AdministrativeUnits::getFullName
            .orElse(null));
  }

}
