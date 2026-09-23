# BÁO CÁO TÓM TẮT THỰC HÀNH LAB 3
## NHẬN DIỆN VÀ ỨNG PHÓ CÁC MỐI ĐE DỌA AN TOÀN THÔNG TIN

- **Môi trường:** Windows 10 VM (Host-only / tạm chuyển NAT tại TH5 để kiểm thử gói tin TLS).
- **Phương thức báo cáo:** Chụp ảnh màn hình thao tác trực tiếp và dán vào báo cáo bài nộp.

---

## 1. Tóm tắt kết quả thực hiện

| Tình huống | Kỹ thuật / Thao tác thực tế | Kết quả đạt được |
| :--- | :--- | :--- |
| **TH1 & TH2** | Kiểm thử mã độc với chuỗi chuẩn EICAR; giám sát phản ứng bảo vệ. | Microsoft Defender nhận diện và cách ly mẫu thử ngay lập tức; Sysmon ghi nhận sự kiện tạo file (Event ID 11). |
| **TH3** | Cấu hình kiểm toán đăng nhập (Audit Policy) và quản lý tài khoản người dùng `lab3user`. | Ghi nhận chính xác sự kiện xác thực thất bại (Event 4625) khi gõ sai/chưa cập nhật mật khẩu và xác thực thành công (Event 4624) sau khi đổi mật khẩu mới. |
| **TH4** | Thiết lập cơ chế tự khởi động (Persistence) và chạy tiến trình ngầm lắng nghe cổng mạng. | Tạo thành công Run key và Scheduled Task; chạy HTTP Server nội bộ cổng 8080 (127.0.0.1); truy vết chính xác PID, đường dẫn và tham số tiến trình qua Process Explorer và Autoruns. |
| **TH5** | Bắt gói tin (Sniffing) so sánh giao thức HTTP bản rõ và giao thức bảo mật HTTPS (TLS). | Wireshark bắt trọn chuỗi tham số nhạy cảm `TRAINING_ONLY` trên HTTP ở URL; trong khi qua HTTPS cổng 443, dữ liệu được bảo vệ an toàn dưới dạng byte mã hóa (`Encrypted Application Data`). |
| **TH6** | Kiểm thử DoS tải cục bộ và phân tích dữ liệu DDoS, Mail Bombing. | Script tải cục bộ gửi thành công 50 request tới 127.0.0.1:8080 (`ok=50`, `failures=0`); phân tích các dải IP nguồn phân tán của DDoS; chỉ ra lượng thư gửi đột biến từ một địa chỉ trong Mail Bombing. |
| **TH7** | Nhận diện dấu hiệu lừa đảo và phân loại các hình thức Tấn công phi kỹ thuật (Social Engineering). | Chỉ ra 5 chỉ dấu bất thường trong mẫu email lừa đảo (khẩn cấp, domain lạ, lệch Reply-To...); phân loại chính xác 6 kịch bản tấn công (Phishing, Spear Phishing, Pretexting, Baiting, Quid Pro Quo, Watering Hole). |
| **TH8** | Dọn dẹp dấu vết (Cleanup), phục hồi hệ thống và niêm phong tính toàn vẹn chứng cứ. | Xóa sạch Run key, Scheduled Task, tài khoản thử nghiệm và dừng HTTP server; xác minh Defender vẫn bật bảo vệ thời gian thực; tính mã băm SHA-256 niêm phong toàn bộ dữ liệu minh chứng. |

---

## 2. Các kỹ thuật an toàn thông tin cốt lõi đã áp dụng

1. **System Auditing & Threat Detection:**
   - Cấu hình chính sách kiểm toán Windows (`auditpol`) để theo dõi các sự kiện đăng nhập và xác thực danh tính.
   - Giám sát tiến trình và tệp tin hệ thống thời gian thực thông qua công cụ **Sysmon**.

2. **Persistence Analysis & Process Forensics:**
   - Phân tích cơ chế mã độc duy trì kiểm soát hệ thống thông qua Windows Registry Run key và Task Scheduler.
   - Định danh, truy vết tiến trình mạng độc hại (Command Line, PID, Publisher) bằng **Process Explorer** và **Autoruns**.

3. **Traffic Sniffing & Cryptographic Protection:**
   - Sử dụng **Wireshark** để bắt và giải mã cấu trúc gói tin mạng.
   - Đánh giá nguy cơ rò rỉ dữ liệu khi truyền thông qua giao thức không mã hóa (HTTP) và vai trò của TLS/HTTPS trong việc đảm bảo tính bí mật dữ liệu.

4. **Resource Exhaustion & Log Analysis:**
   - Phân biệt bản chất giữa tấn công từ chối dịch vụ đơn nguồn (DoS) và đa nguồn phân tán (DDoS).
   - Phân tích nhật ký hệ thống để phát hiện các dấu hiệu tấn công làm cạn kiệt tài nguyên (Mail Bombing).

5. **Human Factor & Social Engineering Defense:**
   - Nhận diện các kỹ thuật tâm lý học đánh vào con người (sợ hãi, cấp bách, tò mò, lòng tham) trong các cuộc tấn công phi kỹ thuật.
   - Đưa ra biện pháp phòng vệ tương ứng (xác thực đa yếu tố chống lừa đảo, nâng cao nhận thức an toàn thông tin).

6. **Incident Cleanup & Digital Evidence Integrity:**
   - Thực hiện quy trình gỡ bỏ triệt để các dấu vết xâm nhập mà vẫn duy trì cơ chế phòng vệ của hệ điều hành.
   - Áp dụng thuật toán băm mật mã học (**SHA-256**) để đảm bảo tính toàn vẹn, chống can thiệp sửa đổi dữ liệu chứng cứ số.