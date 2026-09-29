# BÁO CÁO THỰC HÀNH LAB 4: KHẢO SÁT, GIÁM SÁT VÀ ĐÁNH GIÁ BỀ MẶT MẠNG BẰNG NMAP

## 1. THÔNG TIN SINH VIÊN
* **Họ và tên:** Châu Thị Ngọc Quyên
* **Mã số sinh viên (MSSV):** 1150080113
* **Lớp / Học phần:** An toàn hệ thống thông tin
* **Tên bài thực hành:** Khảo sát và đánh giá bề mặt mạng bằng Nmap

---

## 2. PHIÊN BẢN MÔI TRƯỜNG THỰC HÀNH THỰC TẾ
* **Phần mềm ảo hóa:** VMware Workstation 17.x Pro
* **Máy quét (Attacker/Scanner):**
  * Hệ điều hành: Kali Linux 2026.2 (64-bit)
  * Kernel: `Linux dbad51fa0e33 6.18.44 #1 SMP x86_64 GNU/Linux`
  * Công cụ: Nmap version 7.99, xsltproc, grep
  * Địa chỉ IP thực tế: `192.168.83.131/24` (Interface `eth0`)
* **Máy mục tiêu chính (Target 1):**
  * Hệ điều hành: Metasploitable 2 (Linux kernel 2.6.24-16-server)
  * Tài khoản mặc định: `msfadmin` / `msfadmin`
  * Địa chỉ IP thực tế: `192.168.83.132/24` (Interface `eth0`, MAC: `00:0C:29:3E:BC:7B`)
* **Máy mục tiêu phụ (Target 2):**
  * Hệ điều hành: Windows 10 x64 (Build 19045.3803)
  * Địa chỉ IP: `172.16.16.11` (Adapter Ethernet0)
* **Dải mạng phân tích:** `192.168.83.0/24` (Chế độ mạng cách ly Host-Only / VMnet)

---

## 3. CÁCH DỰNG VÀ THIẾT LẬP MÔI TRƯỜNG
1. **Thiết lập mạng ảo:**
   * Mở VMware Workstation, cấu hình máy Kali Linux và Metasploitable 2 dùng chung card mạng nội bộ để nhận dải IP `192.168.83.0/24`.
2. **Khởi tạo máy ảo mục tiêu:**
   * Nhập (Import) tệp cấu hình máy ảo `Metasploitable.vmx` vào VMware Workstation.
   * Khi VMware hiển thị thông báo hộp thoại *"This virtual machine might have been moved or copied"*, chọn **`I Copied It`** để hệ thống tự động sinh MAC address mới và tránh xung đột mạng.
3. **Xác định IP và kiểm tra kết nối:**
   * Trên Kali Linux kiểm tra IP bằng `ip -br addr` (IP: `192.168.83.131`).
   * Trên Metasploitable 2 đăng nhập `msfadmin`/`msfadmin` và kiểm tra IP bằng lệnh `ifconfig` (IP: `192.168.83.132`).
   * Kiểm tra thông mạng bằng lệnh `ping -c 4 192.168.83.132` từ Kali (kết quả đạt `0% packet loss`).

---

## 4. CÁC TÌNH HUỐNG ĐÃ THỰC HIỆN VÀ KẾT QUẢ

| STT | Tình huống / Kịch bản thực hiện | Câu lệnh thực tế trên Kali | Kết quả ghi nhận | Đánh giá |
| :---: | :--- | :--- | :--- | :---: |
| 1 | **Host Discovery** (Dò tìm thiết bị hoạt động) | `sudo nmap -sn 192.168.83.0/24` | Phát hiện 4 host đang sống (`.1`, `.131`, `.132`, `.254`) qua ARP Ping. | **PASS** |
| 2 | **TCP SYN Scan** (Quét cổng bán mở) | `sudo nmap -sS 192.168.83.132` | Phát hiện 23 cổng mở (open), 977 cổng đóng (closed với cờ RST), 0 cổng filtered trong 4.81 giây. | **PASS** |
| 3 | **Quét cờ dị thường** (FIN, Xmas, NULL) | `sudo nmap -sF/sX/sN 192.168.83.132` | Các cổng mở trả về `open\|filtered` đúng theo quy tắc RFC 793 trên nền tảng Linux. | **PASS** |
| 4 | **ACK Scan** (Kiểm tra bộ lọc Firewall) | `sudo nmap -sA 192.168.83.132` | Trả về `unfiltered` cho toàn bộ các cổng, chứng minh không có Firewall chặn gói ACK. | **PASS** |
| 5 | **UDP Scan** (Quét UDP có kiểm soát) | `sudo nmap -sU --top-ports 20 192.168.83.132` | Hoàn thành quét 20 cổng UDP thông dụng (DNS, NTP, SNMP...). | **PASS** |
| 6 | **Dò phiên bản & OS** (Service/OS Detection) | `sudo nmap -sV -O 192.168.83.132` | Nhận diện chính xác phiên bản vsftpd 2.3.4, OpenSSH 4.7p1, Apache 2.2.8 và Linux kernel 2.6.x. | **PASS** |
| 7 | **Kiểm tra NSE Script** (MS17-010 & SMB) | `sudo nmap -p 445 --script smb-vuln-ms17-010 192.168.83.132` | Cổng 445 mở nhưng script không kích hoạt cảnh báo do hệ thống chạy Samba trên Linux, không bị ảnh hưởng bởi lỗi của Windows. | **PASS** |
| 8 | **Xuất dữ liệu & Tạo báo cáo HTML** | `nmap ... -oN ketqua.txt -oX ketqua.xml`<br>`xsltproc ketqua.xml -o baocao.html` | Chuyển đổi thành công tệp XML thành giao diện web `baocao.html` và hiển thị trên Firefox. | **PASS** |
| 9 | **Thử nghiệm Hardening** (Trước/Sau phòng thủ) | `sudo /etc/init.d/apache2 stop` trên đích & quét lại `-sV -p 80` | Cổng 80 chuyển từ `open` sang `closed`, chứng minh việc thu hẹp bề mặt tấn công thành công. | **PASS** |

---

