## 1. THÔNG TIN SINH VIÊN
* **Họ và tên:** Châu Thị Ngọc Quyên
* **Mã số sinh viên (MSSV):** 1150080113
* **Môn học:** An toàn hệ thống thông tin


# BÁO CÁO TIẾN ĐỘ THỰC HÀNH LAB 8 / LAB 9: LOCAL DNS ATTACK
**Môn học:** Thực hành An toàn Hệ thống Thông tin  
**Môi trường ảo hóa:** VMware Workstation Pro  
**Hệ điều hành tích hợp:** pfSense Community Edition, Ubuntu Desktop/Server 24.04, Kali Linux, DMZ Services.

---

# BÁO CÁO TIẾN TRÌNH THỰC HÀNH LAB: DNS SPOOFING & CACHE POISONING

## 1. Môi trường & Hạ tầng mạng (VMware Workstation)
* **Phân đoạn mạng**: `VMnet9` (Mạng cô lập / Isolated Custom Network)
* **Firewall / Router**: pfSense 
  * Interface `DNSLAB`: `10.9.0.1/24`
* **DNS-SERVER**: Ubuntu Server
  * IP tĩnh: `10.9.0.10/24`
  * Gateway: `10.9.0.1`
  * Dịch vụ: BIND 9 (DNS Server phân giải thuận & nghịch)
* **Client-B (Victim)**: Windows 10
  * IP tĩnh: `10.9.0.100/24`
  * Gateway: `10.9.0.1`
  * Preferred DNS Server: `10.9.0.10`
* **Attacker**: Kali Linux
  * IP tĩnh: `10.9.0.200/24`
  * Gateway: `10.9.0.1`
  * Dịch vụ: Web Server giả mạo (Python HTTP server trên cổng 80)
* **DMZ**: Máy chủ hợp lệ / Authoritative DNS
  * IP: `172.16.6.20` (AUTH-DNS: `auth_dns_delayed.py`)
  * Web Server gốc: `172.16.6.10`

---

## 2. Các cấu hình cốt lõi đã hoàn tất

### A. Cấu hình BIND9 trên DNS-SERVER (10.9.0.10)
* **File `/etc/bind/named.conf.options`**:
  * Thiết lập `recursion yes;` và giới hạn `allow-recursion { 10.9.0.0/24; localhost; };`.
  * Lắng nghe cổng 53: `listen-on port 53 { 127.0.0.1; 10.9.0.10; };`.
  * Tắt kiểm tra bảo mật: `dnssec-validation no;` và tắt cookie máy chủ ngoài: `server 172.16.6.20 { send-cookie no; };`.
* **File `/etc/bind/named.conf.local`**:
  * Khai báo zone thuận: `lab9.test` (sử dụng file `/etc/bind/db.lab9.test`).
  * Khai báo zone nghịch chuẩn cho dải `/24`: `0.9.10.in-addr.arpa` (sử dụng file `/etc/bind/db.10.9.0`).
  * Khai báo zone chuyển tiếp: `external.lab9.test` forward về Authoritative DNS `172.16.6.20`.
* **Cơ sở dữ liệu Zone (`db.lab9.test` & `db.10.9.0`)**:
  * Đã ánh xạ đầy đủ bản ghi A và PTR cho các nút: `ns` / `www` (`10.9.0.10`), `victim` (`10.9.0.100`), `kali` (`10.9.0.200`).

### B. Cấu hình Client-B (Windows 10)
* Thiết lập adapter mạng nhận diện đúng phân đoạn `VMnet9`.
* Cấu hình tĩnh thông số IPv4: `10.9.0.100`, subnet `255.255.255.0`, gateway `10.9.0.1`, DNS `10.9.0.10`.
* Kiểm tra thông tuyến: Ping thành công tới `10.9.0.10` (`time<1ms TTL=64`).
* Kiểm tra phân giải: `nslookup www.lab9.test` nhận diện máy chủ `Server: www.lab9.test` và trả về `Address: 10.9.0.10`.

