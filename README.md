# Color Match Arena

Bản chạy thử client-server của game Đấu Trí Sắc Màu.

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

Nếu bạn đã đặt mật khẩu cho `root`, sửa hằng `PASSWORD` trong `DBConnection.java`.

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
- Dữ liệu tài khoản và xếp hạng hiện lưu trong bộ nhớ server, nên sẽ mất khi
  server tắt.
- `database.sql` đã có sẵn để nối tầng DAO/JDBC khi thêm MySQL Connector/J.
