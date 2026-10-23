# Plan hoàn thiện: Sắc Trận — BGM, asset PNG và polish

Trạng thái: đã triển khai Sắc Trận 2.0, bộ PNG và BGM người dùng chọn. Bàn giao ở dist/sac-tran/.
Kiểm tra tự động đã qua; bước nghe sau mix, Windows DPI thật và trận LAN hai laptop cần xác nhận trên thiết bị.
Báo cáo: ../SAC_TRAN_VALIDATION.md.
Ngày: 2026-10-23. Tiếp nối bản Prism hiện tại và phản hồi thực tế của người chơi.

## 1. Các quyết định đề xuất

1. Bỏ BGM harmonic tự tổng hợp. Dùng đúng MP3 **cyberwave-orchestra-puzzle-game-bright-casual-video-game-music-249202.mp3** do người dùng chọn.
2. Đổi tên hiển thị thành **Sắc Trận**. “Prism” chỉ giữ làm tên hướng mỹ thuật nội bộ.
3. Tạo bộ PNG production thực sự, bắt đầu từ cặp quân và nền. Dùng cùng asset ở login, bàn chơi, thẻ người chơi và kết quả.
4. Giữ UI gọn và đọc được; dành chi tiết hình ảnh cho quân và vật liệu bàn, không phủ mọi panel bằng glow/gradient.
5. Hoàn thiện trải nghiệm một trận trước khi thêm tính năng lớn hoặc mỹ thuật phụ.

## 2. Baseline trước triển khai (lưu để đối chiếu)

Hiện tại đã có 10 PNG riêng; inventory trong assets/images/catalog.properties và báo cáo bàn giao.

| Thành phần hiện tại | Nguồn / nơi dùng |
|---|---|
| BGM `assets/audio/ambient.wav` | Tự tổng hợp từ 4 sóng sin trong `scripts/prepare_audio.py`; vòng lặp 16 giây |
| Click / đặt quân / capture / chat / cảnh báo | Kenney Interface Sounds, chuyển OGG thành WAV |
| Jingle kết quả | Kenney Music Jingles / Pizzicato, chuyển thành WAV |
| Quân đỏ/xanh, nền trang trí | `client/ArenaArt.java`, vẽ Java2D |
| Tile, pattern lãnh thổ, tia và ring | `client/BoardPanel.java`, vẽ Java2D |
| Ảnh concept PNG | `docs/concepts/prism-arena-v1.png`; không dùng trong runtime |

Hiện **chưa có bộ PNG mỹ thuật được game sử dụng**. AssetLoader có khả năng đọc ảnh,
nhưng các call site hiển thị quân vẫn gọi ArenaArt.crystal. Đặt PNG vào assets sẽ
không tự đổi hình nếu chưa nối loader vào renderer. Đây là phần cần hoàn tất, không
phải asset ảnh đã được tải mà người chơi không tìm thấy.

Nguồn audio/giấy phép hiện có: `assets/ASSET_CREDITS.md`, `assets/licenses/`.

Về BGM: bản tổng hợp giữ âm liên tục, ít biến hóa; đây có thể góp phần tạo cảm giác
ù/căng. Phản hồi nghe của người chơi là lý do thay track. Kiểm tra decode WAV trước
đây chỉ chứng minh file chạy được, không chứng minh nhạc hay hoặc phù hợp.

## 3. BGM đã được người dùng chọn

File nguồn: D:\Downloads\cyberwave-orchestra-puzzle-game-bright-casual-video-game-music-249202.mp3.
Runtime: assets/audio/bgm/puzzle_bright.wav, 75.860 giây, stereo PCM 16-bit 44.1 kHz.
Metadata/hash: assets/audio/bgm/track.json. Importer: scripts/import_bgm.py.
Không dùng hai shortlist trước. Giữ bài đầy đủ; fade 60 ms hai mép chống discontinuity,
không tự cắt nhạc theo beat chưa nghe kiểm chứng. Drone cũ được loại khỏi runtime/JAR.
Các tiêu chí nghe dưới đây là checklist kiểm tra sau mix, không phải yêu cầu chọn lại bài.

### Tiêu chí nghe và mix

- Cảm giác puzzle sôi động, có nhịp rõ, không u tối; không chọn track chỉ vì gắn nhãn ambient.
- Nghe ít nhất hai vòng lặp và một trận 5–10 phút, bằng loa laptop và tai nghe.
- SFX đặt quân, capture và cảnh báo lượt nghe rõ khi BGM bật ở mức mặc định.
- Không đổi tốc độ hoặc thêm lớp drone khi sắp hết giờ. Cảnh báo dùng cue riêng.
- Volume mặc định khởi điểm: Music 10–15%, SFX 55–65%, sau đó chỉnh theo nghe thực tế.
  Đây là vị trí slider thử nghiệm, không phải mức loudness chuẩn giữa mọi thiết bị.
