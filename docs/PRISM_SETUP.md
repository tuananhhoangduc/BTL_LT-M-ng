# Chạy bản Prism

JDK 24; `JAVA_HOME` trỏ tới JDK. Không cần Maven/Gradle hoặc Python khi chơi.
Source compile bằng UTF-8 vào thư mục build theo hash, không dùng .class cũ trong repo.

```powershell
cd D:\Coding\BTL_LTM
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\Build.ps1
```

Server vẫn dùng MySQL `color_mind_game`, tài khoản `root` và biến môi trường
`COLOR_MATCH_DB_PASSWORD`. Schema và dữ liệu hiện tại được giữ nguyên.

```powershell
$credential = Get-Credential -UserName root -Message 'Mat khau MySQL'
$env:COLOR_MATCH_DB_PASSWORD = $credential.GetNetworkCredential().Password
.\Chay_Server.bat
```

Mở PowerShell khác và chạy `.\Chay_Client.bat`. Launcher tự build nếu source/asset
thay đổi. Muốn dùng phiên bản mới, đóng server/client cũ rồi mở lại bằng launcher.
Console server dùng UTF-8; client giữ tiếng Việt đầy đủ dấu.

Gửi toàn bộ `dist/prism/` cho laptop thứ hai; mở `Chay_Game.bat` trong đó.
Asset và audio nằm trong JAR nên có thể chạy từ một thư mục làm việc khác.
Máy client không cần MySQL. Hai máy cùng Wi-Fi: nhập IPv4 của server và cổng 5000.

Giao thức BoardState được tăng serialVersionUID lên 2 để có match ID, sequence,
nước vừa đi và tia do server ghi nhận. Bắt buộc cập nhật server và mọi client cùng
phiên bản. JAR cũ ở `dist/` gốc và .class cũ trong source không phải bản Prism.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\Test.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\Test.ps1 -Screenshots
```

Screenshot kiểm tra nằm ở `.work/review/`. Test room dùng session trong bộ nhớ,
test mạng dùng TCP loopback thật; không tạo tài khoản hoặc ghi vào MySQL của bạn.
Audio lấy từ Kenney CC0 và harmonic bed tự tổng hợp; nguồn/giấy phép ở
`assets/ASSET_CREDITS.md`. Build runtime không cần Internet.
