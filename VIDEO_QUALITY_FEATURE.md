# Video Quality Feature

## Tổng quan
Hệ thống đã được thiết kế lại để hỗ trợ nhiều chất lượng video cho mỗi phim. Người dùng có thể chọn chất lượng phù hợp với băng thông của họ.

## Cấu trúc Database

### Bảng `video_qualities`
- `id`: ID tự động tăng
- `movie_id`: ID phim (foreign key)
- `quality`: Chất lượng video (ví dụ: "360p", "480p", "720p", "1080p", "4K")
- `video_url`: URL của video HLS (.m3u8)
- `bitrate`: Bitrate của video (kbps) - tùy chọn
- `file_size`: Kích thước file (bytes) - tùy chọn
- `is_default`: Đánh dấu chất lượng mặc định

## API Usage

### 1. Tạo phim mới với nhiều chất lượng

**Endpoint:** `POST /api/movies`

**Request Body:**
```json
{
  "title": "Phim mẫu",
  "description": "Mô tả phim",
  "year": 2024,
  "durationMin": 120,
  "rating": 8.5,
  "posterUrl": "https://example.com/poster.jpg",
  "backdropUrl": "https://example.com/backdrop.jpg",
  "genreIds": [1, 2, 3],
  "videoQualities": [
    {
      "quality": "360p",
      "videoUrl": "http://192.168.1.7:8080/hls/movie1/360p/playlist.m3u8",
      "bitrate": 800,
      "fileSize": 524288000,
      "isDefault": false
    },
    {
      "quality": "720p",
      "videoUrl": "http://192.168.1.7:8080/hls/movie1/720p/playlist.m3u8",
      "bitrate": 2500,
      "fileSize": 1572864000,
      "isDefault": true
    },
    {
      "quality": "1080p",
      "videoUrl": "http://192.168.1.7:8080/hls/movie1/1080p/playlist.m3u8",
      "bitrate": 5000,
      "fileSize": 3145728000,
      "isDefault": false
    }
  ]
}
```

### 2. Response khi lấy thông tin phim

**Endpoint:** `GET /api/movies/{id}`

**Response:**
```json
{
  "success": true,
  "message": "Lấy dữ liệu thành công",
  "data": {
    "id": 1,
    "title": "Phim mẫu",
    "description": "Mô tả phim",
    "year": 2024,
    "durationMin": 120,
    "rating": 8.5,
    "posterUrl": "https://example.com/poster.jpg",
    "backdropUrl": "https://example.com/backdrop.jpg",
    "genres": ["Hành động", "Phiêu lưu"],
    "videoQualities": [
      {
        "id": 1,
        "quality": "360p",
        "videoUrl": "http://192.168.1.7:8080/hls/movie1/360p/playlist.m3u8",
        "isDefault": false
      },
      {
        "id": 2,
        "quality": "720p",
        "videoUrl": "http://192.168.1.7:8080/hls/movie1/720p/playlist.m3u8",
        "isDefault": true
      },
      {
        "id": 3,
        "quality": "1080p",
        "videoUrl": "http://192.168.1.7:8080/hls/movie1/1080p/playlist.m3u8",
        "isDefault": false
      }
    ]
  }
}
```

## Client Implementation

### Sử dụng trong Desktop Client

Khi nhận được danh sách video qualities, client có thể:

1. **Hiển thị menu chọn chất lượng:**
```java
List<VideoQualityDto> qualities = movie.getVideoQualities();
for (VideoQualityDto quality : qualities) {
    // Tạo menu item cho mỗi chất lượng
    System.out.println(quality.getQuality());
}
```

2. **Chọn chất lượng mặc định:**
```java
VideoQualityDto defaultQuality = movie.getVideoQualities().stream()
    .filter(q -> q.getIsDefault() != null && q.getIsDefault())
    .findFirst()
    .orElse(movie.getVideoQualities().get(0));

String videoUrl = defaultQuality.getVideoUrl();
// Play video with videoUrl
```

3. **Cho phép người dùng chuyển đổi chất lượng:**
```java
// Người dùng chọn chất lượng mới
String selectedQuality = "1080p"; // từ UI
VideoQualityDto selected = movie.getVideoQualities().stream()
    .filter(q -> q.getQuality().equals(selectedQuality))
    .findFirst()
    .orElse(null);

if (selected != null) {
    // Lưu vị trí hiện tại
    long currentPosition = player.getCurrentPosition();
    
    // Đổi sang URL mới
    player.setVideoUrl(selected.getVideoUrl());
    
    // Tiếp tục từ vị trí cũ
    player.seekTo(currentPosition);
}
```

## Lưu ý Migration

Nếu bạn có dữ liệu phim cũ với trường `video_url`, bạn cần:

1. Chuyển đổi dữ liệu cũ sang bảng `video_qualities`
2. Xóa cột `video_url` từ bảng `movies`

Script SQL mẫu đã được tạo ở file `db_migration_video_quality.sql`

## Khuyến nghị

- Luôn có ít nhất một chất lượng được đánh dấu `isDefault = true`
- Sắp xếp chất lượng từ thấp đến cao: 360p, 480p, 720p, 1080p, 4K


