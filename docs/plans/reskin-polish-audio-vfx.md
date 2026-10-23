# Plan reskin, polish, audio và VFX — Color Match Arena

Trạng thái: đã triển khai bản Prism trong source và `dist/prism/`; kết quả kiểm tra
và các bước cần xác nhận trên thiết bị thật nằm tại `../PRISM_VALIDATION.md`.
Ngày: 2026-10-23. Nền tảng: Java Swing / Java2D, TCP, MySQL; JDK 24 của workspace.

## 1. Hướng hình ảnh: Prism Arena

Giữ tên game hiện tại trong sản phẩm; “Prism Arena” là tên hướng mỹ thuật để duyệt concept.
Tinh thể đỏ san hô đối đầu tinh thể xanh băng trên bàn chiến thuật tối. Không đổi luật bàn 9×9, nối từ 3 quân, tia chiếm lãnh, 15 giây/lượt, 50 lượt và server quyết định kết quả.

Concept: `../concepts/prism-arena-v1.png`. Đây là minh họa hình ảnh, không phải screenshot hoặc fixture bàn hợp lệ; số điểm, nước cờ và tia trong ảnh không dùng làm đặc tả luật. Bản thật phải hiển thị tiếng Việt đầy đủ dấu. Các ô nhỏ dưới thẻ người chơi trong concept được bỏ vì game chưa có cơ chế tương ứng.

Palette đề xuất:

| Vai trò | Màu |
|---|---|
| Nền | #0B1220 |
| Panel | #131F33 |
| Panel nổi / hover | #1C2D46 |
| Chữ chính / phụ | #F1F5F9 / #AABBD0 |
| Đỏ | #FF6378 |
| Xanh | #57C8FF |
| Nhấn chiến thắng | #E9C46A |

Typography: Segoe UI có dấu tiếng Việt, số đồng hồ dùng font số rõ; không cần tải font ở runtime. Khoảng cách theo thang 4/8/12/16/24/32 px, viền và bán kính dùng token chung. Quân khác cả màu lẫn hình dạng; vùng đã chiếm dùng tint + pattern, không nhầm với quân còn trên bàn.

## 2. Hiện trạng đã kiểm tra

- `client/UITheme.java`: palette sáng, custom panel/button/icon, có sẵn timer hover.
- `client/GameFrame.java`: bàn 9×9 đặt kích thước 540 px, player card, chat, timer và spectator. BoardCell có pop-in, mỗi cell tự tạo timer.
- `client/AssetLoader.java`: cache ảnh từ thư mục ngoài `assets/`, có fallback Java2D; chưa đọc classpath trong JAR.
- `client/SoundManager.java`: tone sinh bằng code, mở audio line/thread theo lần phát, có bật/tắt chung; chưa có music bus/volume độc lập.
- Âm đặt quân hiện phát ngay lúc click, trước khi server xác nhận.
- `server/GameRoom.java`: broadcast BoardState mỗi giây cho timer. Snapshot không có move ID, tọa độ nước vừa đi hoặc metadata tia.
- `server/GameRules.java`: khi tạo chuỗi, quân chuyển thành vùng; không thể luôn suy ra chính xác tâm/hướng tia chỉ từ hai snapshot.
- `dist/ColorMatch-Client.jar` là artifact có sẵn; sửa source không tự cập nhật JAR này.

## 3. Phạm vi màn hình

| Màn | Reskin / polish |
|---|---|
| Đăng nhập / đăng ký | Nền tinh thể nhẹ, form rõ, Host/Cổng dễ thấy cho LAN; trạng thái đang kết nối, lỗi kết nối, tránh bấm gửi nhiều lần |
| Lobby | Danh sách online rõ trạng thái rảnh/đang chơi, thách đấu/xem trận nổi bật; empty/loading/error state |
| Màn chơi | Bàn chiếm ưu tiên, thẻ hai người chơi, chỉ báo lượt rõ, đồng hồ cảnh báo, chat và hướng dẫn ngắn |
| Thách đấu / xác nhận | Dialog cùng theme; focus và Enter/Escape rõ; đầu hàng/rời trận có xác nhận |
| Kết quả / tái đấu | Thắng/thua/hòa khác nhau; dùng điểm thực từ server, trạng thái chờ đối thủ tái đấu rõ |
| Xếp hạng / lịch sử | Bảng dễ đọc, đồng bộ theme, trạng thái chưa có dữ liệu |
| Cài đặt | Master/Music/SFX, mute, giảm chuyển động; lưu bằng Java Preferences |

Ưu tiên desktop 1366×768 và 1920×1080; kiểm tra Windows scale 100/125/150%. Bàn giữ vuông, font không cắt dấu, thu gọn panel phụ nếu thiếu chỗ. Mất kết nối phải có thông báo và khóa hành động phù hợp.

