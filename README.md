# Color Match Arena

Tên game giữ tiếng Anh. Bản hiện tại **2.2** dùng tile v2, capture từ đúng tâm tia,
phản hồi đặt quân và trạng thái đấu lại của cả hai người.
Gói mới: **dist/color-match-arena/**; ZIP: **dist/ColorMatchArena-Client.zip**.

[Plan game feel](docs/plans/color-match-arena-game-feel-v3.md) · [Kiểm tra 2.2](docs/COLOR_MATCH_ARENA_22.md) · [Ảnh capture](docs/screenshots/color-match-arena/game-feel-v3/capture-timeline.png)

**Khởi động lại cả server và client bằng bản 2.2**, kể cả laptop thứ hai. Protocol 3
bổ sung tâm tia và readiness đấu lại; không dùng lẫn JAR 2.1. BGM do người dùng chọn
và volume đã lưu được giữ nguyên.

Thông tin các bản trước ở dưới chỉ để đối chiếu; tên sản phẩm hiện tại là Color Match Arena.

Lịch sử bản **Sắc Trận 2.0**, dùng bộ PNG riêng và bài BGM do người dùng chọn.
Chạy `Chay_Server.bat` / `Chay_Client.bat`; gửi `dist/SacTran-Client.zip` cho laptop khác.

[Hướng dẫn bản mới](docs/SAC_TRAN_SETUP.md) · [Ảnh game thật](docs/screenshots/sac-tran/game.png) · [Kiểm tra](docs/SAC_TRAN_VALIDATION.md)

Các mục Prism bên dưới là thông tin phiên bản trước. Gói hiện tại ở **dist/sac-tran/**.

Bản chạy thử client-server của game Đấu Trí Sắc Màu.

## Bản Prism — giao diện và âm thanh mới

Yêu cầu **JDK 24** và `JAVA_HOME` trỏ tới JDK. Chạy `Chay_Server.bat` và
`Chay_Client.bat` tại thư mục repo; launcher tự biên dịch source UTF-8 và dùng
build mới, không dùng `.class` cũ được commit sẵn.

Client để gửi sang laptop khác nằm trong **`dist/prism/`**. Thư mục `dist/` gốc
và `ColorMatch-Client.zip` là bản cũ. Server và client Prism phải cùng phiên bản
vì BoardState mới bổ sung dữ liệu tia chiếm lãnh.

- [Cài đặt và chơi LAN](docs/PRISM_SETUP.md)
- [Ảnh giao diện thật](docs/screenshots/game.png)
- [Nguồn asset / giấy phép](assets/ASSET_CREDITS.md)
- [Kết quả kiểm tra](docs/PRISM_VALIDATION.md)

```powershell
# PowerShell tại thư mục dự án; nhập mật khẩu MySQL cục bộ.
$credential = Get-Credential -UserName root -Message 'Mat khau MySQL'
$env:COLOR_MATCH_DB_PASSWORD = $credential.GetNetworkCredential().Password
.\Chay_Server.bat
# Mở terminal khác rồi chạy .\Chay_Client.bat
```

Kiểm tra tự động: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Test.ps1`.

## Cấu trúc

```text
common/   MessageType, NetworkMessage, BoardState, PlayerInfo
server/   TCP server, tài khoản online, thách đấu, GameRoom, luật game
client/   MVP client
  * View: LoginFrame, LobbyFrame, GameFrame
  * Presenter: LoginPresenter, LobbyPresenter, GamePresenter
  * Model/service: ServerConnection
```

## MVP client

- `LoginFrame`, `LobbyFrame`, `GameFrame` chỉ dựng giao diện và phát callback.
- `LoginPresenter`, `LobbyPresenter`, `GamePresenter` điều phối sự kiện UI,
  message từ server và chuyển màn hình.
- `ServerConnection` chỉ chịu trách nhiệm truyền nhận TCP.
- Luật trận đấu vẫn thuộc server trong `GameRoom` và `GameRules`.

## Chạy server

### Chuẩn bị MySQL XAMPP

1. Mở XAMPP và Start `MySQL`.
2. Mở phpMyAdmin tại `http://localhost/phpmyadmin`.
3. Chạy toàn bộ file `database.sql`. File này tạo database `color_mind_game`.
4. Tải **MySQL Connector/J** từ trang MySQL, đặt file `.jar` vào thư mục `lib/`.

Cấu hình mặc định trong `server/DBConnection.java` là:

```text
host: 127.0.0.1
port: 3306
database: color_mind_game
user: root
password: rỗng (mặc định XAMPP)
```

Nếu `root` có mật khẩu, đặt biến môi trường `COLOR_MATCH_DB_PASSWORD` như hướng dẫn trên.

### Biên dịch và chạy

Mở Terminal tại thư mục dự án. Thay tên file Connector/J bằng file bạn đã tải:

```bash
javac -cp "lib/mysql-connector-j.jar" common/*.java server/*.java client/*.java
java -cp ".;lib/mysql-connector-j.jar" server.ColorMatchServer
```

Server mặc định chạy tại cổng `5000`.

## Chạy client

Mở Terminal khác:

```bash
java -cp . client.ClientApp
```

Mở client lần thứ hai bằng lệnh trên để mô phỏng người chơi thứ hai.

## Luồng thử nghiệm

1. Mở server.
2. Mở hai client.
3. Đăng ký hai tài khoản khác nhau.
4. Đăng nhập hai client.
5. Chọn người chơi còn lại trong lobby và bấm `Thách đấu`.
6. Client còn lại bấm chấp nhận.
7. Chơi trên bàn 9x9, mỗi lượt có 15 giây.

## Phạm vi bản chạy thử

- Đã có TCP client-server, đăng nhập, đăng ký, online list, thách đấu, bàn cờ,
  timer, luật chuỗi 3, tia chiếm lĩnh, xin thua, thoát và xếp hạng.
- Tài khoản, điểm và lịch sử trận đấu được lưu qua DAO/JDBC vào MySQL.
- Import phần tạo database và bảng trong `database.sql` một lần. Câu SELECT minh họa
  dùng `WHERE mp.user_id = ?` là tham số JDBC; bỏ qua khi import bằng Workbench.