### C. Cấu hình máy Attacker (Kali Linux)
* Đã gán IP tĩnh cho card mạng: `10.9.0.200/24`.
* Đã chuẩn bị thư mục chứa mã nguồn web giả mạo `~/fake_site/index.html` và sẵn sàng chạy web server trên cổng 80 (`python3 -m http.server 80`).

---

## 3. Nhật ký xử lý sự cố (Troubleshooting Log)
1. **Lỗi tải gói trên Ubuntu do IP tĩnh không khớp card NAT**:
   * *Hiện tượng*: `Destination Host Unreachable` khi ping `8.8.8.8` để cài BIND9.
   * *Xử lý*: Chuyển tạm thời sang DHCP trên card NAT, chạy `sudo ip addr flush dev ens33`, nạp lại Netplan để tải gói `bind9` và `dnsutils`, sau đó trả về `VMnet9` với IP tĩnh `10.9.0.10`.
2. **Lỗi cú pháp cấu hình BIND9 (`named.conf.local`)**:
   * *Hiện tượng*: Lỗi `missing ';'` và `unknown option 'external.lab9.test'`.
   * *Nguyên nhân*: Thiếu dấu ngoặc kép đóng `"` và sai chính tả từ khóa `forwarders`.
   * *Xử lý*: Bổ sung dấu `"` vào `db.10.9.0";` và sửa thành `forwarders { 172.16.6.20; };`.
3. **Lỗi `Server: UnKnown` và Timeout 2 giây trên Windows 10**:
   * *Nguyên nhân*: Khai báo thiếu octet trong tên zone phân giải nghịch (`9.10.in-addr.arpa` thay vì `0.9.10.in-addr.arpa`).
   * *Xử lý*: Đổi tên zone trong `named.conf.local` thành `0.9.10.in-addr.arpa`, giúp Windows tra cứu thành công bản ghi PTR.

---

## 4. Tiến trình bài tập (Tasks Progress)

### [ĐÃ HOÀN THÀNH] Task 1: Thay đổi độ ưu tiên phân giải bằng file Hosts
* Thêm bản ghi `10.9.0.200    www.lab9.test` vào `C:\Windows\System32\drivers\etc\hosts`.
* Gửi truy vấn kiểm tra: `ping www.lab9.test` tự động trỏ về IP của Kali `10.9.0.200`.
* Kiểm chứng độ ưu tiên: `curl http://www.lab9.test` định tuyến sang Attacker Server, trong khi `nslookup www.lab9.test 10.9.0.10` vẫn truy vấn trực tiếp và nhận IP chuẩn `10.9.0.10`.
* Dọn dẹp: Đã xóa bản ghi giả mạo trong file `hosts` và chạy `ipconfig /flushdns`.

---

## 5. Các bước triển khai tiếp theo (Next Steps)
1. **Chuẩn bị Baseline cho Task 2**:
   * Khởi chạy `auth_dns_delayed.py` trên máy DMZ (`172.16.6.20`).
   * Xóa cache trên DNS-SERVER: `sudo rndc flush`.
   * Kiểm tra truy vấn từ Client-B: `nslookup www.external.lab9.test` phân giải ra đúng IP DMZ `172.16.6.10`.
2. **Task 2: Trực tiếp giả mạo phản hồi DNS (Direct DNS Spoofing)**:
   * Chạy script giả lập bắt và phản hồi gói tin DNS từ Kali Linux (`dns_spoof_lab9.py`).
   * Gửi truy vấn từ Victim để kiểm tra việc nhận phản hồi giả trước khi Authoritative DNS thật kịp phản hồi.
3. **Task 3: Đầu độc bộ nhớ đệm DNS (DNS Cache Poisoning)**:
   * Thực hiện kỹ thuật làm nhiễm độc cache trên máy DNS-SERVER, buộc server lưu trữ IP giả mạo trong TTL quy định.