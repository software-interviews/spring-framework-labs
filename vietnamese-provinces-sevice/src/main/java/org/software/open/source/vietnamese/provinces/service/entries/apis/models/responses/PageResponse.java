package org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PageResponse<T> {

  @Builder.Default
  private List<T> content = List.of();
  private int pageNo;
  private int pageSize;
  private long totalElements;
  private int totalPages;

  /** Constructor không tham số: content mặc định list rỗng, không bao giờ null */
  public PageResponse() {
    this.content = List.of();
  }

  public static <T> PageResponse<T> from(Page<T> page) {
    return PageResponse.<T>builder()
        .content(page.getContent() == null ? List.of() : page.getContent())
        .pageNo(page.getNumber())
        .pageSize(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .build();
  }

  public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
    return from(page.map(mapper));
  }

  /**
   * Override setter để giữ đúng invariant cũ của record: content không bao giờ
   * null.
   * (Lombok tự động skip, không sinh setter trùng tên nữa.)
   */
  public void setContent(List<T> content) {
    this.content = (content == null) ? List.of() : content;
  }

}
