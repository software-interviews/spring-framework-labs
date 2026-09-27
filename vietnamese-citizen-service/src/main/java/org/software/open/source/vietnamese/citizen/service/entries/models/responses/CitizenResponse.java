package org.software.open.source.vietnamese.citizen.service.entries.models.responses;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenResponse {

  private UUID id;
  private String firstName;
  private String lastName;
  private String fullName;
  private LocalDate birthday;
  private OffsetDateTime createdDate;
  private OffsetDateTime updatedDate;
  private List<AddressResponse> address;

}
