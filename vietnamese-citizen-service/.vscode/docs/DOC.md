```java
package org.software.open.source.vietnamese.citizen.service.io.entities;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

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
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    if (getCreatedDate() == null) {
      setCreatedDate(now);
    }
    this.updatedDate = now;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedDate = OffsetDateTime.now(ZoneOffset.UTC);
  }

}
```

---


```java
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
```

---

```java
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
```

---

```java
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
```