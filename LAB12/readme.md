# LAB 3: MÃ HÓA KHÓA CÔNG KHAI & CHỮ KÝ SỐ (JAVA & SQL SERVER)

Ứng dụng minh họa các nguyên lý và kỹ thuật mật mã khóa công khai (Asymmetric Cryptography) xây dựng bằng Java Swing (Maven), kết hợp ghi nhận nhật ký thao tác vào cơ sở dữ liệu Microsoft SQL Server thông qua JDBC.

---

## 1. Tính năng & Nội dung thực hành

Dự án bao gồm chương trình chạy trên Console và hệ thống giao diện đồ họa (Java Swing) tích hợp qua Menu chính (`frm_Menu`):

* **Bài 3.1 – Mật mã RSA Console (`RSADemo.java`):**
  * Tạo cặp khóa RSA kích thước `2048 bit` và `3072 bit`.
  * Mã hóa và giải mã thông điệp ngắn bằng chuẩn **RSA-OAEP với SHA-256** (`RSA/ECB/OAEPWithSHA-256AndMGF1Padding`).
  * Đo thời gian sinh khóa, độ dài bản mã Base64 và kiểm chứng giới hạn kích thước bản rõ ($190\text{ byte}$ với RSA-2048 và $318\text{ byte}$ với RSA-3072).
* **Bài 3.2 – Mật mã RSA Swing (`frm_RSA.java`):**
  * Giao diện trực quan cho phép chọn kích thước khóa, tạo khóa, mã hóa và giải mã.
  * Minh họa tính ngẫu nhiên của đệm OAEP (cùng một bản rõ khi mã hóa nhiều lần sẽ sinh ra các bản mã khác nhau nhưng đều giải mã về đúng bản rõ gốc).
* **Bài 3.3 – Mật mã đường cong Elliptic (`frm_ECC.java`):**
  * Sinh cặp khóa trên đường cong chuẩn **`secp256r1` (P-256)**.
  * Thực hiện mã hóa theo mô hình **ECIES**: trao đổi khóa **ECDH** tạo bí mật chung $\rightarrow$ dẫn xuất khóa phiên bằng **HKDF-SHA256** $\rightarrow$ mã hóa xác thực dữ liệu bằng **AES-256-GCM**.
  * Hỗ trợ lưu/nạp cặp khóa ra tệp văn bản (`Save Keys` / `Load Keys`) và kiểm thử từ chối giải mã khi dùng sai khóa riêng (`AEADBadTagException: Tag mismatch`).
* **Bài 3.4 – Hệ mã lai RSA + AES-GCM (`frm_RSA_AES.java`):**
  * Giải quyết nhược điểm giới hạn kích thước của RSA bằng cách dùng **AES-256-GCM** mã hóa văn bản dài ($> 1.000\text{ ký tự}$) và dùng **RSA-OAEP** mã hóa khóa phiên AES $32\text{ byte}$.
  * Tích hợp chức năng giả lập chỉnh sửa bản mã (`Sửa 1 ký tự bản mã`) để kiểm chứng khả năng phát hiện sai lệch toàn vẹn dữ liệu của AES-GCM.
* **Bài 3.5 – Chữ ký số ECDSA (`frm_ECC_Signature.java`):**
  * Ký số thông điệp bằng khóa riêng và xác minh bằng khóa công khai theo thuật toán **`SHA256withECDSA`**.
  * Kiểm thử trạng thái hợp lệ (`VALID`) và không hợp lệ (`INVALID`) khi thông điệp bị thay đổi hoặc dùng sai khóa công khai xác minh.
* **Tích hợp SQL Server (`frm_KetNoiSQL.java` & `frm_LichSu.java`):**
  * Quản lý cấu hình kết nối an toàn trong bộ nhớ (`DbConfig`), không lưu cứng mật khẩu vào mã nguồn.
  * Tự động ghi nhật ký các thao tác (`ENCRYPT`, `DECRYPT`, `SIGN`, `VERIFY`), thuật toán, độ dài đầu vào và trạng thái thành công/thất bại vào bảng `dbo.History`.

