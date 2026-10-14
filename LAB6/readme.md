## 1. THÔNG TIN SINH VIÊN
* **Họ và tên:** Châu Thị Ngọc Quyên
* **Mã số sinh viên (MSSV):** 1150080113
* **Môn học:** An toàn hệ thống thông tin


## XÁC THỰC ACTIVE DIRECTORY VÀ CHÍNH SÁCH TRUY CẬP TRÊN pfSense

# BÁO CÁO THỰC HÀNH: TÍCH HỢP pfSENSE VỚI ACTIVE DIRECTORY, CAPTIVE PORTAL VÀ KIỂM SOÁT TRUY CẬP THEO CHÍNH SÁCH

---

## 1. MỤC TIÊU VÀ TỔNG QUAN HỆ THỐNG
* **Xác thực tập trung qua LDAP:** Tích hợp pfSense với Active Directory Domain Services (AD DS) trên Windows Server để đồng bộ và xác thực người dùng mạng qua giao thức LDAP (TCP/389).
* **Kiểm soát truy cập qua Captive Portal:** Triển khai cổng xác thực Captive Portal trên vùng LAN, bắt buộc người dùng máy trạm phải đăng nhập bằng tài khoản miền trước khi được cấp quyền truy cập mạng.
* **Tường lửa lọc gói tin dựa trên chính sách (Policy-Based Filtering):**
  * Quản lý danh mục IP nguồn, IP đích và dải cổng dịch vụ thông qua **Aliases**.
  * Ràng buộc khung giờ có hiệu lực của các chính sách bằng **Schedules** (giờ hành chính và giờ nghỉ trưa).
  * Xây dựng thứ tự bộ luật theo nguyên tắc **First-match** nhằm chặn các đích nhạy cảm, cấp quyền truy cập theo điều kiện và đặt luật *Explicit Deny* ở cuối ruleset.
* **Cô lập vùng mạng DMZ:** Ngăn chặn các máy chủ dịch vụ đặt tại DMZ chủ động mở kết nối ngược vào vùng mạng nội bộ LAN.

---

## 2. KIẾN TRÚC MẠNG & PHÂN BỔ ĐỊA CHỈ IP

### 2.1. Phân vùng mạng (Network Segments)
* **Vùng MGMT (Host-Only / VMnet1):** `192.168.56.0/24`
  * pfSense MGMT Interface: `192.168.56.254` (Kênh truy cập WebGUI quản trị cách ly)
* **Vùng LAN (Internal / VMnet2):** `10.6.0.0/24`
  * Gateway (pfSense LAN Interface): `10.6.0.1`
  * Domain Controller / DNS Server: `10.6.0.10` (Domain: `quyen.local`)
  * Client-A: `10.6.0.21`
  * Client-B: `10.6.0.22`
* **Vùng DMZ (Internal / VMnet3):** `172.16.6.0/24`
  * Gateway (pfSense DMZ Interface): `172.16.6.1`
  * DMZ Web Server (Nginx đa IP ảo):
    * `GENERAL_SITE`: `172.16.6.10:80`
    * `JOB_SITE`: `172.16.6.20:80`
    * `SOCIAL_SITE`: `172.16.6.30:80`
    * `RESTRICTED_SITE`: `172.16.6.40:80`

---

## 3. CẤU HÌNH HỆ THỐNG CHI TIẾT

### 3.1. Cấu hình Active Directory (Windows Server)
* **Tổ chức thư mục (OU):** Tạo OU `Lab6_Users` trực thuộc domain `quyen.local`.
* **Tài khoản người dùng:**
  * `sv_a`: Thuộc OU `Lab6_Users`, mật khẩu thỏa mãn chính sách phức tạp, tùy chọn *Password never expires*.
  * `sv_b`: Thuộc OU `Lab6_Users`, mật khẩu thỏa mãn chính sách phức tạp, tùy chọn *Password never expires*.
  * `svc_pfsense_ldap`: Tài khoản dịch vụ người dùng thông thường đặt tại thư mục mặc định `Users`.

