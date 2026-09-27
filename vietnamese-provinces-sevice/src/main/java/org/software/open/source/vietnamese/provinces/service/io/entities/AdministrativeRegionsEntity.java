package org.software.open.source.vietnamese.provinces.service.io.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "administrative_regions")
public class AdministrativeRegionsEntity {

  @Id
  private Integer id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "name_en", nullable = false)
  private String nameEn;

  @Column(name = "code_name")
  private String codeName;

  @Column(name = "code_name_en")
  private String codeNameEn;

}
