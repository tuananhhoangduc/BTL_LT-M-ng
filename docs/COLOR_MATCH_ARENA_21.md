# Color Match Arena 2.1 — tile readability

Tên sản phẩm giữ tiếng Anh **Color Match Arena**. Ngôn ngữ hướng dẫn/nút vẫn là tiếng Việt.
Các tài liệu và gói Sắc Trận/Prism là lịch sử của phiên bản trước, không phải tên mới.

## Đã thay đổi

- Tạo lại ba PNG bằng built-in image_gen và tích hợp qua catalog: graphite neutral,
  coral đỏ/sọc chéo, cobalt xanh/hình thoi. Giữ file cũ để đối chiếu.
- PNG mới ở `assets/images/tiles/*-v2.png`; exact prompt/ref ở `assets/images/tile-v2-prompts.json`.
- Bỏ lớp phủ tối alpha 100/255 làm mờ màu phe; viền/hover khớp màu người chơi.
- Pending có ghost và viền đứt ở ô đã gửi, thông báo header; tick không giải phóng
  request, kết thúc/mất kết nối/error sẽ xóa marker. Không đặt quân chính thức trước server.
- Pop-in 180 ms dùng ease-out để bớt cứng. Luật, điểm, protocol và BGM được giữ nguyên.
- AppBrand, title launcher/server và gói phân phối dùng tên tiếng Anh.

## Kiểm tra đã chạy

`powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Test.ps1 -Screenshots`

- Compile, RulesChecks, RoomChecks, NetworkChecks: PASS.
- PNG decode, alpha, catalog 10 entries: PASS.
- TileReadabilityChecks: PASS. RGB trung bình mặt tile: neutral (34.8,42.5,51.9),
  red (181.4,54.4,63.1), blue (14.9,86.5,164.0). Đây là sanity check dự án,
  không phải chứng nhận accessibility.
- Rà ảnh 32/48/64 px và bàn thật có hai vùng rộng, quân chồng trên lãnh thổ mình.
- Pending state, chống gửi lặp, cleanup marker, snapshot/timer dedupe: PASS.
- Chạy AssetChecks/PresentationChecks từ TEMP với classpath trỏ JAR mới: PASS.
- Mẫu offscreen khoảng 6.8–9.1 ms/lượt vẽ; không gọi đây là FPS trên thiết bị.
- Không dừng hoặc thay tiến trình server đang chạy để kiểm tra asset.

Chưa xác nhận: một trận LAN hai laptop thật, phản hồi cảm giác chơi của người dùng,
đọc pattern khi giảm saturation và Windows DPI thực trên thiết bị khác.

## Bản chạy mới

Đóng client cũ rồi mở `Chay_Client.bat` từ repo. Launcher tự build.
Gửi `dist/ColorMatchArena-Client.zip` cho máy thứ hai, giải nén rồi mở
`color-match-arena/Chay_Game.bat`. JDK 24; Host là IP Wi-Fi server, port 5000.

Plan game feel tiếp theo: `docs/plans/color-match-arena-game-feel-v3.md`.
Screenshot bản này: `docs/screenshots/color-match-arena/`.
