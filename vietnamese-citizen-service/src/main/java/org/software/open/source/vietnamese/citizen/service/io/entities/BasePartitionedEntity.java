package org.software.open.source.vietnamese.citizen.service.io.entities;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BasePartitionedEntity<I> {

  @Column(name = "updated_date", nullable = false)
  protected OffsetDateTime updatedDate;

  public abstract I getId();

  public abstract void setId(I id);

  public abstract OffsetDateTime getCreatedDate();

  public abstract void setCreatedDate(OffsetDateTime createdDate);

  public abstract PartitionKey getKey();

  public abstract void setKey(PartitionKey key);

  @PrePersist
  protected void onCreate() {
    OffsetDateTime now = OffsetDateTime.now();
    if (getCreatedDate() == null) {
      setCreatedDate(now);
    }
    this.updatedDate = now;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedDate = OffsetDateTime.now();
  }

}