- Fade vào khoảng 1 giây, chuyển ngữ cảnh 0.5–1 giây; duck nhẹ khi thách đấu/kết quả.
- Không restart bài khi chuyển login → lobby → match hoặc mở Settings.
- So loudness giữa BGM và SFX; không tiếp tục peak-normalize mọi clip về cùng 0.65
  rồi coi là đã cân bằng âm lượng cảm nhận.
- Giữ mute/music-off độc lập và lưu lựa chọn cũ. Trong lúc thay nhạc, bỏ hẳn track
  tổng hợp khỏi lựa chọn mặc định, không fallback về tiếng cũ nếu thiếu file mới.

### File cần sửa ở giai đoạn audio

- `scripts/prepare_audio.py`: bỏ phần tự sinh ambient; không ghi đè track được chọn
  mỗi lần tái tạo SFX. Ghi lại quy trình convert của track đã chọn.
- `assets/audio/bgm/puzzle_bright.wav`: bản runtime, giữ source/license riêng.
- `client/SoundManager.java`: tên track rõ, gain/fade/duck theo bus; không thay logic gameplay.
- `client/ClientSettings.java`, `SettingsDialog.java`: hiển thị mức âm lượng rõ; giữ preference đã có.
- `assets/ASSET_CREDITS.md`, `assets/licenses/`: bổ sung tác giả và giấy phép mới.
- Chuyển MP3 người dùng cung cấp sang WAV PCM 16-bit để dùng Java Sound hiện tại; đo bộ nhớ khi nạp track mới. Nếu dùng
  quá nhiều RAM mới tách music streaming, không viết lại toàn bộ audio engine trước khi đo.

## 4. Đổi tên game

**Tên ưu tiên: SẮC TRẬN**

“Sắc” gắn với hai màu; “Trận” gắn với đấu trí và chiếm ô. Ngắn hơn tên cũ, dễ đọc
trong title bar, logo và màn kết quả. Phụ đề khi cần giải thích: “Nối quân, chiếm lãnh thổ”.

Hai phương án khác: **Lăng Kính** (thiên về hình ảnh tinh thể), **Giao Sắc** (mềm hơn,
thiên về puzzle). “Sắc Trận” là tên làm việc đề xuất; chưa tuyên bố là tên độc quyền.

Khi triển khai: gom tên/phiên bản vào một AppBrand dùng chung; đổi title window,
login, header lobby/game, dialog, launcher, README và tên gói phân phối. Không đổi
database `color_mind_game`, package Java hoặc message enum chỉ để đổi thương hiệu.
Logo chữ dựng bằng typography có dấu; không dùng chữ AI sinh trong PNG.

## 5. Bộ PNG production

Đã tạo và tích hợp đủ 10 file. Kích thước nguồn thực: sprite/tile/emblem 1254×1254,
login 1024×1536, arena 1672×941; buffer sprite runtime chuẩn hóa tối đa 256 px và cache theo DPI.
Bảng dưới là kích thước dự kiến ban đầu, không phải kích thước file đã xuất.

Concept mới: `../concepts/sac-tran-art-sheet-v2.png`; prompt gốc và hướng chỉnh tại
`../concepts/sac-tran-art-v2-prompt.txt`. Built-in image_gen tạo preview art sheet:
tinh thể đỏ tròn đa giác/xanh hình thoi, tile gốm có pattern, emblem và arena.
Đây là bản duyệt style, không cắt ảnh này thành sprite atlas production và không
lấy bố trí quân/vùng trong hình làm fixture luật. Các file riêng vẫn cần alpha thật.


Hướng thống nhất: tinh thể có khối vừa phải, ánh sáng từ trên-trái, bề mặt sạch,
đỏ san hô và xanh băng trên nền xanh đêm. Một hệ vật liệu, một góc nhìn trực diện.
Đỏ vẫn khác xanh cả hình dạng lẫn màu; nhận ra quân và vùng ở kích thước nhỏ.

| File đích | Kích thước nguồn | Đặc tả |
|---|---|---|
| `assets/images/pieces/red.png` | 512×512 | Tinh thể đỏ dạng tròn đa giác, alpha thật, không nền/bệ |
| `assets/images/pieces/blue.png` | 512×512 | Tinh thể xanh hình thoi, cùng ánh sáng, độ bóng, tỷ lệ với đỏ |
| `assets/images/tiles/neutral.png` | 256×256 | Ô nền trung tính, góc và padding đồng nhất |
| `assets/images/tiles/red_area.png` | 256×256 | Cùng tile, tint đỏ nhẹ và pattern riêng |
| `assets/images/tiles/blue_area.png` | 256×256 | Cùng tile, tint xanh nhẹ và pattern riêng |
| `assets/images/backgrounds/login.png` | 1536×1024 | Tinh thể ở rìa, vùng giữa yên, không chữ hoặc form |
| `assets/images/backgrounds/arena.png` | 1920×1080 | Khung cảnh nền độ tương phản thấp; phần sau bàn không rối |
| `assets/images/emblems/victory.png` | 256×256 | Biểu tượng nhỏ có alpha, cùng vật liệu; không cúp vàng stock lệch style |
| `assets/images/emblems/draw.png` | 256×256 | Hai màu cân bằng, cùng mức chi tiết |
| `assets/images/emblems/defeat.png` | 256×256 | Giảm độ sáng, bình tĩnh, không hình ảnh trừng phạt |

