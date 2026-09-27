package org.software.open.source.vietnamese.citizen.service.mappers;

import java.util.List;
import java.util.UUID;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.software.open.source.vietnamese.citizen.service.entries.models.requests.AddressRequest;
import org.software.open.source.vietnamese.citizen.service.entries.models.responses.AddressResponse;
import org.software.open.source.vietnamese.citizen.service.io.entities.AddressEntity;
import org.software.open.source.vietnamese.citizen.service.io.entities.PartitionKey;

@Mapper(componentModel = "spring")
public interface AddressMapper {

  @Mapping(target = "key", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "citizenId", source = "citizenId")
  AddressEntity toEntity(AddressRequest request, UUID citizenId);

  @AfterMapping
  default void generateKey(@MappingTarget AddressEntity entity) {
    if (entity.getKey() == null) {
      entity.setKey(new PartitionKey(UUID.randomUUID(), null));
    }
  }

  @Mapping(source = "key.id", target = "id")
  @Mapping(source = "key.createdDate", target = "createdDate")
  AddressResponse toResponse(AddressEntity entity);

  List<AddressResponse> toResponseList(List<AddressEntity> entities);

  @Mapping(target = "key", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "citizenId", ignore = true)
  void updateEntity(AddressRequest request, @MappingTarget AddressEntity entity);

}
