package org.software.open.source.vietnamese.citizen.service.entries.controllers;

import org.software.open.source.vietnamese.citizen.service.entries.models.requests.CitizenRequest;
import org.software.open.source.vietnamese.citizen.service.entries.models.responses.BaseResponse;
import org.software.open.source.vietnamese.citizen.service.entries.models.responses.CitizenResponse;
import org.software.open.source.vietnamese.citizen.service.services.CitizenService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/citizen")
public class CitizenController {

  private final CitizenService citizenService;

  @PostMapping
  public ResponseEntity<BaseResponse<CitizenResponse>> createCitizen(
      @RequestBody @Valid CitizenRequest request) {
    log.info("Citizen request: {}", request);
    CitizenResponse data = citizenService.createCitizen(request);

    return ResponseEntity.accepted()
        .body(BaseResponse.success(data));
  }

}