---

## 2. Cấu trúc Project

```text
LAB12_MaHoaKhoaCongKhai/
├── pom.xml                          # Khai báo thư viện mssql-jdbc và cấu hình Java 17
└── src/main/java/week_03/
    ├── CryptoSupport.java           # Hàm tiện ích: Base64, UTF-8, AES-256-GCM, HKDF-SHA256, xử lý tệp khóa
    ├── FileTextIO.java              # Hộp thoại mở/lưu tệp văn bản .txt (UTF-8)
    ├── DbConfig.java                # Lưu trữ thông tin kết nối SQL Server trong phiên chạy
    ├── HistoryDao.java              # Thao tác INSERT/SELECT bảng dbo.History qua PreparedStatement
    ├── RSACipher.java               # Xử lý thuật toán RSA-OAEP (2048/3072 bit)
    ├── RSADemo.java                 # Chương trình Console cho Bài 3.1
    ├── ECCCipher.java               # Xử lý thuật toán ECC (ECDH + HKDF + AES-GCM)
    ├── RSA_AES_Cipher.java          # Xử lý hệ mã lai RSA + AES-256-GCM
    ├── ECCSignature.java            # Xử lý ký số và xác minh ECDSA
    ├── frm_Menu.java                # Cửa sổ giao diện chính điều hướng toàn bộ chương trình
    ├── frm_KetNoiSQL.java           # Giao diện cấu hình và kiểm tra kết nối SQL Server
    ├── frm_LichSu.java              # Giao diện tra cứu lịch sử thao tác từ dbo.History
    ├── frm_RSA.java                 # Giao diện Bài 3.2
    ├── frm_ECC.java                 # Giao diện Bài 3.3
    ├── frm_RSA_AES.java             # Giao diện Bài 3.4
    └── frm_ECC_Signature.java       # Giao diện Bài 3.5
```

---

## 3. Yêu cầu hệ thống & Cài đặt CSDL

### Yêu cầu môi trường
* **JDK:** Java 17 trở lên.
* **IDE:** Eclipse / Apache NetBeans hỗ trợ Maven.
* **Database:** Microsoft SQL Server (đã bật giao thức **TCP/IP** tại cổng **`14333`** và chế độ xác thực **Mixed Mode**).

### Khởi tạo Cơ sở dữ liệu
Mở **SQL Server Management Studio (SSMS)** và thực thi đoạn script sau để tạo database `LAB3_MaHoaKhoaCongKhai`, bảng `dbo.History` và tài khoản `lab3_user`:

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

## 4. Hướng dẫn chạy & Kiểm thử

1. **Chạy Bài 3.1 (Console):**
   * Chạy tệp `week_03/RSADemo.java`.
   * Nhập kích thước khóa (`2048` hoặc `3072`) và bản rõ `HELLO RSA` trên cửa sổ Console để xem kết quả.
2. **Chạy hệ thống giao diện chính:**
   * Chạy tệp `week_03/frm_Menu.java`.
   * Chọn **Kết nối SQL Server**, nhập mật khẩu của `lab3_user` (`LabUser@2026`) và bấm **Test Connection**.
   * Mở lần lượt các chức năng **3.2 RSA**, **3.3 ECC**, **3.4 RSA + AES**, **3.5 Chữ ký ECDSA** để thực hiện mã hóa, giải mã, ký số và xác minh.
   * Chọn **Xem lịch sử** trên Menu chính (hoặc truy vấn trực tiếp bảng `dbo.History` trong SSMS) để kiểm tra nhật ký thao tác đã được ghi nhận.

---

## 5. Lưu ý bảo mật

* Không lưu trữ hoặc công khai khóa riêng (`PRIVATE=...`) thật và mật khẩu cơ sở dữ liệu lên kho mã nguồn công khai (GitHub/GitLab).
* Các tệp khóa xuất ra từ chương trình (`ecc_keys.txt`, `rsa_aes_keys.txt`, `ecdsa_keys.txt`) chỉ phục vụ mục đích thử nghiệm cục bộ.