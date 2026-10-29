## 1. THÔNG TIN SINH VIÊN
* **Họ và tên:** Châu Thị Ngọc Quyên
* **Mã số sinh viên (MSSV):** 1150080113
* **Môn học:** An toàn hệ thống thông tin


# BÁO CÁO TIẾN ĐỘ THỰC HÀNH LAB 8 / LAB 9: LOCAL DNS ATTACK
**Môn học:** Thực hành An toàn Hệ thống Thông tin  
**Môi trường ảo hóa:** VMware Workstation Pro  
**Hệ điều hành tích hợp:** pfSense Community Edition, Ubuntu Desktop/Server 24.04, Kali Linux, DMZ Services.

---

## 1. Bảng Phân Bổ Mạng & Ánh Xạ Máy Ảo

| Tên VM trên VMware | Vai trò trong bài Lab | Card mạng kết nối (VMware) | Địa chỉ IPv4 gán tĩnh | Ghi chú cấu hình |
| :--- | :--- | :--- | :--- | :--- |
| **pfSense** | Gateway / Firewall | Adapter 5: **VMnet9** | `10.9.0.1/24` | Gateway mặc định của mạng DNS-LAB |
| **Ubuntu64-bit** (Clone) | **DNS-SERVER** (BIND9) | **VMnet9** | `10.9.0.10/24` | Recursive Resolver & Local Master Zone |
| **Client-B** | **VICTIM** | **VMnet9** | `10.9.0.100/24` | DNS trỏ duy nhất về `10.9.0.10` |
| **kali-linux-...** | **ATTACKER** | **VMnet9** | `10.9.0.200/24` | Chạy Scapy spoofing, Web Server giả lập |
| **WEB-PROXY** / VM DMZ | **APP-TEST / AUTH-DNS** | DMZ Network | `172.16.6.20/24` | Authoritative DNS mô phỏng độ trễ 1.5s |
| **DMZ-WEB** | Web mục tiêu hợp lệ | DMZ Network | `172.16.6.10/24` | Chạy Web Server gốc (cổng 80) |

---

## 2. Các Bước Đã Hoàn Thành

### Bước 1: Thiết lập mạng & pfSense Interface
- [x] Tạo dải mạng nội bộ riêng **VMnet9** (Host-only, tắt DHCP tích hợp).
- [x] Thêm card mạng thứ 5 cho pfSense kết nối tới **VMnet9**.
- [x] Gán interface mới mang tên **`DNSLAB`** với IP: `10.9.0.1/24` (không thiết lập Upstream Gateway).
- [x] Thiết lập 5 firewall rules cô lập cho interface `DNSLAB`:
  1. `Pass` UDP/TCP 53 từ `10.9.0.10` sang `172.16.6.20` (DNS đệ quy ra ngoài).
  2. `Pass` TCP 80 từ `10.9.0.0/24` sang `172.16.6.10` (Victim vào web DMZ hợp lệ).
  3. `Block + Log` từ `10.9.0.0/24` sang `10.6.0.0/24` (Bảo vệ mạng LAN-DOMAIN).
  4. `Block + Log` từ `10.9.0.0/24` sang `192.168.56.0/24` (Bảo vệ MGMT).
  5. `Block + Log` từ `10.9.0.0/24` sang `any` (Default isolate rule).

---

### Bước 2: Cấu hình DNS-SERVER (10.9.0.10)
- [x] Gán IP tĩnh `10.9.0.10/24`, Gateway `10.9.0.1` qua Netplan.
- [x] Cài đặt các gói `bind9` và `dnsutils`.
- [x] Cấu hình file `/etc/bind/named.conf.options`:
  - Cho phép phân giải đệ quy cho dải `10.9.0.0/24`.
  - Thiết lập `server 172.16.6.20 { send-cookie no; };` để tái lập kịch bản tấn công on-path.
- [x] Cấu hình file `/etc/bind/named.conf.local`:
  - Khai báo Master zone thuận: `lab9.test` trỏ file `/etc/bind/db.lab9.test`.
  - Khai báo Master zone nghịch: `9.10.in-addr.arpa` trỏ file `/etc/bind/db.10.9.0`.
  - Khai báo Forward zone: `external.lab9.test` forward tới `172.16.6.20`.
- [x] Đã tạo file dữ liệu các zone `db.lab9.test` và `db.10.9.0`.
- [x] Đã kiểm tra cú pháp cấu hình thành công:
  - `sudo named-checkconf` (Không còn lỗi cú pháp).

---

## 3. Các Bước Cần Thực Hiện Tiếp Theo

### Bước 3: Hoàn tất khởi động BIND9 & Baseline trên DNS-SERVER
Chạy các lệnh kiểm tra zone và khởi động dịch vụ:
```bash
sudo named-checkzone lab9.test /etc/bind/db.lab9.test
sudo named-checkzone 9.10.in-addr.arpa /etc/bind/db.10.9.0
sudo systemctl restart bind9
sudo ss -lunpt | grep :53
```
Kiểm tra phân giải baseline nội bộ (📸 **Chụp ảnh minh chứng**):
```bash
dig @10.9.0.10 www.lab9.test A +short
dig @10.9.0.10 -x 10.9.0.10 +short
```

---

### Bước 4: Thiết lập máy VICTIM & ATTACKER
1. **VICTIM (10.9.0.100 - Client-B):**
   - Đặt IP tĩnh: `10.9.0.100/24`, Gateway `10.9.0.1`, DNS: `10.9.0.10`.
   - Kiểm tra `resolvectl query www.lab9.test`.
2. **ATTACKER (10.9.0.200 - Kali Linux):**
   - Đặt IP card VMnet9: `10.9.0.200/24`.
   - Bật web server giả lập:
     ```bash
     mkdir -p ~/LAB9/fake_site
     cp fake_index.html ~/LAB9/fake_site/index.html
     sudo python3 -m http.server 80 --directory ~/LAB9/fake_site
     ```

---

### Bước 5: Baseline Hệ Thống
1. Trên máy APP-TEST (`172.16.6.20`), khởi chạy:
   ```bash
   sudo python3 auth_dns_delayed.py --bind 172.16.6.20 --delay 1.5
   ```
2. Trên DNS-SERVER: `sudo rndc flush`.
3. Trên máy VICTIM:
   ```bash
   dig @10.9.0.10 www.external.lab9.test A +noall +answer
   curl http://www.external.lab9.test
   ```
   *Yêu cầu:* Kết quả bắt buộc phải trả về IP gốc **`172.16.6.10`** (📸 **Chụp ảnh minh chứng**).

---

### Bước 6: Thực hiện 3 kịch bản tấn công (Attack Phase)
* **Task 1:** Chỉnh sửa file `/etc/hosts` trên máy Victim trỏ `www.lab9.test` về `10.9.0.200`. So sánh giữa `curl` (vào site giả) và `dig` (vẫn ra IP thật).
* **Task 2 (Direct DNS Spoofing):** Chạy `dns_spoof_lab9.py --mode victim` trên Kali để giả mạo phản hồi trả trực tiếp về máy Victim.
* **Task 3 (DNS Cache Poisoning):** Chạy `dns_spoof_lab9.py --mode resolver` trên Kali để đầu độc trực tiếp bộ nhớ đệm cache của DNS-SERVER BIND9.