package org.software.open.source.vietnamese.provinces.service.io.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "provinces", indexes = {
    @Index(name = "idx_provinces_unit", columnList = "administrative_unit_id")
})
public class ProvincesEntity {

  @Id
  private String code;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "name_en")
  private String nameEn;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "full_name_en")
  private String fullNameEn;

  @Column(name = "code_name")
  private String codeName;

  @Column(name = "postal_code_prefix")
  private String postalCodePrefix;

  @Column(name = "administrative_unit_id")
  private Integer administrativeUnitId;

}
