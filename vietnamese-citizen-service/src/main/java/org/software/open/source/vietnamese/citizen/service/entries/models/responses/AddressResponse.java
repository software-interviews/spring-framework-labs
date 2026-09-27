package org.software.open.source.vietnamese.citizen.service.entries.models.responses;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

  private UUID id;
  private UUID citizenId;
  private String detail;
  private String ward;
  private String province;
  private String addressType;
  private OffsetDateTime createdDate;
  private OffsetDateTime updatedDate;

}
