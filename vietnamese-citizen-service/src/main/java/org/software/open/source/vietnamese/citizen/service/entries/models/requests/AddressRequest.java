package org.software.open.source.vietnamese.citizen.service.entries.models.requests;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

  @NotNull(message = "Citizen ID is required")
  private UUID citizenId;

  @NotBlank(message = "Address detail is required")
  private String detail;

  @Size(max = 100, message = "Ward must be at most 100 characters")
  private String ward;

  @Size(max = 100, message = "Province must be at most 100 characters")
  private String province;

  @NotBlank(message = "Address type is required")
  @Size(max = 50, message = "Address type must be at most 50 characters")
  private String addressType;

}