### 3.2. Cấu hình LDAP Server trên pfSense
* **Đường dẫn cấu hình:** `System` → `User Manager` → `Authentication Servers`
* **Các tham số thiết lập:**
  * **Type:** `LDAP`
  * **Hostname or IP address:** `10.6.0.10`
  * **Port value:** `389` (Transport: `TCP - Standard`)
  * **Search scope:** `Entire Subtree`
  * **Base DN:** `DC=quyen,DC=local`
  * **Authentication containers:** `OU=Lab6_Users,DC=quyen,DC=local`
  * **Bind user DN:** `CN=svc_pfsense_ldap,CN=Users,DC=quyen,DC=local`
  * **Initial Template:** `Microsoft AD` (User naming: `samAccountName`, Group member: `memberOf`)

### 3.3. Cấu hình Captive Portal
* **Đường dẫn cấu hình:** `Services` → `Captive Portal`
* **Zone Name:** `LAB6_USERS`
* **Interfaces:** `LAN`
* **Authentication Method:** `Use an Authentication backend`
* **Authentication Server:** Chọn `LAB6-AD`

### 3.4. Cấu hình Firewall Aliases & Schedules
* **Firewall Aliases:**
  * *IP Hosts:* `CLIENT_A` (`10.6.0.21`), `CLIENT_B` (`10.6.0.22`), `POLICY_CLIENTS` (`10.6.0.21`, `10.6.0.22`).
  * *Site Hosts:* `GENERAL_SITE` (`172.16.6.10`), `JOB_SITE` (`172.16.6.20`), `SOCIAL_SITE` (`172.16.6.30`), `RESTRICTED_SITE` (`172.16.6.40`).
  * *Ports:* `WEB_PORTS` (`80, 443`).
* **Firewall Schedules:**
  * `WORK_HOURS`: Thứ 2 – Thứ 6, từ 08:00 đến 17:00.
  * `LUNCH_WINDOW`: Thứ 2 – Thứ 6, từ 11:30 đến 13:00.
  * `TEST_NOW`: Khung giờ bao quanh thời điểm thực hành để phục vụ kiểm thử nhanh.

---

## 4. QUY TẮC TƯỜNG LỬA (RULESET)

### 4.1. Ruleset trên Interface LAN (Tuân thủ thứ tự First-Match)
1. **Anti-Lockout Rule (Mặc định):** Cho phép truy cập WebGUI/SSH quản trị.
2. **Cho phép dịch vụ nội bộ:** `Pass` | Protocol: `TCP/UDP` | Source: `LAN net` | Destination: `LAN address` | Ports: `8000 - 8002` (Hỗ trợ truy cập cổng chuyển hướng Captive Portal).
3. **Rule 1 (Block Job Site):** `Block` | Protocol: `TCP` | Source: `CLIENT_A` | Destination: `JOB_SITE` | Port: `80` | Log: `Bật`.
4. **Rule 2 (Block Restricted Site):** `Block` | Protocol: `TCP` | Source: `POLICY_CLIENTS` | Destination: `RESTRICTED_SITE` | Port: `80` | Log: `Bật`.
5. **Rule 3 (Allow Social trưa):** `Pass` | Protocol: `TCP` | Source: `CLIENT_B` | Destination: `SOCIAL_SITE` | Port: `80` | Schedule: `LUNCH_WINDOW`.
6. **Rule 4 (Block Social ngoài giờ):** `Block` | Protocol: `TCP` | Source: `CLIENT_B` | Destination: `SOCIAL_SITE` | Port: `80` | Log: `Bật`.
7. **Rule 5 (Allow General Site):** `Pass` | Protocol: `TCP` | Source: `POLICY_CLIENTS` | Destination: `GENERAL_SITE` | Port: `80`.
8. **Rule 6 (Allow Web giờ làm việc):** `Pass` | Protocol: `TCP` | Source: `POLICY_CLIENTS` | Destination: `Any` | Port: `WEB_PORTS` | Schedule: `WORK_HOURS`.
9. **Rule 7 (Explicit Deny):** `Block` | Protocol: `Any` | Source: `POLICY_CLIENTS` | Destination: `Any` | Log: `Bật`.

