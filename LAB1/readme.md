# BÁO CÁO TỔNG KẾT LAB 1: QUẢN TRỊ TỪ XA VÀ BẢO MẬT KÊNH TRUYỀN (SSH VS. TELNET)

- **Sinh viên thực hiện:** Châu Thị Ngọc Quyên
- **Mã số sinh viên (MSSV):** 1150080113
- **Tài liệu tham khảo/Video:** [https://youtu.be/kyWBpFW3zXI](https://youtu.be/kyWBpFW3zXI)

---

## 1. Môi trường và Cấu hình Mạng

Hệ thống giả lập gồm 3 máy ảo kết nối trong cùng dải mạng nội bộ
- **Windows Server 2022 (Server):** IP `192.168.90.11/24`, cấu hình dịch vụ OpenSSH Server (port 22)[cite: 11]. Tạo tài khoản thực hành: `chauthingocquyen`
- **Windows 10 (Client):** IP `192.168.90.12/24`, cài đặt tính năng Telnet Client và dùng OpenSSH Client để kiểm thử kết nối
- **Kali Linux (Attacker / Sniffer / Server test):** IP `192.168.90.13/24`, sử dụng Wireshark để phân tích lưu lượng và mở socket lắng nghe cổng 23 (Telnet)

---

## 2. Tóm tắt Kết quả Thực nghiệm

| Tiêu chí | SSH (Secure Shell - Cổng 22) | Telnet (Cổng 23) |
| :--- | :--- | :--- |
| **Bảo vệ kênh truyền** | Dữ liệu phiên được mã hóa toàn diện sau bước bắt tay trao đổi khóa[cite: 11]. | Hoàn toàn **không mã hóa** (truyền bản rõ - cleartext) |
| **Kết quả bắt gói (Wireshark)** | Payload hiển thị chuỗi byte nhị phân/ngẫu nhiên (`Encrypted packet`), không thể đọc được nội dung | Khôi phục nguyên vẹn toàn bộ username (`chauthingocquyen`), password (`@1150080113Q`) và lệnh thực thi (`whoami`) qua *Follow TCP Stream* |
| **Thông tin bị lộ (Metadata)** | Lộ IP nguồn/đích, cổng 22, kích thước gói tin và chuỗi banner phiên bản phần mềm | Lộ toàn bộ từ siêu dữ liệu mạng (Metadata) đến thông tin xác thực nhạy cảm |
| **Đánh giá mức độ an toàn** | Đảm bảo tính bí mật (Confidentiality) và tính toàn vẹn (Integrity); an toàn cho môi trường thực tế | Tiềm ẩn rủi ro cao trước các tấn công nghe lén (Sniffing) và tấn công đứng giữa (MitM) |

---

## 3. Kiến thức & Kỹ thuật Cốt lõi

1. **Sniffing & TCP Stream Analysis:**
   - Sử dụng **Wireshark** lọc theo cổng `tcp.port == 22` và `tcp.port == 23`
   - Sử dụng tính năng **Follow TCP Stream** để kiểm chứng khả năng rò rỉ dữ liệu phiên làm việc
2. **Nguyên lý Mật khẩu vs. Mã hóa đường truyền:**
   - Việc đặt mật khẩu dài/phức tạp chỉ phòng chống tấn công dò quét (Brute-force) khi bẻ khóa offline
   - Với các giao thức không mã hóa kênh truyền như Telnet, mật khẩu phức tạp vẫn bị trích xuất trực tiếp nguyên vẹn qua payload gói tin
3. **Xác thực và Bảo mật SSH:**
   - **Host Key Fingerprint:** Dùng để xác minh danh tính máy chủ, ngăn chặn tấn công giả mạo máy chủ (Rogue Server/MitM)
   - **Public-key Authentication (Cơ chế Challenge-Response):** Phương thức xác thực an toàn thay thế mật khẩu, sử dụng cặp khóa Private/Public key giúp chống lại hoàn toàn tấn công Brute-force
4. **Biện pháp Hardening cho SSH trong thực tế:**
   - Chặn đăng nhập trực tiếp bằng quyền Root/Administrator
   - Tắt cơ chế xác thực bằng mật khẩu (`PasswordAuthentication no`), bắt buộc sử dụng SSH Key
   - Thay đổi cổng mặc định (đổi cổng 22) và triển khai giải pháp tự động chặn IP như Fail2ban