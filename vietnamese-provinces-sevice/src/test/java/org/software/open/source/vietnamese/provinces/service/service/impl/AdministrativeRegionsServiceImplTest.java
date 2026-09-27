package org.software.open.source.vietnamese.provinces.service.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.AdministrativeRegionsResponse;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.PageResponse;
import org.software.open.source.vietnamese.provinces.service.io.entities.AdministrativeRegionsEntity;
import org.software.open.source.vietnamese.provinces.service.io.repositories.AdministrativeRegionsRepository;
import org.software.open.source.vietnamese.provinces.service.mappers.AdministrativeRegionsMapper;
import org.software.open.source.vietnamese.provinces.service.utils.Helper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class AdministrativeRegionsServiceImplTest {

  @Mock
  private AdministrativeRegionsRepository administrativeRegionsRepository;

  @Mock
  private AdministrativeRegionsMapper administrativeRegionsMapper;

  @InjectMocks
  private AdministrativeRegionsServiceImpl service;

  @Test
  void getAdministrativeRegions_whenSearchIsEmpty_shouldCallFindAll() {
    // Arrange
    String search = "   "; // StringUtils.hasText will be false
    int page = 0;
    int size = 10;
    String sortBy = "id";
    String sortDir = "asc";

    AdministrativeRegionsEntity entity = new AdministrativeRegionsEntity();
    Sort sort = Helper.buildSort(AdministrativeRegionsEntity.class, List.of("id"), List.of("asc"));
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<AdministrativeRegionsEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
    AdministrativeRegionsResponse response = new AdministrativeRegionsResponse();

    // Mock static methods của Helper để test hoàn toàn isolate
    try (MockedStatic<Helper> mockedHelper = mockStatic(Helper.class)) {
      mockedHelper.when(() -> Helper.isValidPage(page)).thenReturn(true);
      mockedHelper.when(() -> Helper.isValidSize(size)).thenReturn(true);
      mockedHelper.when(() -> Helper.buildSort(any(), any(), any())).thenReturn(sort);

      when(administrativeRegionsRepository.findAll(any(Pageable.class))).thenReturn(entityPage);
      when(administrativeRegionsMapper.toResponse(entity)).thenReturn(response);

      // Act
      PageResponse<AdministrativeRegionsResponse> result = service.getAdministrativeRegions(search, page, size, sortBy,
          sortDir);

      // Assert
      assertThat(result).isNotNull();
      verify(administrativeRegionsRepository).findAll(any(Pageable.class));
      verify(administrativeRegionsRepository, never()).searchAdministrativeRegions(anyString(), any(Pageable.class));
      verify(administrativeRegionsMapper).toResponse(entity);
    }
  }

  @Test
  void getAdministrativeRegions_whenSearchIsPresent_shouldCallSearchMethod() {
    // Arrange
    String search = "Miền Bắc";
    int page = 0;
    int size = 10;
    String sortBy = "name";
    String sortDir = "desc";

    Sort sort = Helper.buildSort(AdministrativeRegionsEntity.class, List.of("name"), List.of("desc"));
    Pageable pageable = PageRequest.of(page, size, sort);

    AdministrativeRegionsEntity entity = new AdministrativeRegionsEntity();
    Page<AdministrativeRegionsEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
    AdministrativeRegionsResponse response = new AdministrativeRegionsResponse();

    try (MockedStatic<Helper> mockedHelper = mockStatic(Helper.class)) {
      mockedHelper.when(() -> Helper.isValidPage(page)).thenReturn(true);
      mockedHelper.when(() -> Helper.isValidSize(size)).thenReturn(true);
      mockedHelper.when(() -> Helper.buildSort(any(), any(), any())).thenReturn(sort);

      when(administrativeRegionsRepository.searchAdministrativeRegions(eq(search), any(Pageable.class)))
          .thenReturn(entityPage);
      when(administrativeRegionsMapper.toResponse(entity)).thenReturn(response);

      // Act
      PageResponse<AdministrativeRegionsResponse> result = service.getAdministrativeRegions(search, page, size, sortBy,
          sortDir);

      // Assert
      assertThat(result).isNotNull();
      verify(administrativeRegionsRepository).searchAdministrativeRegions(eq(search), any(Pageable.class));
      verify(administrativeRegionsRepository, never()).findAll(any(Pageable.class));
    }
  }

  @Test
  void getAdministrativeRegions_whenPageAndSizeInvalid_shouldAdjustToMinValues() {
    // Arrange
    String search = null;
    int invalidPage = -1;
    int invalidSize = -5;
    String sortBy = "id";
    String sortDir = "asc";

    int minPage = 0;
    int minSize = 10;
    Sort sort = Helper.buildSort(AdministrativeRegionsEntity.class, List.of("id"), List.of("asc"));
    Pageable expectedPageable = PageRequest.of(minPage, minSize, sort);

    Page<AdministrativeRegionsEntity> emptyPage = new PageImpl<>(Collections.emptyList(), expectedPageable, 0);

    try (MockedStatic<Helper> mockedHelper = mockStatic(Helper.class)) {
      mockedHelper.when(() -> Helper.isValidPage(invalidPage)).thenReturn(false);
      mockedHelper.when(() -> Helper.getMinPage()).thenReturn(minPage);
      mockedHelper.when(() -> Helper.isValidSize(invalidSize)).thenReturn(false);
      mockedHelper.when(() -> Helper.getMinSize()).thenReturn(minSize);
      mockedHelper.when(() -> Helper.buildSort(any(), any(), any())).thenReturn(sort);

      when(administrativeRegionsRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

      // Act
      PageResponse<AdministrativeRegionsResponse> result = service.getAdministrativeRegions(search, invalidPage,
          invalidSize, sortBy, sortDir);

      // Assert
      assertThat(result).isNotNull();

      // Verify repository được gọi với Pageable đã được adjust về minPage và minSize
      // SỬA: Bỏ eq(...) đi, truyền thẳng object vào
      verify(administrativeRegionsRepository).findAll(expectedPageable);
    }

  }

}
