package org.software.open.source.vietnamese.citizen.service.io.repositories;

import java.util.List;
import java.util.UUID;

import org.software.open.source.vietnamese.citizen.service.io.entities.AddressEntity;
import org.software.open.source.vietnamese.citizen.service.io.entities.PartitionKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<AddressEntity, PartitionKey> {
  List<AddressEntity> findByCitizenId(UUID citizenId);
}
