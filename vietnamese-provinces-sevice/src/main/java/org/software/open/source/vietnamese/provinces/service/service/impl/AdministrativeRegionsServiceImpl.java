package org.software.open.source.vietnamese.provinces.service.service.impl;

import java.util.List;

import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.AdministrativeRegionsResponse;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.PageResponse;
import org.software.open.source.vietnamese.provinces.service.io.entities.AdministrativeRegionsEntity;
import org.software.open.source.vietnamese.provinces.service.io.repositories.AdministrativeRegionsRepository;
import org.software.open.source.vietnamese.provinces.service.mappers.AdministrativeRegionsMapper;
import org.software.open.source.vietnamese.provinces.service.service.AdministrativeRegionsService;
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
public class AdministrativeRegionsServiceImpl implements AdministrativeRegionsService {

  private final AdministrativeRegionsRepository administrativeRegionsRepository;
  private final AdministrativeRegionsMapper administrativeRegionsMapper;

  @Override
  public PageResponse<AdministrativeRegionsResponse> getAdministrativeRegions(
      String search, int page, int size, String sortBy, String sortDir) {

    // Check page/size bằng Helper (static), kể cả caller gọi thẳng qua interface
    int validPage = Helper.isValidPage(page) ? page : Helper.getMinPage();
    int validSize = Helper.isValidSize(size) ? size : Helper.getMinSize();

    if (validPage != page || validSize != size) {
      log.warn("Paging params adjusted: page {} -> {}, size {} -> {}",
          page, validPage, size, validSize);
    }

    Pageable pageable = PageRequest.of(validPage, validSize, Helper.buildSort(
        AdministrativeRegionsEntity.class, List.of(sortBy), List.of(sortDir)));

    // search rỗng thì lấy hết, ngược lại thì lấy theo search
    Page<AdministrativeRegionsEntity> entityPage = StringUtils.hasText(search)
        ? administrativeRegionsRepository.searchAdministrativeRegions(search.trim(), pageable)
        : administrativeRegionsRepository.findAll(pageable);

    // PageResponse record tự map từ Page + mapper function
    return PageResponse.from(entityPage, administrativeRegionsMapper::toResponse);

  }

}
