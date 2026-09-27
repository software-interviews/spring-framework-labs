package org.software.open.source.vietnamese.citizen.service.mappers;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.software.open.source.vietnamese.citizen.service.entries.models.requests.CitizenRequest;
import org.software.open.source.vietnamese.citizen.service.entries.models.responses.CitizenResponse;
import org.software.open.source.vietnamese.citizen.service.io.entities.CitizenEntity;

@Mapper(componentModel = "spring")
public interface CitizenMapper {

  @Mapping(target = "key.id", source = "id")
  @Mapping(target = "key.createdDate", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "fullName", source = "request", qualifiedByName = "buildFullName")
  CitizenEntity toEntity(UUID id, CitizenRequest request);

  @Mapping(source = "key.id", target = "id")
  @Mapping(source = "key.createdDate", target = "createdDate")
  @Mapping(target = "address", ignore = true)
  CitizenResponse toResponse(CitizenEntity entity);

  @Mapping(target = "key", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "fullName", source = "request", qualifiedByName = "buildFullName")
  void updateEntity(CitizenRequest request, @MappingTarget CitizenEntity entity);

  /**
   * Ghép Họ + Tên theo chuẩn tiếng Việt.
   * Ví dụ: lastName="Nguyễn", firstName="An" → "Nguyễn An"
   */
  @Named("buildFullName")
  default String buildFullName(CitizenRequest request) {
    if (request == null) {
      return null;
    }
    String last = request.getLastName() == null ? "" : request.getLastName().trim();
    String first = request.getFirstName() == null ? "" : request.getFirstName().trim();
    return (last + " " + first).trim();
  }

}