### Quy trình generate để tránh asset AI rời rạc

1. Tạo master art của một quân, duyệt ở kích thước thật 32/48/64 px, không chỉ xem bản 512 px.
2. Tạo quân còn lại bằng tham chiếu master, khóa ánh sáng, vật liệu, padding và chất lượng cạnh.
3. Chốt cặp quân trước; dùng cùng tham chiếu cho tile và emblem. Mỗi output là file riêng.
4. Kiểm tra alpha trên nền trắng/đen/xanh đêm; loại halo, bóng nền giả, cạnh răng cưa.
5. Dựng contact sheet và một screenshot bàn thật để so cùng nhau. Contact sheet chỉ dùng duyệt.
6. Chỉ sau đó tạo hai background. Không generate nhiều bộ độc lập rồi trộn lại.
7. Lưu prompt/reference, nguồn gốc và phiên bản trong `assets/ASSET_CREDITS.md` và
   manifest asset. Có file production trong workspace và trong JAR, không chỉ trong thư mục ảnh mặc định.

Brief cho lần generate đầu: “Front-facing coral faceted crystal game token,
rounded octagonal silhouette, restrained satin-glass material, one soft top-left
key light, readable silhouette at 32 pixels, centered with consistent padding,
true transparent background, no text, no pedestal, no frame, no cast background,
no particles, no watermark.” Bản xanh tham chiếu bản đỏ, đổi silhouette thành thoi
và đổi màu; các thuộc tính còn lại giữ nguyên.

### Tích hợp PNG vào game

- Tạo SpriteCatalog ánh xạ semantic ID → file trong manifest, preload ngoài EDT.
- Nối sprite vào `BoardPanel`, `GameFrame`, `LoginFrame`, `UITheme` và result dialog;
  cùng một quân ở mọi nơi, không để một màn dùng PNG còn màn khác dùng hình cũ.
- Cache BufferedImage và các cỡ hiển thị theo DPI, giới hạn cache; không ImageIO/resize
  ảnh lớn trong paint loop. Thiếu ảnh có fallback đơn giản và log một lần.
- Background giữ tỷ lệ và crop có chủ đích; không kéo dãn hình. Bảo đảm tương phản chữ
  bằng lớp nền tối nhẹ ở UI, không bake toàn bộ UI vào ảnh.
- Chữ, layout, trạng thái disabled/focus, đồng hồ và tia động tiếp tục là component/code.
  PNG thay phần mỹ thuật; không biến toàn bộ màn hình thành một screenshot lớn.
- Giữ nét 9×9, pattern phân biệt vùng, ring nước cuối và hover hợp lệ. VFX vẫn lấy
  dữ liệu server hiện có; lượt này không sửa luật để phục vụ hình ảnh.

## 6. Polish ưu tiên theo trải nghiệm chơi

### P0 — Sửa điều đang gây khó chịu

- Thay BGM qua nghe thử; rà lại tick/capture/jingle, không làm âm báo dồn dập.
- Cặp quân PNG nhất quán, lớn vừa đủ ở 960×620; không che pattern lãnh thổ.
- Một tên game, một bộ palette, một kiểu khoảng cách trên toàn luồng.

### P1 — Làm một trận đấu dễ hiểu và có phản hồi rõ

- Chỉ báo “Bạn: Đỏ/Xanh” và “Lượt của bạn/Đối thủ”, không dựa hoàn toàn vào màu.
- Hover ghost, nước cuối và các ô có thể đặt rõ; không thêm trợ lý chiến thuật hoặc nước gợi ý.
- Khi capture: highlight chuỗi nguồn → tia → vùng đổi chủ; nhãn thay đổi số ô ngắn,
  điểm cuối luôn lấy từ server. Không delay model/network để chờ hiệu ứng.
- Transition 120–250 ms cho thao tác nhỏ; capture khoảng 350–500 ms, không phủ bàn
  bằng particle. Reduced motion vẫn có highlight tĩnh đầy đủ thông tin.
- Thanh đồng hồ biểu diễn 15 giây; cảnh báo mức thấp tối đa một cue mỗi giây theo
  state server, không tự trừ lượt ở client.
