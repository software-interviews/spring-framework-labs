package org.software.open.source.vietnamese.citizen.service.io.entities;

import java.time.LocalDate;
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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "citizen")
@EqualsAndHashCode(callSuper = true)
public class CitizenEntity extends BasePartitionedEntity<UUID> {

  @EmbeddedId
  private PartitionKey key;

  @Column(name = "first_name", nullable = false, length = 50)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 50)
  private String lastName;

  @Column(name = "full_name", nullable = false, length = 150)
  private String fullName;

  @Column(name = "birthday", nullable = false)
  private LocalDate birthday;

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
