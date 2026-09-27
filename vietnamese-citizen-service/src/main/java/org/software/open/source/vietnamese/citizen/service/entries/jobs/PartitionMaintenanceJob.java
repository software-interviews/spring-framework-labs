package org.software.open.source.vietnamese.citizen.service.entries.jobs;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.software.open.source.vietnamese.citizen.service.services.PartitionMaintenanceService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "partition.maintenance.enabled", havingValue = "true", matchIfMissing = true)
public class PartitionMaintenanceJob {

  private final PartitionMaintenanceService partitionMaintenanceService; // interface, không phải Impl

  @Scheduled(cron = "${partition.maintenance.cron-job.partition:0 59 * * * *}")
  public void run() {
    log.info("PartitionMaintenanceJob start at: ", ZonedDateTime.now(ZoneId.of("UTC")));
    partitionMaintenanceService.maintain();
    log.info("PartitionMaintenanceJob end at: ", ZonedDateTime.now(ZoneId.of("UTC")));
  }

}