- Kết quả đúng màu, đúng điểm cuối, rõ ai đồng ý tái đấu và đang chờ ai.
- Lobby có trạng thái đang gửi/chờ thách đấu; chưa có đối thủ thì hướng dẫn mời
  laptop khác vào đúng IP/cổng, không thêm màn trống trang trí.

### P2 — Hoàn thiện và bền hơn

- Hướng dẫn 3 bước bằng snapshot hợp lệ: đặt quân → nối ba → tia bị chặn. Dùng ví dụ
  được GameRules xác nhận; không chép một bản luật khác vào UI. Chế độ tập/bot để sau.
- Mất kết nối: thông báo rõ, khóa nước đi, nút về đăng nhập; không tự hứa reconnect
  và phục hồi trận nếu server chưa có giao thức tương ứng.
- Đo layout ở 1366×768 và Windows DPI 100/125/150%; kiểm tra tên dài, tiếng Việt,
  Tab/Enter/Escape, focus và điểm nhìn của khán giả.
- Credits trong Settings/About: nhạc, SFX và nguồn PNG. Build/JAR/ZIP cùng phiên bản.
- Một trận LAN hoàn chỉnh, tái đấu và mất mạng thật; chạy nhiều lần vào/ra trận để
  kiểm tra timer, thread, Clip và image cache không tăng mãi.

Không đưa shop, currency, skin unlock, battle pass hoặc leaderboard mới vào đợt polish này.

## 7. Trình tự thực hiện và cổng chấp nhận

| Mốc | Sản phẩm cụ thể | Chấp nhận khi |
|---|---|---|
| A — Audio trước | Preview track mới + mix thử trong game | Người chơi nghe hai vòng/trận thử, không còn cảm giác ghê/ù, SFX rõ |
| B — Art nhỏ nhưng đúng | Cặp quân PNG + 3 tile + contact sheet | Đọc tốt ở 32–64 px, alpha sạch, cùng vật liệu và ánh sáng |
| C — Một màn chơi hoàn chỉnh | Bàn thật dùng PNG, nền arena, capture/turn feedback | Hình ảnh bám bộ asset đã chốt, điểm/lượt đúng, không che hành động |
| D — Đồng bộ toàn app | Login/lobby/result, tên Sắc Trận, Settings/Credits | Không còn đồ họa cũ lẫn vào màn mới; placeholder/ảnh thiếu được phát hiện |
| E — Bản bàn giao | JAR + ZIP + screenshot + báo cáo QA | Kiểm tra tự động qua và có một trận LAN/kiểm tra nghe thực tế |

Dependency: A và B độc lập; C phụ thuộc B; D theo style của C; E sau A/C/D.
Mốc A không chờ hoàn thành cả bộ ảnh. Không tải asset trả phí hoặc thêm thư viện
runtime chỉ để có preview; nếu tải miễn phí bị chặn bởi tài khoản thì ghi rõ nguồn cần lấy.

## 8. Checklist kiểm tra cần bổ sung

- Asset: mọi file trong manifest tồn tại, PNG alpha đúng, kích thước/tỷ lệ đúng;
  test đọc từ JAR ở thư mục ngoài repo và so sprite được dùng tại mọi call site.
- Audio: decode đúng, không clip, loop không click/ngắt, fade/mute hoạt động;
  đánh giá nghe có ghi nhận từ người dùng, không đánh dấu đạt chỉ bằng unit test.
- Feedback: snapshot chỉ đổi timer/khán giả không replay; move sequence và match ID
  chống trùng; reset tái đấu và snapshot khán giả mới vào không phát lại trận cũ.
- Gameplay: giữ nguyên test rule/room/TCP hiện tại và kiểm tra điểm cuối trước result.
- Render: chụp trước/sau ở cùng trạng thái bàn; thêm cảnh vùng bị đổi chủ và khán giả.
- Performance: đo paint với PNG/VFX thật, asset preload, cache size; không suy ra FPS
  thiết bị chỉ từ benchmark offscreen.
- Bàn giao có danh sách phần đã làm và phần chưa thử; nghe BGM, DPI và LAN phải có
  evidence tương ứng, không gom chung thành “đã polish hoàn chỉnh”.

## Nguồn khảo sát

- Eric Matyas, Arcade Puzzler: https://opengameart.org/content/arcade-puzzler-looping
- License được niêm yết: CC BY 3.0, https://creativecommons.org/licenses/by/3.0/
- Fablefly Music, Hi-Score Hustle: https://fablefly-music.itch.io/hiscore-hustle
- Nguồn asset hiện có: `../../assets/ASSET_CREDITS.md`.
- File liên quan đã đọc: ArenaArt, BoardPanel call sites, AssetLoader, SoundManager,
  prepare_audio.py và báo cáo PRISM_VALIDATION.md.
