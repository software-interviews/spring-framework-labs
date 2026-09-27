package org.software.open.source.vietnamese.citizen.service.io.entities;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartitionKey implements Serializable {

  @Column(name = "id", nullable = false)
  private UUID id;

  // Đã bỏ @CreationTimestamp vì Hibernate 6 không cho phép dùng nó trên trường
  // @Id
  @Column(name = "created_date", nullable = false, updatable = false)
  private OffsetDateTime createdDate;
}