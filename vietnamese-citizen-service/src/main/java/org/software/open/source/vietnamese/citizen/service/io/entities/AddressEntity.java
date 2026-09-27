package org.software.open.source.vietnamese.citizen.service.io.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AddressEntity extends BasePartitionedEntity<UUID> {

  @EmbeddedId
  private PartitionKey key;

  @Column(name = "citizen_id", nullable = false)
  private UUID citizenId;

  @Column(name = "detail", columnDefinition = "TEXT")
  private String detail;

  @Column(name = "ward", length = 100)
  private String ward;

  @Column(name = "province", length = 100)
  private String province;

  @Column(name = "address_type", nullable = false, length = 50)
  private String addressType;

  @Override
  public UUID getId() {
    return key != null ? key.getId() : null;
  }

  @Override
  public void setId(UUID id) {
    if (this.key == null)
      this.key = new PartitionKey();
    this.key.setId(id);
  }

  @Override
  public java.time.OffsetDateTime getCreatedDate() {
    return key != null ? key.getCreatedDate() : null;
  }

  @Override
  public void setCreatedDate(java.time.OffsetDateTime createdDate) {
    if (this.key == null)
      this.key = new PartitionKey();
    this.key.setCreatedDate(createdDate);
  }

  @Override
  public PartitionKey getKey() {
    return this.key;
  }

  @Override
  public void setKey(PartitionKey key) {
    this.key = key;
  }

}
