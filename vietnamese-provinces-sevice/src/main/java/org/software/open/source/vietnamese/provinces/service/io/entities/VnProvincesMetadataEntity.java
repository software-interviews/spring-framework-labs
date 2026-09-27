package org.software.open.source.vietnamese.provinces.service.io.entities;

import java.time.Instant;

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
@Table(name = "vn_provinces_metadata")
public class VnProvincesMetadataEntity {

  @Id
  @Column(name = "dataset_version", nullable = false)
  private String datasetVersion;

  @Column(name = "latest_decree")
  private String latestDecree;

  @Column(name = "generated_at", nullable = false)
  private Instant generatedAt;

}
