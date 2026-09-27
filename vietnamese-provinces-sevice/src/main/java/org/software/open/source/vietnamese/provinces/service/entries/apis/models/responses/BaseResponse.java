package org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true) // toBuilder để nuôi 2 helper withData/withErrors bên dưới
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

  private boolean success;
  private String code;
  private String message;
  private Instant timestamp;
  private T data;
  private List<ErrorDetails> errors;

  public static <T> BaseResponse<T> success(T data) {
    return BaseResponse.<T>builder()
        .success(true).code("SUCCESS").message("Request processed successfully")
        .timestamp(Instant.now()).data(data).build();
  }

  public static <T> BaseResponse<T> success(String message, T data) {
    return BaseResponse.<T>builder()
        .success(true).code("SUCCESS").message(message)
        .timestamp(Instant.now()).data(data).build();
  }

  public static <T> BaseResponse<T> created(T data) {
    return BaseResponse.<T>builder()
        .success(true).code("CREATED").message("Resource created successfully")
        .timestamp(Instant.now()).data(data).build();
  }

  public static <T> BaseResponse<T> created(String message, T data) {
    return BaseResponse.<T>builder()
        .success(true).code("CREATED").message(message)
        .timestamp(Instant.now()).data(data).build();
  }

  public static <T> BaseResponse<T> error(String code, String message) {
    return BaseResponse.<T>builder()
        .success(false).code(code).message(message)
        .timestamp(Instant.now()).build();
  }

  public static <T> BaseResponse<T> badRequest(String message) {
    return error("BAD_REQUEST", message);
  }

  public static <T> BaseResponse<T> notFound(String message) {
    return error("NOT_FOUND", message);
  }

  public static <T> BaseResponse<T> internalError(String message) {
    return error("INTERNAL_ERROR", message);
  }

  public static <T> BaseResponse<T> validationError(List<ErrorDetails> errors) {
    return BaseResponse.<T>builder()
        .success(false).code("VALIDATION_ERROR").message("Validation failed")
        .timestamp(Instant.now()).errors(errors).build();
  }

  /**
   * Helper thay cho setter khi cần "sửa" response theo kiểu bất biến giống record
   * cũ
   */
  public BaseResponse<T> withData(T newData) {
    return toBuilder().data(newData).build();
  }

  public BaseResponse<T> withErrors(List<ErrorDetails> newErrors) {
    return toBuilder().errors(newErrors).build();
  }

}