### 4.2. Ruleset trên Interface DMZ
* **Block DMZ to LAN:** `Block` | Protocol: `Any` | Source: `DMZ net` | Destination: `LAN net` | Log: `Bật` (Cách ly máy chủ DMZ không cho chủ động mở kết nối vào mạng nội bộ).

---

## 5. KẾT QUẢ THỰC NGHIỆM VÀ ĐỐI CHIẾU

| Kịch bản kiểm thử | Đối tượng / Đích | Kết quả mong đợi | Kết quả thực tế | Tình trạng |
| :--- | :--- | :--- | :--- | :---: |
| **Xác thực LDAP** | Test `sv_a`, `sv_b` trên Diagnostics | Thông báo thành công màu xanh | Ghi nhận tài khoản hợp lệ từ Active Directory | Đạt |
| **Captive Portal** | Máy Client truy cập Web HTTP | Chuyển hướng sang trang đăng nhập | Đăng nhập tài khoản miền thành công, mở phiên mạng | Đạt |
| **Kịch bản 1 (Job Site)** | Client-A → `172.16.6.20:80` | Bị chặn, ghi log | Trình duyệt báo Timeout; firewall ghi nhận log Block | Đạt |
| **Kịch bản 2 (Restricted)**| Policy Clients → `172.16.6.40:80` | Bị chặn, ghi log | Cả Client-A và Client-B đều bị chặn hoàn toàn | Đạt |
| **Kịch bản 3 (Social Site)**| Client-B → `172.16.6.30:80` | Cho phép trong giờ trưa, cấm ngoài giờ | Trong schedule truy cập được Nginx; ngoài schedule bị chặn và ghi log | Đạt |
| **Kịch bản 4 (DMZ Isolation)**| DMZ Server ping sang LAN (`10.6.0.x`) | 100% Packet loss, ghi log | Gói tin ICMP bị chặn hoàn toàn bởi rule DMZ | Đạt |
| **Kịch bản 6 (Block vs Reject)**| Phép thử so sánh cơ chế chặn TCP | Reject ngắt tức thì, Block chờ timeout | Reject trả TCP RST (lỗi kết nối tức thì < 1s); Block hủy gói âm thầm (chờ 20-30s) | Đạt |
| **Kịch bản 7 (State & Log)**| Kiểm tra nhật ký và bảng trạng thái | Ghi đúng IP/Port; quản lý state chặt chẽ | Bảng Firewall Logs phản ánh chính xác các luồng; State Table lưu phiên kết nối hợp lệ | Đạt |

---

## 6. PHÂN TÍCH KỸ THUẬT VÀ NGUYÊN LÝ HOẠT ĐỘNG
* **Cơ chế First-Match:** pfSense duyệt ruleset tuần tự từ trên xuống dưới. Ngay khi gói tin khớp với rule đầu tiên, hành động tương ứng sẽ được thực thi ngay lập tức và các rule bên dưới sẽ bị bỏ qua. Vì vậy, các luật cấm có tính chất cụ thể, hẹp (specific blocks) bắt buộc phải đặt lên phía trên các luật cho phép mở rộng (general pass).
* **Bản chất Stateful Firewall:** pfSense kiểm soát luồng gói tin theo trạng thái phiên. Khi một kết nối TCP được Pass, một bản ghi được lưu vào **State Table**. Mọi gói tin kế tiếp thuộc kết nối này sẽ được cho phép tự động dựa trên bảng State mà không cần kiểm tra lại ruleset. Do đó, khi chỉnh sửa luật tường lửa, cần thao tác **Reset States** để buộc các máy trạm thiết lập lại phiên kết nối mới theo đúng chính sách vừa thay đổi.
* **Nguyên tắc phân quyền tối thiểu (Least Privilege):** Việc phân tách mạng quản trị MGMT độc lập và sử dụng tài khoản dịch vụ riêng biệt `svc_pfsense_ldap` (thay vì Domain Administrator) giúp đảm bảo an toàn cho cơ sở dữ liệu Active Directory và chống leo thang đặc quyền trong trường hợp tường lửa bị xâm phạm.