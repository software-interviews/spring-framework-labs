package org.software.open.source.vietnamese.provinces.service.service.impl;

import java.util.List;

import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.AdministrativeUnitsResponse;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.PageResponse;
import org.software.open.source.vietnamese.provinces.service.io.entities.AdministrativeUnitsEntity;
import org.software.open.source.vietnamese.provinces.service.io.repositories.AdministrativeUnitsRepository;
import org.software.open.source.vietnamese.provinces.service.mappers.AdministrativeUnitsMapper;
import org.software.open.source.vietnamese.provinces.service.service.AdministrativeUnitsService;
import org.software.open.source.vietnamese.provinces.service.utils.Helper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdministrativeUnitsServiceImpl implements AdministrativeUnitsService {

  private final AdministrativeUnitsRepository administrativeUnitsRepository;
  private final AdministrativeUnitsMapper administrativeUnitsMapper;

  @Override
  public PageResponse<AdministrativeUnitsResponse> getAdministrativeUnits(String search, int page, int size,
      String sortBy, String sortDir) {
    int validPage = Helper.isValidPage(page) ? page : Helper.getMinPage();
    int validSize = Helper.isValidSize(size) ? size : Helper.getMinSize();

    if (validPage != page || validSize != size) {
      log.warn("Paging params adjusted: page {} -> {}, size {} -> {}",
          page, validPage, size, validSize);
    }

    Pageable pageable = PageRequest.of(validPage, validSize, Helper.buildSort(
        AdministrativeUnitsEntity.class, List.of(sortBy), List.of(sortDir)));

    // search rỗng thì lấy hết, ngược lại thì lấy theo search
    Page<AdministrativeUnitsEntity> entityPage = StringUtils.hasText(search)
        ? administrativeUnitsRepository.searchAdministrativeUnits(search.trim(), pageable)
        : administrativeUnitsRepository.findAll(pageable);

    // PageResponse record tự map từ Page + mapper function
    return PageResponse.from(entityPage, administrativeUnitsMapper::toResponse);
  }

}
