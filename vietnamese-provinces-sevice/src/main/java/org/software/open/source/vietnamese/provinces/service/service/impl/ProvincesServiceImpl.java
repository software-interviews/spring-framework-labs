package org.software.open.source.vietnamese.provinces.service.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.PageResponse;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.ProvincesResponse;
import org.software.open.source.vietnamese.provinces.service.io.entities.ProvincesEntity;
import org.software.open.source.vietnamese.provinces.service.io.entities.WardsEntity;
import org.software.open.source.vietnamese.provinces.service.io.repositories.ProvincesRepository;
import org.software.open.source.vietnamese.provinces.service.io.repositories.WardsRepository;
import org.software.open.source.vietnamese.provinces.service.mappers.ProvinceMapper;
import org.software.open.source.vietnamese.provinces.service.mappers.WardsMapper;
import org.software.open.source.vietnamese.provinces.service.service.ProvincesService;
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
public class ProvincesServiceImpl implements ProvincesService {

  private final ProvincesRepository provincesRepository;
  private final WardsRepository wardsRepository;

  private final ProvinceMapper provinceMapper;
  private final WardsMapper wardsMapper;

  @Override
  public PageResponse<ProvincesResponse> getProvinces(
      String search, int page, int size, String sortBy,
      String sortDir, boolean includeWard) {

    // 1. Validate & chuẩn hóa tham số phân trang
    int validPage = Helper.isValidPage(page) ? page : Helper.getMinPage();
    int validSize = Helper.isValidSize(size) ? size : Helper.getMinSize();

    if (validPage != page || validSize != size) {
      log.warn("Paging params adjusted: page {} -> {}, size {} -> {}", page, validPage, size, validSize);
    }

    Pageable pageable = PageRequest.of(validPage, validSize, Helper.buildSort(
        ProvincesEntity.class, List.of(sortBy), List.of(sortDir)));

    // 2. Query danh sách tỉnh/thành phố
    Page<ProvincesEntity> entityPage = StringUtils.hasText(search)
        ? provincesRepository.searchProvinces(search.trim(), pageable)
        : provincesRepository.findAll(pageable);

    // 3. Nếu không include wards -> map và trả về luôn (early return cho nhẹ)
    if (!includeWard) {
      return PageResponse.from(entityPage, provinceMapper::toResponse);
    }

    // 4. BULK FETCH: lấy sỉ wards của các province trong trang hiện tại
    final Map<String, List<WardsEntity>> wardsByProvince;
    if (entityPage.isEmpty()) {
      wardsByProvince = Map.of();
    } else {
      // 4.1: Method reference cho ProvincesEntity::getCode
      List<String> provinceCodes = entityPage.getContent().stream()
          .map(e -> e.getCode())
          .toList();

      // 4.2: 1 query duy nhất
      List<WardsEntity> allWardsInPage = wardsRepository.findByProvinceCodeIn(provinceCodes);

      // 4.3: Dùng lambda (ward -> ward.getProvinceCode()) thay vì method reference
      // để compiler (Eclipse/STS) không báo warning unchecked conversion null-safety.
      wardsByProvince = allWardsInPage.stream()
          .collect(Collectors.groupingBy(ward -> ward.getProvinceCode()));
    }

    // 5. Map entity -> response rồi ghép wards từ Map lookup O(1)
    return PageResponse.from(entityPage, entity -> {
      ProvincesResponse response = provinceMapper.toResponse(entity);

      response.setWards(wardsMapper.toResponses(
          wardsByProvince.getOrDefault(entity.getCode(), List.of())));

      return response;
    });

  }
}
