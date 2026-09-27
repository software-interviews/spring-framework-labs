package org.software.open.source.vietnamese.citizen.service.services;

public interface PartitionMaintenanceService {

  /**
   * Đảm bảo các partition cho giờ hiện tại và giờ kế tiếp luôn tồn tại.
   * Idempotent - gọi lại bao nhiêu lần cũng an toàn.
   */
  void maintain();

}
