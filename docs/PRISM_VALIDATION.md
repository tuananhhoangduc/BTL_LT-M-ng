# Prism — kiểm tra bàn giao

Ngày: 2026-10-23. Build bằng JDK 24, UTF-8, `--release 24`.

## Đã thực hiện

- Compile source và đóng gói client có `common`, `client`, `assets` vào JAR mới.
- `server.RulesChecks`: chuỗi ngang/dọc/hai đường chéo, tia dừng trước quân đối thủ,
  nước không hợp lệ, đặt trên vùng của mình và giữ nguyên thứ tự áp luật hiện tại.
- `server.RoomChecks`: GameRoom thật, hai session, năm nước đi tạo capture, metadata
  nước đi/tia, timer giữ move sequence, khán giả vào muộn, từ chối sai lượt, snapshot
  lưu đúng vùng nằm dưới quân. Không ghi vào database.
- `client.NetworkChecks`: TCP loopback và Java ObjectStreams thật; serialize tia,
  delivery trên Swing EDT, điểm cuối cập nhật trước result dialog, reply tái đấu và
  trạng thái chờ đối thủ. Không xác nhận tầng đăng nhập/MySQL bằng test này.
- `client.PresentationChecks`: snapshot đầu không replay, duplicate/tick không lặp
  feedback, countdown đúng lượt, timer dừng, chỉ một yêu cầu đặt quân đang chờ,
  khóa bàn sau kết thúc, đọc/giải mã đủ 10 WAV PCM.
- Chạy PresentationChecks với classpath trỏ vào JAR từ thư mục TEMP ngoài repo:
  thành công, chứng minh resource không phụ thuộc working directory.
- Render/kiểm tra hình ảnh thật của login, lobby, ranking, history, bàn chơi thường
  và bàn chơi 960×620. Ảnh lấy trực tiếp từ component Swing, không phải concept.
- Mẫu đo offscreen 120 lần vẽ bàn 560×560: khoảng 6–9 ms/lần trên môi trường này;
  đây không phải FPS hoặc benchmark thiết bị thật.
- `git diff --check`: không có lỗi whitespace; thông báo LF/CRLF là cấu hình Git Windows.

Lệnh tái hiện:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Test.ps1 -Screenshots
```

## Cần xác nhận thủ công

- Một trận hoàn chỉnh qua tài khoản/MySQL hiện tại trên hai laptop LAN.
- Loa/tai nghe: cảm nhận âm lượng, nhạc và mức cân bằng từng tiếng. File được decode
  và kiểm tra định dạng; chưa có đánh giá nghe trên thiết bị của người dùng.
- DPI Windows 125/150%, thu phóng/phông chữ hệ thống, máy có màn hình nhỏ.
- Stress nhiều ván liên tục và mất mạng thật; chưa tuyên bố đạt gate release.

## Chạy thử bản mới

Đóng server/client cũ rồi chạy lại launcher gốc. Nhập mật khẩu MySQL bằng hướng dẫn
trong PRISM_SETUP.md. Gửi `dist/ColorMatch-Prism.zip` cho máy thứ hai và giải nén,
mở `prism/Chay_Game.bat`; nhập IP máy server, cổng 5000.

Không dùng JAR cũ ở `dist/` gốc cùng server mới: BoardState serialVersionUID đã
tăng từ 1 lên 2. Không thay đổi schema, luật tính điểm hay credential MySQL hiện tại.
