package com.codegym.store.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "storage_drive")
@Data
@EqualsAndHashCode(callSuper = true)
public class Storage extends Product {
    private String storageType; // SSD hoặc HDD
    private String brand; // Hãng
    private String capacity; // Dung lượng (VD: 1 TB, 500 GB)
    
    // Thuộc tính dành riêng cho SSD
    private String connectionStandard; // Chuẩn kết nối (SATA 3, M.2 NVMe, v.v.)
    private String pcieStandard; // Chuẩn PCIe (VD: PCIe 3.0, PCIe 4.0)
    private Integer readSpeed; // Tốc độ đọc (MB/s)
    private Integer writeSpeed; // Tốc độ ghi (MB/s)
    
    // Thuộc tính dành riêng cho HDD
    private String rpm; // Tốc độ vòng quay (VD: 7200 RPM)
    
    // Thuộc tính chung cho cả hai loại
    private Integer cache; // Bộ nhớ đệm (MB)
}
