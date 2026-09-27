package org.software.open.source.vietnamese.provinces.service.entries.apis.controllers;

import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.BaseResponse;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.PageResponse;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.ProvincesResponse;
import org.software.open.source.vietnamese.provinces.service.service.ProvincesService;
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
@RequestMapping("/provinces")
public class ProvincesController {

  private final ProvincesService provincesService;

  @GetMapping
  public ResponseEntity<BaseResponse<PageResponse<ProvincesResponse>>> getProvinces(
      @RequestParam(defaultValue = "") String search,
      @Min(value = 0, message = "page must be greater than or equal to 0") @RequestParam(defaultValue = "0") int page,
      @Min(value = 0, message = "size must be greater than or equal to 0") @Max(value = 100, message = "size must be less than or equal to 100") @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "code") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir,
      @RequestParam(defaultValue = "true") boolean includeWard) {

    int validSize = size == 0 ? 20 : size;

    PageResponse<ProvincesResponse> data = provincesService.getProvinces(
        search,
        page,
        validSize,
        sortBy,
        sortDir, includeWard);

    return ResponseEntity.accepted()
        .body(BaseResponse.success(data));
  }

}