## 4. Asset và audio

Nguồn đã kiểm tra trên trang tác giả:

- UI SFX: https://kenney.nl/assets/interface-sounds — CC0.
- Jingle thắng/thua: https://kenney.nl/assets/music-jingles — CC0; phải nghe chọn clip hợp chủ đề trước khi chốt.
- UI tham khảo / icon phụ nếu phù hợp: https://kenney.nl/assets/ui-pack — CC0. Không trộn nguyên bộ panel khác style vào theme.

Âm nền ambient nhẹ: chọn nguồn miễn phí có giấy phép rõ ở giai đoạn asset; hiện chưa chốt track. Nếu chưa có track phù hợp, bản đầu chỉ ship SFX/jingle, không lấy bản quyền không rõ. Imagegen chỉ tạo ảnh, không tạo âm thanh.

Tự vẽ Java2D: tile, pattern lãnh thổ, viền, button, icon đơn giản, ring, tia, particle. Generate ảnh khi cần nét riêng: nền đăng nhập/sảnh, hai sprite quân tinh thể đồng bộ, emblem kết quả. PNG trong suốt cho quân; nền không chứa chữ/UI. Không dùng ảnh concept làm toàn bộ giao diện.

Danh sách đích: `assets/images/`, `assets/audio/`, `assets/licenses/`; giữ file nguồn, URL, tác giả và license trong `assets/ASSET_CREDITS.md`. Concept ở docs, không load khi chơi. Giữ tương thích đường dẫn piece hiện tại hoặc migrate call site rõ ràng.

Audio: WAV PCM cho Java Sound, preload ở background; pool Clip hữu hạn, giới hạn tiếng đồng thời, stop/close khi tắt ứng dụng. Music loop riêng có fade, duck khi thông báo quan trọng. Không đọc file hoặc decode trên Swing EDT. Không mở thread không giới hạn mỗi click. Nếu thiết bị không có audio hoặc thiếu file, game tiếp tục hoạt động.

## 5. Feedback theo sự kiện

| Sự kiện | Hình ảnh | Âm thanh | Thời lượng mục tiêu |
|---|---|---|---|
| Hover / focus ô hợp lệ | Viền + ghost quân nhẹ | Không | 80–120 ms |
| Gửi nước đi | Chặn gửi lặp khi chờ xác nhận | Click UI nhẹ nếu cần | Không làm chậm request |
| Server chấp nhận nước đi | Pop quân + vòng lan nhỏ | Đặt tinh thể | 120–180 ms |
| Chuỗi được kích hoạt | Nháy các ô nguồn, tia mảnh | Chime + sweep | 250–450 ms |
| Vùng đổi chủ | Tint chuyển màu, pattern mới | Gộp trong tiếng capture | 180–280 ms |
| Chuyển lượt | Viền player card + nhãn lượt | Cue nhẹ | 150–220 ms |
| Còn 5 giây | Ring/nhãn cảnh báo | Tick giới hạn một lần/giây | Theo timer server |
| Thắng / thua / hòa | Result card, particle thắng nhỏ | Jingle tương ứng | 600–1000 ms |
| Chat / thách đấu | Badge hoặc toast | Cue ngắn, chống spam | 100–200 ms |

Chế độ giảm chuyển động bỏ particle, pulse và overshoot, giữ highlight tĩnh và thông báo lượt. Hiệu ứng không che tọa độ, không chiếm focus hoặc chặn thao tác. Không camera shake mặc định cho bàn chiến thuật.

## 6. Kiến trúc triển khai

- Giữ MVP hiện tại, theme tokens trong UITheme; tách painter/effect/audio/settings thành lớp nhỏ trong client, không đưa âm thanh vào server.
- Board model hiển thị lưu previous/current snapshot độc lập; không animate lại khi chỉ đổi secondsRemaining/spectator count. Snapshot đầu vào và tái đấu dựng trạng thái tĩnh/reset baseline.
- Bản client-only đầu: animate quân/vùng thay đổi từ diff, chưa dựng tia giả nếu không đủ dữ kiện.
- Để có tia chính xác: thêm DTO sự kiện server xác nhận, gồm match ID, move sequence, người đánh, ô đặt, các chuỗi/tia và ô đổi chủ lấy từ chính quá trình áp luật. Giữ API boolean hiện có nếu có consumer khác; bổ sung result model nội bộ thay vì chép lại luật trong client.
- State cuối vẫn là nguồn sự thật; VFX chỉ đọc event. Chống phát trùng theo match ID + sequence; snapshot cũ không chạy lại hiệu ứng. Khán giả mới vào không phát lại toàn trận.
- Thay đổi DTO chung yêu cầu build/phát hành client và server cùng phiên bản; không mặc định JAR cũ tương thích.
- Một animation clock cho board/effect layer, dùng elapsed time; repaint vùng cần thiết. Không khởi tạo 81 timer, không load asset trong paintComponent. Stop timer khi dispose và reset khi tái đấu.
- Network xử lý ngoài EDT; cập nhật Swing trên EDT; không sleep trên EDT. Hủy connect/task theo vòng đời cửa sổ.
- AssetLoader đọc được classpath và thư mục override, preload/cache scaled images có giới hạn. JAR chứa asset để laptop thứ hai không phụ thuộc thư mục làm việc.

