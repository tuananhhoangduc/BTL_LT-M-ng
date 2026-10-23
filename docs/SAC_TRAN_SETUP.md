# Sắc Trận 2.0

Bản mới dùng PNG production và BGM do người dùng chọn. JDK 24, `JAVA_HOME` trỏ tới
thư mục JDK. Không cần Internet, Python hoặc thư viện audio/image ngoài khi chơi.

## Máy chạy server

Schema MySQL, tài khoản và dữ liệu được giữ nguyên. Trong PowerShell tại repo:

```powershell
cd D:\Coding\BTL_LTM
$credential = Get-Credential -UserName root -Message 'Mat khau MySQL'
$env:COLOR_MATCH_DB_PASSWORD = $credential.GetNetworkCredential().Password
.\Chay_Server.bat
```

Mở terminal khác, chạy `.\Chay_Client.bat`. Hai launcher tự build nếu source/asset
thay đổi, dùng class output mới thay vì .class cũ trong source. Để dùng nhạc/ảnh mới,
đóng client cũ rồi mở lại; không có hot reload giữa một trận đang chơi.

## Laptop thứ hai

Giải nén `dist/SacTran-Client.zip`, mở `sac-tran/Chay_Game.bat`. Nhập IPv4 Wi-Fi của
máy server, cổng 5000; máy client không cần MySQL hoặc source. PNG và WAV ở trong JAR.
JAR cũ ở dist/ và dist/prism/ được giữ để đối chiếu, không phải gói phân phối mới.

## Asset và âm thanh

- PNG riêng: `assets/images/pieces`, `tiles`, `backgrounds`, `emblems`.
- Catalog: `assets/images/catalog.properties`. Prompt/ref: `assets/images/art-prompts.json`.
- BGM: `assets/audio/bgm/puzzle_bright.wav`; nguồn MP3 được chọn và checksum ở track.json.
- Import lại nhạc: `python scripts/import_bgm.py 'DUONG_DAN_MP3'` (cần soundfile/numpy ở .work/python-deps).
- Script SFX không tự sinh lại drone cũ. Drone cũ không được dùng hoặc đóng vào JAR mới.
- Settings giữ mức âm lượng người chơi đã lưu, có mute, reduced motion, hướng dẫn 3 bước và Credits.

## Kiểm tra

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Test.ps1 -Screenshots
```

Ảnh mới tại `.work/review/`; bản bàn giao tại `docs/screenshots/sac-tran/`.
Kết quả và giới hạn xác minh: `docs/SAC_TRAN_VALIDATION.md`.
