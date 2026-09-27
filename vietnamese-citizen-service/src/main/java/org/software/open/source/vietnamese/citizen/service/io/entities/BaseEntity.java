package org.software.open.source.vietnamese.citizen.service.io.entities;

import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

  @CreationTimestamp
  @Column(name = "created_date", nullable = false)
  private OffsetDateTime createdDate;

  @UpdateTimestamp
  @Column(name = "updated_date", nullable = false)
  private OffsetDateTime updatedDate;

}
