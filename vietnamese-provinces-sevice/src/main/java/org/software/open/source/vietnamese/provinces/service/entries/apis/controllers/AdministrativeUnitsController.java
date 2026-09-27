package org.software.open.source.vietnamese.provinces.service.entries.apis.controllers;

import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.AdministrativeUnitsResponse;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.BaseResponse;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.PageResponse;
import org.software.open.source.vietnamese.provinces.service.service.AdministrativeUnitsService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/administrative-units")
public class AdministrativeUnitsController {

  private final AdministrativeUnitsService administrativeUnitsService;

  @GetMapping
  public ResponseEntity<BaseResponse<PageResponse<AdministrativeUnitsResponse>>> getAdministrativeUnits(
      @RequestParam(defaultValue = "") String search,
      @Min(value = 0, message = "page must be greater than or equal to 0") @RequestParam(defaultValue = "0") int page,
      @Min(value = 0, message = "size must be greater than or equal to 0") @Max(value = 100, message = "size must be less than or equal to 100") @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir) {

    /*
     * Vì Spring Data PageRequest không chấp nhận size = 0,
     * nếu client truyền size = 0 thì mình nên đưa về size mặc định.
     */
    int validSize = size == 0 ? 20 : size;

    PageResponse<AdministrativeUnitsResponse> data = administrativeUnitsService.getAdministrativeUnits(
        search,
        page,
        validSize,
        sortBy,
        sortDir);

    return ResponseEntity.accepted()
        .body(BaseResponse.success(data));
  }

}
