## 1. THÔNG TIN SINH VIÊN
* **Họ và tên:** Châu Thị Ngọc Quyên
* **Mã số sinh viên (MSSV):** 1150080113
* **Môn học:** An toàn hệ thống thông tin


## LAB 7: PFSENSE WEB PROTECTION & DMZ PROXY

## 1. Thông Tin & Mô Hình Hệ Thống
* **Phân vùng mạng & Interface trên pfSense:**
  * **WAN:** VMnet8 (kết nối Internet)
  * **MGMT:** VMnet1 - Dải 192.168.56.0/24 (IP pfSense: 192.168.56.254)[cite: 1]
  * **LAN:** VMnet2 - Dải 10.6.0.0/24 (IP pfSense Gateway: 10.6.0.1)[cite: 1]
  * **DMZ:** VMnet3 - Dải 172.16.6.0/24 (IP pfSense Gateway: 172.16.6.1)[cite: 1]
* **Danh sách máy ảo thành phần:**
  * **pfSense:** Firewall, định tuyến và phân vùng mạng (WAN, LAN, MGMT, DMZ)[cite: 1].
  * **DC01 (Windows Server):** Quản trị Domain/DNS Server (10.6.0.10)[cite: 1].
  * **DMZ-WEB (Ubuntu Desktop):** Web Server đóng vai trò lưu trữ 13 domain mô phỏng và file kiểm thử (172.16.6.10)[cite: 1].
  * **WEB-PROXY (Ubuntu):** Proxy Server kiểm soát nội dung và bảo vệ web (10.6.0.30)[cite: 1].

---

## 2. Các Công Việc Đã Hoàn Thành

### Phase 1: Khởi tạo và thiết lập pfSense
* Khắc phục sự cố xác thực WebGUI do xung đột HTTPS session binding.
* Gán đầy đủ các cổng mạng: WAN, LAN, MGMT, DMZ[cite: 1].
* Cấu hình Firewall Aliases và hoàn tất bộ 7 quy tắc (Firewall Rules) điều hướng lưu lượng vùng LAN[cite: 1].
* Thiết lập Pass Rule trên tab DMZ (OPT2) cho phép lưu lượng mạng nội bộ kết nối tới gateway 172.16.6.1[cite: 1].

### Phase 2: Cấu hình DNS trên máy chủ DC01
* Thiết lập Forward Lookup Zone trên DC01 (10.6.0.10)[cite: 1].
* Trỏ bản ghi A cho toàn bộ 13 domain mô phỏng phục vụ kịch bản kiểm thử về IP của DMZ-WEB (172.16.6.10)[cite: 1].

### Phase 3: Cấu hình hạ tầng máy chủ DMZ-WEB
* **Xác định Interface:** Xác định đúng tên card mạng vật lý của máy ảo là ens33[cite: 1].
* **Cấu hình mạng Netplan:**
  * Xử lý lỗi cú pháp thụt lề YAML (Invalid YAML) và đồng bộ trình quản lý renderer: NetworkManager[cite: 4, 15].
  * Đặt IP tĩnh: 172.16.6.10/24, Gateway: 172.16.6.1, DNS: 10.6.0.10[cite: 1].
  * Gán đúng card mạng máy ảo vào VMnet3 trên VMware Workstation[cite: 1, 4].
* **Kiểm tra thông tuyến:** Ping thành công 100% (0% packet loss) từ DMZ-WEB tới gateway 172.16.6.1 của pfSense[cite: 7].

---

## 3. Các Vấn Đề Gặp Phải & Hướng Giải Quyết

| Vấn đề phát sinh | Nguyên nhân | Giải pháp đã xử lý / Phương án |
| :--- | :--- | :--- |
| **Không ping được Gateway DMZ**[cite: 6] | Rule mặc định của pfSense chặn cổng DMZ[cite: 1] và card mạng chưa khớp VMnet[cite: 4]. | Chuyển card sang VMnet3[cite: 4], thêm Pass Rule Allow Any trên giao diện DMZ của pfSense[cite: 1]. |
| **Lỗi cú pháp Netplan (Invalid YAML)**[cite: 4] | Thụt đầu dòng không chuẩn xác và dùng phím Tab. | Chuẩn hóa thụt lề bằng 2 khoảng trắng (Space) và thêm renderer: NetworkManager[cite: 1, 15]. |
| **Temporary failure resolving archive.ubuntu.com**[cite: 10] | DMZ-WEB bị cô lập trong vùng DMZ, chưa có Outbound NAT/Rule ra Internet[cite: 1, 16]. | Đổi tạm thời card mạng sang NAT (VMnet8) để cài đặt Nginx/VMware Tools[cite: 4], hoặc sử dụng Python Web Server có sẵn[cite: 1]. |
| **Lệnh sudo dhclient báo command not found**[cite: 16] | Ubuntu Desktop bản mới mặc định không cài isc-dhcp-client[cite: 16]. | Điều khiển card mạng qua tiện ích nmcli hoặc chuyển dhcp4: true trong file Netplan[cite: 15]. |

---

## 4. Kế Hoạch Tiếp Theo
1. Hoàn tất cài đặt Nginx trên DMZ-WEB và cấu hình Virtual Hosts đáp ứng cho 13 domain mô phỏng bài lab[cite: 1].
2. Tạo thư mục tải file và tạo mẫu thử mã độc kiểm thử EICAR (eicar.com.txt)[cite: 1].
3. Khởi chạy máy ảo WEB-PROXY, gán card mạng vào VMnet2 (LAN) và đặt IP tĩnh 10.6.0.30[cite: 1, 4].
4. Cài đặt các thành phần Proxy cốt lõi: Squid, E2Guardian và trình quét mã độc ClamAV[cite: 1].
