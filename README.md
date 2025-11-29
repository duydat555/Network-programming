# HLS Server với Giao diện Monitor

Hệ thống HLS Streaming Server với giao diện giám sát client real-time sử dụng Spring Boot và FlatLaf.

![Version](https://img.shields.io/badge/version-0.0.1-blue.svg)
![Java](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-green.svg)

---

## 📋 Mục lục

- [Tính năng](#-tính-năng)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt](#-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [Chạy ứng dụng](#-chạy-ứng-dụng)
- [Sử dụng](#-sử-dụng)
- [Troubleshooting](#-troubleshooting)
- [Kiến trúc](#-kiến-trúc)

---

## ✨ Tính năng

### Backend (Spring Boot)
- ✅ HLS video streaming với HTTP Range requests
- ✅ Hỗ trợ adaptive bitrate (nhiều quality: 1080p, 720p, 480p...)
- ✅ Tracking clients real-time
- ✅ Auto cleanup clients inactive (10 giây)
- ✅ RESTful API endpoints
- ✅ MySQL database integration (User, Movie, Favorite, WatchHistory)

### GUI Monitor (Swing + FlatLaf)
- ✅ Hiển thị danh sách clients đang kết nối (IP address)
- ✅ Hiển thị segment hiện tại mỗi client đang xem
- ✅ Hiển thị tổng số requests và thời gian hoạt động
- ✅ Hiển thị thời gian inactive: `○ (5s)`, `○ (12s)`
- ✅ Chi tiết 10 segments gần nhất của mỗi client
- ✅ Auto refresh mỗi 2 giây
- ✅ FlatLaf Dark theme
- ✅ Nút "Dọn dẹp Client không hoạt động"

---

## 💻 Yêu cầu hệ thống

### Phần mềm bắt buộc:

1. **Java Development Kit (JDK) 25**
   - Download: https://jdk.java.net/25/
   - Hoặc: https://www.oracle.com/java/technologies/downloads/

2. **Apache Maven 3.8+**
   - Download: https://maven.apache.org/download.cgi
   - Hoặc dùng Maven Wrapper (đã có sẵn: `mvnw.cmd`)

3. **MySQL 8.0+**
   - Download: https://dev.mysql.com/downloads/mysql/
   - Hoặc dùng XAMPP/WAMP

4. **IDE (Khuyến nghị)**
   - IntelliJ IDEA: https://www.jetbrains.com/idea/
   - Hoặc Eclipse: https://www.eclipse.org/

### Hệ điều hành:
- ✅ Windows 10/11 (Có GUI support)
- ✅ Linux Desktop (Có X Server)
- ⚠️ Linux Server (Headless) - Cần disable GUI

### Phần cứng tối thiểu:
- CPU: 2 cores
- RAM: 4GB
- Disk: 10GB trống (cho video files)
- Network: 100 Mbps

---

## 📥 Cài đặt

### Bước 1: Clone/Download project

```bash
# Nếu dùng Git
git clone <repository-url>
cd hls-server

# Hoặc download ZIP và giải nén
```

### Bước 2: Cài đặt Java 25

#### Windows:
```bash
# Download JDK 25 từ Oracle hoặc Adoptium
# Cài đặt và set JAVA_HOME

# Kiểm tra version
java -version
# Output mong đợi: java version "25..."
```

#### Linux:
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-25-jdk

# Kiểm tra
java -version
```

### Bước 3: Cài đặt Maven

#### Windows:
```bash
# Download Maven từ https://maven.apache.org/download.cgi
# Giải nén vào C:\Program Files\Apache\maven
# Thêm vào PATH: C:\Program Files\Apache\maven\bin

# Kiểm tra
mvn -version
```

#### Linux:
```bash
sudo apt install maven

# Kiểm tra
mvn -version
```

**Hoặc dùng Maven Wrapper (không cần cài Maven):**
```bash
# Windows
.\mvnw.cmd -version

# Linux/Mac
./mvnw -version
```

### Bước 4: Cài đặt MySQL

#### Windows (XAMPP):
1. Download XAMPP: https://www.apachefriends.org/
2. Cài đặt và start MySQL service
3. Mở phpMyAdmin: http://localhost/phpmyadmin
4. Tạo database mới tên `movie`

#### Linux:
```bash
sudo apt install mysql-server
sudo systemctl start mysql
sudo mysql_secure_installation

# Tạo database
mysql -u root -p
CREATE DATABASE movie;
exit;
```

### Bước 5: Tạo thư mục video

```bash
# Windows
mkdir D:\hls\movie1

# Linux
mkdir -p /var/hls/movie1
```

#### Cấu trúc thư mục video:
```
D:/hls/movie1/
├── 1080p/
│   ├── index.m3u8
│   ├── segment_0.ts
│   ├── segment_1.ts
│   └── ...
├── 720p/
│   ├── index.m3u8
│   ├── segment_0.ts
│   └── ...
└── 480p/
    ├── index.m3u8
    ├── segment_0.ts
    └── ...
```

**Lưu ý:** Bạn cần encode video thành HLS format trước. Xem phần [Encode Video](#encode-video).

### Bước 6: Download dependencies

```bash
cd hls-server
mvn clean install
```

Lệnh này sẽ:
- Download tất cả dependencies (Spring Boot, FlatLaf, MySQL connector, etc.)
- Compile code
- Run tests (nếu có)
- Package thành JAR file

---

## ⚙️ Cấu hình

### File: `backend/src/main/resources/application.properties`

```properties
# Server configuration
spring.application.name=hls-server
server.address=192.168.12.197    # ← THAY ĐỔI IP của bạn
server.port=8080

# GUI Monitor (true = hiển thị giao diện, false = headless)
server.gui.enabled=true

# MySQL Database
spring.datasource.url=jdbc:mysql://localhost:3306/movie?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
spring.datasource.username=root  # ← THAY ĐỔI username
spring.datasource.password=      # ← THAY ĐỔI password (để trống nếu không có)
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### Cấu hình quan trọng:

| Property | Mô tả | Giá trị mặc định |
|----------|-------|------------------|
| `server.address` | IP server sẽ bind | `192.168.12.197` |
| `server.port` | Port server | `8080` |
| `server.gui.enabled` | Bật/tắt GUI | `true` |
| `spring.datasource.url` | MySQL connection URL | `jdbc:mysql://localhost:3306/movie` |
| `spring.datasource.username` | MySQL username | `root` |
| `spring.datasource.password` | MySQL password | *(trống)* |

### Đường dẫn video trong code:

**File:** `backend/src/main/java/com/example/hls_server/api/HlsController.java`

```java
// Line 54
File target = new File("D:/hls/movie1/" + folder + "/" + file);
```

**Thay đổi nếu cần:**
- Windows: `"D:/hls/movie1/"`
- Linux: `"/var/hls/movie1/"`

---

## 🚀 Chạy ứng dụng

### Cách 1: Dùng Maven (Khuyến nghị cho Development)

```bash
cd backend
mvn spring-boot:run
```

**Hoặc dùng Maven Wrapper:**
```bash
# Windows
cd backend
..\mvnw.cmd spring-boot:run

# Linux/Mac
cd backend
../mvnw spring-boot:run
```

### Cách 2: Dùng JAR file (Khuyến nghị cho Production)

```bash
# Build JAR
cd backend
mvn clean package -DskipTests

# Chạy JAR
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Cách 3: Từ IDE (IntelliJ IDEA)

1. Mở project trong IntelliJ
2. Tìm file `HlsServerApplication.java`
3. Click chuột phải → Run 'HlsServerApplication'

### Kết quả mong đợi:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.4.1)

2025-11-25 19:30:15.123  INFO 12345 --- [main] c.e.h.HlsServerApplication : Starting HlsServerApplication...
2025-11-25 19:30:16.456  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.Tomcat : Tomcat started on port(s): 8080 (http)
2025-11-25 19:30:16.789  INFO 12345 --- [main] c.e.h.HlsServerApplication : Started HlsServerApplication in 2.5 seconds
Server UI started successfully
```

✅ **GUI window sẽ tự động hiển thị** (nếu `server.gui.enabled=true`)

---

## 📖 Sử dụng

### 1. Truy cập HLS Stream

**Master Playlist:**
```
http://192.168.12.197:8080/hls/movie1/1080p/index.m3u8
```

**Các quality khác:**
```
http://192.168.12.197:8080/hls/movie1/720p/index.m3u8
http://192.168.12.197:8080/hls/movie1/480p/index.m3u8
```

### 2. Test bằng VLC Media Player

1. Mở VLC
2. Media → Open Network Stream
3. Nhập URL: `http://192.168.12.197:8080/hls/movie1/1080p/index.m3u8`
4. Play

### 3. Test bằng Browser

Tạo file `test.html`:
```html
<!DOCTYPE html>
<html>
<head>
    <title>HLS Test</title>
    <script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>
</head>
<body>
    <video id="video" controls width="800"></video>
    <script>
        var video = document.getElementById('video');
        var videoSrc = 'http://192.168.12.197:8080/hls/movie1/1080p/index.m3u8';
        
        if (Hls.isSupported()) {
            var hls = new Hls();
            hls.loadSource(videoSrc);
            hls.attachMedia(video);
        }
    </script>
</body>
</html>
```

### 4. Sử dụng GUI Monitor

#### Giao diện bảng:
- **IP Address**: Địa chỉ IP của client
- **Current Segment**: Segment hiện tại đang xem
- **Total Requests**: Tổng số requests
- **Last Activity**: Thời gian hoạt động cuối
- **Status**: 
  - `●` = Đang hoạt động (< 10 giây)
  - `○ (5s)` = Không hoạt động 5 giây

#### Xem chi tiết client:
1. Click vào một client trong bảng
2. Panel dưới sẽ hiển thị:
   - Thông tin tổng hợp
   - 10 segments gần nhất

#### Dọn dẹp clients:
- **Auto**: Mỗi 2 giây tự động cleanup clients inactive > 10s
- **Manual**: Click nút "Dọn dẹp Client không hoạt động"

---

## 🔧 Troubleshooting

### Lỗi 1: HeadlessException

```
Exception in thread "AWT-EventQueue-0" java.awt.HeadlessException
```

**Nguyên nhân:** Java đang chạy headless mode

**Giải pháp:**
```properties
# application.properties
server.gui.enabled=false
```

Hoặc thêm VM option:
```bash
java -Djava.awt.headless=false -jar backend.jar
```

**Chi tiết:** Xem `FIX_HEADLESS_EXCEPTION.md`

---

### Lỗi 2: StackOverflowError

**Nguyên nhân:** Đệ quy vô hạn (đã fix trong code hiện tại)

**Giải pháp:** Update code từ repository mới nhất

**Chi tiết:** Xem `FIX_STACKOVERFLOW.md`

---

### Lỗi 3: Cannot connect to MySQL

```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**Kiểm tra:**
1. MySQL service đã chạy chưa?
   ```bash
   # Windows (XAMPP)
   # Start MySQL trong XAMPP Control Panel
   
   # Linux
   sudo systemctl status mysql
   ```

2. Database `movie` đã tạo chưa?
   ```sql
   CREATE DATABASE movie;
   ```

3. Username/password đúng chưa?
   ```properties
   spring.datasource.username=root
   spring.datasource.password=
   ```

---

### Lỗi 4: File not found (video segments)

```
404 Not Found
```

**Kiểm tra:**
1. Thư mục video tồn tại chưa?
   ```bash
   # Windows
   dir D:\hls\movie1\1080p
   
   # Linux
   ls /var/hls/movie1/1080p
   ```

2. Đường dẫn trong code đúng chưa?
   ```java
   // HlsController.java, line 54
   File target = new File("D:/hls/movie1/" + folder + "/" + file);
   ```

3. File `.m3u8` và `.ts` có trong thư mục không?

---

### Lỗi 5: Port 8080 already in use

```
Web server failed to start. Port 8080 was already in use.
```

**Giải pháp:**

**Cách 1:** Đổi port
```properties
# application.properties
server.port=8081
```

**Cách 2:** Kill process đang dùng port 8080
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux
sudo lsof -i :8080
sudo kill -9 <PID>
```

---

### Lỗi 6: Compilation errors (Lombok)

```
cannot find symbol: method getUsername()
```

**Nguyên nhân:** Lombok annotation processing chưa enable

**Giải pháp:**

**IntelliJ IDEA:**
1. File → Settings → Build → Compiler → Annotation Processors
2. Check "Enable annotation processing"
3. Rebuild project

**Eclipse:**
1. Install Lombok plugin
2. Project → Properties → Java Compiler → Annotation Processing
3. Enable annotation processing

---

## 🎬 Encode Video

Để chuyển video sang HLS format, dùng FFmpeg:

### Cài đặt FFmpeg:

**Windows:**
```bash
# Download từ https://ffmpeg.org/download.html
# Giải nén và thêm vào PATH
```

**Linux:**
```bash
sudo apt install ffmpeg
```

### Encode video thành HLS:

```bash
# Tạo thư mục output
mkdir -p D:/hls/movie1/1080p
mkdir -p D:/hls/movie1/720p
mkdir -p D:/hls/movie1/480p

# 1080p
ffmpeg -i input.mp4 \
  -vf scale=1920:1080 \
  -c:v libx264 -b:v 5000k \
  -c:a aac -b:a 192k \
  -hls_time 10 \
  -hls_list_size 0 \
  -hls_segment_filename "D:/hls/movie1/1080p/segment_%03d.ts" \
  "D:/hls/movie1/1080p/index.m3u8"

# 720p
ffmpeg -i input.mp4 \
  -vf scale=1280:720 \
  -c:v libx264 -b:v 2800k \
  -c:a aac -b:a 128k \
  -hls_time 10 \
  -hls_list_size 0 \
  -hls_segment_filename "D:/hls/movie1/720p/segment_%03d.ts" \
  "D:/hls/movie1/720p/index.m3u8"

# 480p
ffmpeg -i input.mp4 \
  -vf scale=854:480 \
  -c:v libx264 -b:v 1400k \
  -c:a aac -b:a 96k \
  -hls_time 10 \
  -hls_list_size 0 \
  -hls_segment_filename "D:/hls/movie1/480p/segment_%03d.ts" \
  "D:/hls/movie1/480p/index.m3u8"
```

---

## 🏗️ Kiến trúc

### Cấu trúc project:

```
hls-server/
├── backend/                    # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/hls_server/
│   │   │   │       ├── HlsServerApplication.java
│   │   │   │       ├── api/
│   │   │   │       │   └── HlsController.java          # HLS streaming endpoint
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── dto/
│   │   │   │       ├── entity/
│   │   │   │       ├── gui/
│   │   │   │       │   └── ServerUI.java               # Swing GUI monitor
│   │   │   │       ├── model/
│   │   │   │       │   └── ClientInfo.java             # Client tracking model
│   │   │   │       ├── repository/
│   │   │   │       └── service/
│   │   │   │           └── ClientMonitor.java          # Client monitoring service
│   │   │   └── resources/
│   │   │       └── application.properties              # Configuration
│   │   └── test/
│   └── pom.xml                                         # Maven dependencies
├── client/                     # Desktop client (Swing)
├── mvnw, mvnw.cmd             # Maven wrapper
├── pom.xml                     # Parent POM
└── README.md                   # This file
```

### Dependencies chính:

```xml
<!-- Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- FlatLaf GUI -->
<dependency>
    <groupId>com.formdev</groupId>
    <artifactId>flatlaf</artifactId>
    <version>3.6.1</version>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

---

## 📝 API Endpoints

### HLS Streaming

**GET** `/hls/movie1/{quality}/{file}`

**Ví dụ:**
```
GET /hls/movie1/1080p/index.m3u8
GET /hls/movie1/1080p/segment_0.ts
GET /hls/movie1/720p/index.m3u8
```

**Headers:**
- `Range` (optional): Byte range request
- `Accept-Ranges`: bytes

**Response:**
- `.m3u8`: `application/vnd.apple.mpegurl`
- `.ts`: `video/mp2t`
- `.m4s`: `video/iso.segment`

---

## 🤝 Đóng góp

Nếu bạn muốn đóng góp cho project:

1. Fork repository
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

## 📞 Liên hệ

Project Link: [https://github.com/your-username/hls-server](https://github.com/your-username/hls-server)

---

## 🎉 Hoàn tất!

**Chúc bạn cài đặt thành công!** 🚀

Nếu gặp vấn đề, xem các file documentation:
- `IMPROVEMENTS.md` - Cải tiến mới nhất
- `FIX_HEADLESS_EXCEPTION.md` - Fix lỗi HeadlessException
- `FIX_STACKOVERFLOW.md` - Fix lỗi StackOverflow
- `DONE.md` - Tổng kết tính năng

**Enjoy streaming!** 📺

