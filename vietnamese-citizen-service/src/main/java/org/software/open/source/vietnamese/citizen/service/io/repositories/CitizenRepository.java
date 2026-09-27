package org.software.open.source.vietnamese.citizen.service.io.repositories;

import org.software.open.source.vietnamese.citizen.service.io.entities.CitizenEntity;
import org.software.open.source.vietnamese.citizen.service.io.entities.PartitionKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitizenRepository extends JpaRepository<CitizenEntity, PartitionKey> {

}
