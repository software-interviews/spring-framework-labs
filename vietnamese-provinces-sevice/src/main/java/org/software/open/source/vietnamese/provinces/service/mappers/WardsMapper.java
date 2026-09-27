package org.software.open.source.vietnamese.provinces.service.mappers;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.WardsResponse;
import org.software.open.source.vietnamese.provinces.service.io.entities.WardsEntity;
import org.software.open.source.vietnamese.provinces.service.utils.enums.AdministrativeUnits;

@Mapper(componentModel = "spring")
public interface WardsMapper {

  @Mapping(target = "administrativeUnitsName", ignore = true)
  WardsResponse toResponse(WardsEntity entity);

  List<WardsResponse> toResponses(List<WardsEntity> entities);

  @AfterMapping
  default void fillAdministrativeUnitsName(WardsEntity entity, @MappingTarget WardsResponse response) {
    response.setAdministrativeUnitsName(
        AdministrativeUnits.findById(entity.getAdministrativeUnitId())
            .map(unit -> unit.getFullName())
            .orElse(null));
  }
}
