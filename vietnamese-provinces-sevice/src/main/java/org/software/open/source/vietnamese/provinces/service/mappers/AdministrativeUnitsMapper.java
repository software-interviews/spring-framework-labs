package org.software.open.source.vietnamese.provinces.service.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.AdministrativeUnitsResponse;
import org.software.open.source.vietnamese.provinces.service.io.entities.AdministrativeUnitsEntity;

@Mapper(componentModel = "spring")
public interface AdministrativeUnitsMapper {

  AdministrativeUnitsResponse toResponse(AdministrativeUnitsEntity entity);

  List<AdministrativeUnitsResponse> toResponses(List<AdministrativeUnitsEntity> entities);

}
