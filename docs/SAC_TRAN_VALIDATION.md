# Sắc Trận 2.0 — kiểm tra và bàn giao

Ngày: 2026-10-23. Build UTF-8, JDK 24 / --release 24.

## Nội dung đã triển khai

- Tên dùng chung AppBrand: Sắc Trận, giữ package/protocol/database cũ.
- Đúng MP3 người dùng cung cấp, chuyển PCM WAV 44.1 kHz stereo, 75.860 giây.
  SHA256 nguồn khớp track.json; peak PCM 0.89, sample đầu/cuối bằng zero nhờ fade 60 ms.
  Đây là phát lại toàn bài có fade ở mép, không tuyên bố đã cắt loop đúng phách.
- BGM không restart theo cửa sổ; scene giảm âm khi vào trận, fade/duck, volume/mute
  và mức âm lượng đã lưu được giữ. Master/SFX zero dừng clip đang phát.
- 10 PNG riêng tạo bằng built-in image_gen theo concept đã chốt; nguồn và exact
  prompt/ref trong assets/images/art-prompts.json. Không cắt concept thành UI giả.
- SpriteCatalog preload ngoài EDT, chuẩn hóa buffer trong RAM, cache cỡ hiển thị tối đa
  64 entries, xử lý device scale; ArenaArt chỉ còn facade PNG và fallback.
- Quân cùng hình ở login, game, player card, icon; tile có pattern, arena/background PNG;
  result có emblem. Chữ và layout vẫn là Swing, hiệu ứng vẫn theo state server.
- Thanh 15 giây, nhận diện màu của mình, source/beam feedback, nhãn số ô capture,
  trạng thái chờ thách đấu và tái đấu; mất kết nối khóa bàn và cho về đăng nhập.
- Hướng dẫn 3 bước dùng snapshot được kiểm tra lại bằng GameRules. Không thêm bot/luật mới.
- Gói mới dist/sac-tran/SacTran-Client.jar; bản Prism và ZIP cũ không bị ghi đè.

## Kết quả tự động

Lệnh: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Test.ps1 -Screenshots`.

| Kiểm tra | Kết quả |
|---|---|
| Compile source, test source, đóng gói | PASS |
| RulesChecks: 4 hướng, blocker, vùng dưới quân, thứ tự áp luật | PASS |
| Tutorial transition so với GameRules thật | PASS |
| RoomChecks: 2 người, capture metadata, timer, khán giả, từ chối sai lượt | PASS |
| NetworkChecks: ObjectStreams qua TCP loopback, score cuối, rematch | PASS |
| Peer đóng TCP thật → callback mất kết nối trên EDT, khóa board | PASS |
| AssetChecks: 10 PNG tồn tại, decode, kích thước và alpha sprite | PASS |
| Feedback dedupe theo snapshot/timer/match; pending input và khóa board | PASS |
| 10 WAV decode gồm bài MP3 được chọn đã convert | PASS |
| Chạy AssetChecks/PresentationChecks từ TEMP với classpath chỉ có tests và JAR mới | PASS |
| Render login/lobby/game/game nhỏ/ranking/history/tutorial/settings/result | PASS |
| JRE forced uiScale 1.25 / 1.5: render hoàn tất và rà ảnh mẫu | PASS (không thay Windows DPI thật) |
| Jar có PNG/catalog/BGM mới, không có audio/ambient.wav | PASS |
| git diff --check | PASS; có thông báo LF/CRLF từ cấu hình Git Windows |

Mẫu đo offscreen 120 lượt vẽ bàn 560×560 có PNG: khoảng 6.5–9.3 ms/lượt ở môi trường
này. Không gọi đây là FPS, không dùng nó thay cho đo laptop thật hoặc stress lâu dài.

## Chưa xác nhận trên thiết bị thật

- Một trận đầy đủ qua MySQL và hai laptop LAN; không có credential MySQL của người
  dùng trong môi trường kiểm tra, test không ghi vào database hiện tại.
- Cân bằng nghe trên loa/tai nghe của người dùng. Bài nhạc đã đúng lựa chọn, format và
  đầu/cuối đã kiểm tra; chưa tuyên bố đạt đánh giá nghe chủ quan sau mix.
- Windows DPI thực, driver audio khác, mất Wi-Fi thật, nhiều ván liên tục.

Ảnh trong docs/screenshots/sac-tran là render component Swing thật với dữ liệu minh họa,
không phải ảnh AI mockup. Contact sheet chỉ để kiểm tra asset, không phải atlas runtime.
