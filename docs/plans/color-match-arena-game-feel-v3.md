# Color Match Arena — Tile Readability & Game Feel

Trạng thái 2.2: đã triển khai phần code P1–P3 và audio mix P4. Đã kiểm tra tự động
rule/room/TCP/presentation và ảnh render thật. Nghe thử trên loa/tai nghe, quay trận
thật, kiểm tra LAN hai laptop và DPI vật lý vẫn cần kiểm tra thủ công.
Build/test/report: [bản 2.2](../COLOR_MATCH_ARENA_22.md).

## 1. Phạm vi và nguyên tắc

- Tên sản phẩm luôn là **Color Match Arena**. Giữ tiếng Việt cho hướng dẫn/nút hiện tại;
  không dịch tên game, không đổi database/package/protocol chỉ để đổi branding.
- Giữ BGM MP3 đã được người dùng chọn. Không đổi nhạc hoặc reset volume đã lưu.
- Bàn, quân và lượt là nguồn thông tin chính. Hiệu ứng giúp đọc hành động; không thêm
  rung toàn cửa sổ, bloom, particle liên tục hoặc lời quảng cáo.
- Server quyết định nước hợp lệ, điểm và lượt. Pending ở client chỉ là thông báo gửi,
  không phải dự đoán kết quả hay tự cập nhật quân chính thức.

## 2. Nguyên nhân và hướng tile mới

Đã kiểm tra: neutral cũ ngả xanh, tile xanh gần màu thép xám, mặt ceramic nhiều hạt
và viền sáng lặp 81 lần. BoardPanel còn phủ màu tối alpha 100/255 lên vùng đã chiếm.
Các yếu tố này làm phe xanh dễ lẫn neutral và làm màu phe mất độ rõ khi vào game.

| Trạng thái | Tile v2 | Dấu hiệu ngoài màu |
|---|---|---|
| Neutral | Graphite charcoal, ít saturation và texture | Không pattern |
| Red territory | Coral đỏ rõ, rim đỏ hồng dịu | Ba dải chéo song song |
| Blue territory | Cobalt xanh rõ, rim cyan dịu | Lưới hình thoi lớn |

Tất cả có cùng hình vuông, độ bo, padding, hướng sáng và độ dày bevel. Không dùng
viền trắng lấp lánh. Nhịp texture phải còn nhận ra ở 32 px, không chỉ đẹp ở ảnh lớn.
Quân đỏ tròn đa giác/quân xanh thoi vẫn sáng hơn nền để phân biệt quân với lãnh thổ.

Asset v2 lưu riêng trong assets/images/tiles, đổi catalog sang file mới; giữ tile cũ
để so sánh. Không cắt từ concept hoặc tô lại bitmap bằng script.

## 3. Điều chỉnh nhỏ thực hiện cùng tile

- Bỏ lớp phủ tối cố định lên tile có màu; rim renderer dùng đúng màu phe.
- Hover outline theo màu người chơi. Ghost nhẹ trên ô hợp lệ; không tô cả bàn.
- Pending: ghost mờ và viền đứt tại ô vừa gửi, header “Đang gửi nước đi…”. Chỉ một
  request đang chờ; tick đồng hồ không mở khóa request. Error/reset/lượt mới giải phóng.
- Quân đã được xác nhận pop-in với ease-out 180 ms thay cho scale tuyến tính cứng.
- Tên tiếng Anh qua AppBrand, title launcher/server, gói ColorMatchArena-Client.jar.

## 4. Plan polish tiếp theo

### P1 — Nhịp một nước đi

1. Hover hợp lệ: 60–90 ms, ghost cùng shape/màu, focus bàn phím rõ.
2. Click/gửi: phản hồi thị giác ngay, không bật tiếng “đặt thành công” trước server.
3. Server xác nhận: pop 120–180 ms, tối đa overshoot nhẹ nếu thử thấy cần; ring nước cuối
   giữ tĩnh sau khi animation hết, không pulse vô hạn.
