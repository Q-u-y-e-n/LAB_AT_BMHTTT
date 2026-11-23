# LAB 3: MÃ HÓA KHÓA CÔNG KHAI & CHỮ KÝ SỐ (JAVA & SQL SERVER)

Ứng dụng Java Swing xây dựng bằng Maven minh họa các thuật toán mật mã khóa công khai (**RSA**, **ECC**, **Hệ mã lai RSA + AES-GCM**, **Chữ ký số ECDSA**), tích hợp lưu trữ và tra cứu nhật ký thao tác trên **Microsoft SQL Server**.

---

## 1. Chức năng chính

* **Bài 3.1 – RSA Console (`RSADemo.java`):** Tạo cặp khóa RSA `2048`/`3072 bit`; mã hóa và giải mã chuẩn `RSA-OAEP (SHA-256)`; đo thời gian sinh khóa và kiểm tra giới hạn kích thước bản rõ ($190\text{ byte}$ và $318\text{ byte}$).
* **Bài 3.2 – RSA Giao diện (`frm_RSA.java`):** Mã hóa/giải mã trực quan và quan sát tính ngẫu nhiên của đệm OAEP qua các lần mã hóa cùng một bản rõ.
* **Bài 3.3 – Mật mã đường cong Elliptic (`frm_ECC.java`):** Sinh khóa trên đường cong `secp256r1` (P-256); mã hóa theo mô hình **ECIES** (`ECDH + HKDF-SHA256 + AES-256-GCM`); hỗ trợ lưu/nạp khóa (`Save Keys` / `Load Keys`) và phát hiện giải mã sai khóa (`AEADBadTagException`).
* **Bài 3.4 – Hệ mã lai RSA + AES (`frm_RSA_AES.java`):** Mã hóa văn bản dài ($> 1.000\text{ ký tự}$) bằng `AES-256-GCM` và bọc khóa phiên AES bằng `RSA-OAEP`; kiểm thử phát hiện bản mã bị chỉnh sửa.
* **Bài 3.5 – Chữ ký số ECDSA (`frm_ECC_Signature.java`):** Ký số bằng khóa riêng và xác minh bằng khóa công khai (`SHA256withECDSA`), phân biệt trạng thái `VALID` / `INVALID`.
* **Quản lý kết nối & Lịch sử (`frm_KetNoiSQL.java`, `frm_LichSu.java`):** Lưu thông tin kết nối SQL Server an toàn trong bộ nhớ phiên chạy và tự động ghi log vào bảng `dbo.History`.

---

## 2. Cấu trúc mã nguồn

```text
LAB12_MaHoaKhoaCongKhai/
├── pom.xml                          # Cấu hình Java 17 và thư viện mssql-jdbc
├── README.md                        # Tài liệu hướng dẫn dự án
└── src/main/java/week_03/
    ├── CryptoSupport.java           # Tiện ích Base64, UTF-8, AES-GCM, HKDF, tệp khóa
    ├── FileTextIO.java              # Đọc/ghi tệp văn bản .txt (UTF-8)
    ├── DbConfig.java                # Quản lý cấu hình kết nối SQL Server
    ├── HistoryDao.java              # Truy vấn INSERT/SELECT bảng dbo.History
    ├── RSACipher.java               # Xử lý thuật toán RSA-OAEP
    ├── RSADemo.java                 # Chương trình Console Bài 3.1
    ├── ECCCipher.java               # Xử lý thuật toán ECC (ECIES)
    ├── RSA_AES_Cipher.java          # Xử lý hệ mã lai RSA + AES-GCM
    ├── ECCSignature.java            # Xử lý ký số và xác minh ECDSA
    ├── frm_Menu.java                # Giao diện Menu chính của ứng dụng
    ├── frm_KetNoiSQL.java           # Giao diện kết nối SQL Server
    ├── frm_LichSu.java              # Giao diện xem lịch sử thao tác
    ├── frm_RSA.java                 # Giao diện Bài 3.2
    ├── frm_ECC.java                 # Giao diện Bài 3.3
    ├── frm_RSA_AES.java             # Giao diện Bài 3.4
    └── frm_ECC_Signature.java       # Giao diện Bài 3.5
```

---

## 3. Cài đặt Cơ sở dữ liệu (SQL Server)

1. Mở **SQL Server Configuration Manager**, bật **TCP/IP** và cấu hình cổng tĩnh **`14333`** tại mục `IPAll`, sau đó khởi động lại dịch vụ SQL Server.
2. Mở **SSMS** (đảm bảo đã bật chế độ xác thực **Mixed Mode**) và chạy script khởi tạo sau:

```sql
USE master;
GO
IF DB_ID(N'LAB3_MaHoaKhoaCongKhai') IS NULL
    CREATE DATABASE LAB3_MaHoaKhoaCongKhai;
GO
USE LAB3_MaHoaKhoaCongKhai;
GO
IF OBJECT_ID(N'dbo.History', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.History (
        Id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        Algorithm NVARCHAR(30) NOT NULL,
        Operation NVARCHAR(12) NOT NULL,
        InputLength INT NOT NULL,
        Success BIT NOT NULL,
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Lab3_History_CreatedAt DEFAULT SYSDATETIME(),
        CONSTRAINT CK_Lab3_InputLength CHECK (InputLength >= 0),
        CONSTRAINT CK_Lab3_Operation CHECK (Operation IN (N'ENCRYPT', N'DECRYPT', N'SIGN', N'VERIFY'))
    );
END;
GO

USE master;
GO
IF SUSER_ID(N'lab3_user') IS NULL
    CREATE LOGIN lab3_user WITH PASSWORD = N'LabUser@2026', CHECK_POLICY = ON;
GO
USE LAB3_MaHoaKhoaCongKhai;
GO
IF DATABASE_PRINCIPAL_ID(N'lab3_user') IS NULL
BEGIN
    CREATE USER lab3_user FOR LOGIN lab3_user;
    GRANT SELECT, INSERT ON OBJECT::dbo.History TO lab3_user;
END;
GO
```

---

## 4. Hướng dẫn chạy chương trình

* **Chạy Bài 3.1 (Console):** Chạy tệp `week_03/RSADemo.java`, nhập kích thước khóa (`2048` hoặc `3072`) và bản rõ `HELLO RSA` trên cửa sổ Console.
* **Chạy hệ thống giao diện chính:**
  1. Chạy tệp `week_03/frm_Menu.java`.
  2. Chọn **Kết nối SQL Server**, nhập mật khẩu tài khoản `lab3_user` (`LabUser@2026`) và bấm **Test Connection**.
  3. Mở lần lượt các chức năng **3.2 RSA**, **3.3 ECC**, **3.4 RSA + AES**, **3.5 Chữ ký ECDSA** để thực hiện thao tác.
  4. Chọn **Xem lịch sử** trên Menu chính (hoặc truy vấn bảng `dbo.History` trong SSMS) để kiểm tra dữ liệu đã lưu.

