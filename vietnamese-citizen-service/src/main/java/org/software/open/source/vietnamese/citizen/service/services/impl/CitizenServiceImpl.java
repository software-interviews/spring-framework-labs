package org.software.open.source.vietnamese.citizen.service.services.impl;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.software.open.source.vietnamese.citizen.service.entries.models.requests.CitizenRequest;
import org.software.open.source.vietnamese.citizen.service.entries.models.responses.CitizenResponse;
import org.software.open.source.vietnamese.citizen.service.io.entities.AddressEntity;
import org.software.open.source.vietnamese.citizen.service.io.entities.CitizenEntity;
import org.software.open.source.vietnamese.citizen.service.io.repositories.AddressRepository;
import org.software.open.source.vietnamese.citizen.service.io.repositories.CitizenRepository;
import org.software.open.source.vietnamese.citizen.service.mappers.AddressMapper;
import org.software.open.source.vietnamese.citizen.service.mappers.CitizenMapper;
import org.software.open.source.vietnamese.citizen.service.services.CitizenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenServiceImpl implements CitizenService {

  private final CitizenRepository citizenRepository;
  private final AddressRepository addressRepository;
  private final CitizenMapper citizenMapper;
  private final AddressMapper addressMapper;

  @Override
  @Transactional
  public CitizenResponse createCitizen(CitizenRequest request) {
    log.info("Tạo citizen: {} {}", request.getFirstName(), request.getLastName());

    UUID citizenId = UUID.randomUUID();
    CitizenEntity citizen = citizenMapper.toEntity(citizenId, request);

    // 1. Lưu citizen trước - @PrePersist sẽ set createdDate tự động
    CitizenEntity savedCitizen = citizenRepository.save(citizen);
    // Flush để @PrePersist thực sự chạy và createdDate được set trên entity object
    citizenRepository.flush();

    OffsetDateTime sharedCreatedDate = savedCitizen.getCreatedDate(); // ← lấy createdDate đã được set

    // 2. Lưu addresses với CÙNG citizenId và CÙNG createdDate
    List<AddressEntity> savedAddresses = Collections.emptyList();
    if (request.getAddresses() != null && !request.getAddresses().isEmpty()) {
      List<AddressEntity> addressEntities = request.getAddresses().stream()
          .map(addrReq -> {
            AddressEntity addr = addressMapper.toEntity(addrReq, savedCitizen.getId());
            // QUAN TRỌNG: share createdDate với citizen
            // @PrePersist check "if (getCreatedDate() == null)" nên sẽ KHÔNG overwrite
            addr.setCreatedDate(sharedCreatedDate);
            return addr;
          })
          .toList();

      savedAddresses = addressRepository.saveAll(addressEntities);
    }

    // 3. Build response
    CitizenResponse response = citizenMapper.toResponse(savedCitizen);
    response.setAddress(addressMapper.toResponseList(savedAddresses));

    log.info("Tạo thành công citizen {} với {} addresses",
        response.getId(), savedAddresses.size());
    return response;
  }

}