4. Nước bị từ chối: giữ board đúng snapshot, unlock pending, inline reason ngắn;
   tránh modal cắt nhịp cho lỗi ô không hợp lệ có thể sửa ngay.

Kiểm tra: click/Enter lặp, nước trên lãnh thổ mình, từ chối từ server, mạng chậm,
snapshot timer trong lúc pending. Không đặt thêm quân ở client để che latency.

### P2 — Capture có nhịp và đọc được

Timeline đề xuất, cần thử trong trận thật:

| Thời điểm | Feedback |
|---|---|
| 0–100 ms | Highlight nhóm nguồn dựa trên dữ liệu server/previous snapshot |
| 80–280 ms | Tia quét từ vị trí kích hoạt, chặn đúng đầu cuối authoritative |
| 180–400 ms | Vùng thay phe chuyển màu ngắn, vẫn giữ pattern dễ nhận |
| 250–900 ms | Nhãn số ô thay đổi và một accent score, không trì hoãn điểm thật |

Để tia tỏa từ tâm đúng tuyệt đối, nếu endpoints hiện tại không đủ thì bổ sung tâm
được GameRules ghi nhận; không suy diễn lại luật trong paint. Dedupe bằng match ID +
move sequence. Capture nhiều ô chỉ phát một cue chính, không phát 1 sound mỗi tile.
Reduced motion dùng màu/highlight tĩnh, không bỏ thông tin.

### P3 — Lượt và kết quả

- Chuyển lượt: player-card border + nhãn, cue âm ngắn; không thay BGM mỗi lượt.
- Countdown: nhấn 5 giây cuối bằng thanh/nhãn, cue tối đa một lần/giây trên lượt mình.
- Kết quả: mở sau khi final snapshot được áp; dùng thắng/thua/hòa và đúng điểm.
- Chờ tái đấu: thể hiện “Bạn đã sẵn sàng / Chờ đối thủ”; nếu cần trạng thái peer mới
  thì thêm message rõ ràng, không giả từ timer local.

### P4 — Rà âm thanh và trạng thái ngoài trận

- Giữ bài nhạc đã chọn, nghe một trận 5–10 phút trên loa laptop và tai nghe.
- Cân lại click/move/capture/tick theo loudness cảm nhận; không mặc định cùng peak
  là cùng độ to. Ưu tiên capture/turn, giảm chat hoặc cue trùng.
- Challenge/loading/error/empty state đồng bộ theme; thoát mạng có đường về login.
- Không thêm shop, currency, bot hoặc cơ chế mới vào đợt game-feel này.

## 5. Cổng kiểm tra

- PNG: alpha thật, geometry nhất quán; mẫu nhìn ở 32/48/64 px với quân chồng lên.
- Board fixture có đủ hai vùng màu rộng và quân trên vùng của mình; không chỉ xem
  một hàng đỏ và một ô xanh bị quân che.
- Sanity check màu vùng trung tâm: red có R trội, blue có B trội, neutral tối/ít màu;
  đây là kiểm tra dự án, không thay đánh giá accessibility.
- Rà bằng mắt pattern khi giảm saturation, DPI 100/125/150% và cửa sổ nhỏ.
- Compile, rule/room/TCP, tutorial, pending input, asset/JAR checks hiện có phải qua.
- Đo frame time khi capture và nhiều quân, không suy FPS từ ảnh concept.
- Các bước cần hai laptop/loa thật ghi riêng, không báo đã qua chỉ từ unit test.

## 6. Thứ tự bàn giao

1. Tile v2 + screenshot bàn thật + tên tiếng Anh (đợt này).
2. P1 một nước đi, đo với latency thử; giữ gameplay nguyên trạng.
3. P2 capture, chỉnh timeline từ một video ngắn quay trận thật.
4. P3/P4 chuyển lượt, tái đấu, audio mix và QA LAN.

Gói mới: dist/color-match-arena/. Bản sac-tran/prism cũ là lịch sử, không phải tên
sản phẩm hiện tại. Prompt tile v2 và kết quả kiểm tra lưu cùng bản bàn giao.