## 7. Các giai đoạn và cổng hoàn thành

| Giai đoạn | Công việc / file chính | Điều kiện hoàn thành | Ước lượng |
|---|---|---|---|
| 0 — Chốt hướng | Concept, palette, shot list, inventory asset; chụp baseline UI hiện tại | Chốt style và phân biệt quân/vùng; danh sách asset có nguồn | 0.5 ngày |
| 1 — Nền giao diện | UITheme, AssetLoader, token/layout dùng chung, GameFrame + LoginFrame vertical slice | Màn chơi tĩnh và login theo style ở 1366×768; giữ đủ chức năng | 1–1.5 ngày |
| 2 — Reskin toàn luồng | Lobby, Ranking, History, dialogs, settings, connecting/error/empty state | Hai tài khoản hoàn thành login → thách đấu → trận → tái đấu → lobby | 1–1.5 ngày |
| 3 — Audio + VFX | SoundManager, board effect layer, diff; metadata từ GameRules/GameRoom/common nếu cần tia chính xác | Cue đúng một lần cho nước hợp lệ; không lặp theo tick; mute/reduced motion hoạt động | 1.5–2 ngày |
| 4 — Kiểm tra / đóng gói | Build source + JAR mới, asset/license, launcher và hướng dẫn LAN | Chạy từ thư mục bất kỳ, hai laptop cùng server, không cần MySQL ở client | 0.5–1 ngày |

Tổng dự kiến: 5–7.5 ngày làm việc, cần điều chỉnh sau vertical slice và kiểm tra trạng thái runtime. Ưu tiên style tĩnh và luồng chơi trước, tia/particle cầu kỳ sau.

## 8. Kiểm tra và bàn giao

1. Compile sạch UTF-8 bằng JDK 24 vào build output riêng; không dựa vào .class có sẵn. Không ghi đè artifact phân phối cũ khi chưa xác nhận bản mới.
2. Kiểm tra fixture meaningful: snapshot chỉ đổi timer không tạo sound/VFX; đặt trên vùng của mình; nước bị từ chối; chuỗi ngang/dọc/hai đường chéo; giao nhiều chuỗi; tia bị quân đối thủ chặn; diff vùng đổi chủ.
3. Hai client cùng server: mỗi bên thấy đúng màu/lượt/điểm, không double submit; thêm spectator, spectator rời/vào, rematch, surrender, disconnect giữa animation. Kiểm tra cue không nhân đôi khi thêm người xem.
4. Hai laptop LAN dùng JAR mới: kết nối bằng Host/Cổng, đăng ký/đăng nhập, chơi, chat, tái đấu. Client không cần database hoặc thư mục source.
5. UI: font dấu tiếng Việt, Tab/Enter/Escape, focus, scale 100/125/150%, phân biệt trạng thái ngoài màu, reduced motion. Asset thiếu có fallback; không audio device vẫn mở game.
6. Mục tiêu performance: phản hồi UI <100 ms trên máy thử; effect layer frame budget khoảng 16.7 ms khi đang animate. Đo thực tế và điều chỉnh particle; sau nhiều lần vào/ra trận không tăng timer/thread/Clip còn sống. Không suy diễn đạt FPS từ compile.
7. Bàn giao: screenshot trước/sau, manifest asset/giấy phép, bản client/server cùng phiên bản, hướng dẫn LAN và danh sách lỗi còn lại. Chỉ đánh dấu hoàn thành sau kiểm tra thực tế.

## 9. Concept đã tạo

Built-in image_gen, preview UI landscape. Brief/prompt cốt lõi: “Prism Arena desktop Java Swing board strategy game, exact 9×9 front-facing board, midnight navy panels, coral round crystal versus ice-blue diamond tokens, subtle claimed-area patterns, restrained capture beam, player cards left, chat/rules right, readable turn/timer, achievable Java2D interface.”

Ảnh chỉ dùng duyệt art direction; asset production được tạo riêng theo danh sách ở mục 4.

Quyết định triển khai: quân, tile, emblem và nền được vẽ Java2D để đồng bộ hình học
và giữ nét khi đổi kích thước; không cần generate thêm bitmap production. Bản thật
tiết chế khung trang trí so với concept. Audio dùng Kenney CC0 và harmonic bed tự tổng hợp.
