package org.software.open.source.vietnamese.citizen.service.services.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.regex.Pattern;

import org.software.open.source.vietnamese.citizen.service.services.PartitionMaintenanceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartitionMaintenanceServiceImpl implements PartitionMaintenanceService {

  private static final Pattern SAFE_TABLE_NAME = Pattern.compile("^[a-z_][a-z0-9_]*$");

  private static final String SQL_MAINTAIN = "SELECT create_upcoming_partitions(?, ?)";

  private final JdbcTemplate jdbcTemplate;

  @Value("${partition.maintenance.tables:citizen,address}")
  private List<String> tables;

  @Value("${partition.maintenance.num-intervals:1}")
  private int numIntervals;

  @Override
  public void maintain() {
    jdbcTemplate.execute(
        (Connection con) -> con.prepareStatement(SQL_MAINTAIN),
        (PreparedStatement ps) -> {
          ps.setInt(2, numIntervals); // set MỘT lần, ngoài vòng lặp
          for (String table : tables) {
            if (!SAFE_TABLE_NAME.matcher(table).matches()) {
              log.error("Rejecting unsafe table name from config: '{}'", table);
              continue;
            }
            try {
              ps.setString(1, table); // chỉ phần biến thiên mới ở trong loop
              ps.execute();
              log.info("Partition maintenance OK for table: {}", table);
            } catch (Exception e) {
              log.error("Partition maintenance FAILED for table: {}", table, e);
            }
          }
          return Boolean.TRUE;
        });
  }

}
