# Tóm tắt thay đổi - Xóa các trường bitrate và fileSize

## Các file đã cập nhật:

### Backend

1. **VideoQuality.java** (Entity)
   - ✅ Đã xóa trường `bitrate` (Integer)
   - ✅ Đã xóa trường `fileSize` (Long)

2. **VideoQualityDto.java** (DTO)
   - ✅ Đã xóa trường `bitrate`
   - ✅ Đã xóa trường `fileSize`

3. **VideoQualityCreateRequest.java** (DTO)
   - ✅ Đã xóa trường `bitrate`
   - ✅ Đã xóa trường `fileSize`

4. **MovieServiceImpl.java** (Service)
   - ✅ Đã xóa việc set `bitrate` và `fileSize` khi tạo VideoQuality
   - ✅ Đã xóa việc map `bitrate` và `fileSize` trong method toDto

### Database Migration

5. **db_migration_video_quality.sql**
   - ✅ Đã cập nhật script tạo bảng, bỏ các cột `bitrate` và `file_size`
   - ✅ Đã cập nhật ví dụ INSERT

### Documentation

6. **VIDEO_QUALITY_FEATURE.md**
   - ✅ Đã cập nhật cấu trúc bảng
   - ✅ Đã cập nhật ví dụ JSON request
   - ✅ Đã cập nhật ví dụ JSON response
   - ✅ Đã cập nhật code ví dụ cho client
   - ✅ Đã xóa khuyến nghị liên quan đến bitrate và fileSize

## Cấu trúc mới của VideoQuality:

```java
public class VideoQuality {
    private Long id;
    private Movie movie;
    private String quality;      // "360p", "480p", "720p", "1080p", "4K"
    private String videoUrl;     // URL của video HLS
    private Boolean isDefault;   // Chất lượng mặc định
}
```

## Lưu ý khi chạy lại ứng dụng:

- JPA sẽ tự động cập nhật schema database (do `spring.jpa.hibernate.ddl-auto=update`)
- Bảng `video_qualities` sẽ được tạo mới hoặc cập nhật
- Nếu đã có dữ liệu cũ trong bảng `video_qualities`, các cột `bitrate` và `file_size` sẽ không bị xóa tự động
- Để dọn dẹp hoàn toàn, có thể chạy:
  ```sql
  ALTER TABLE video_qualities DROP COLUMN IF EXISTS bitrate;
  ALTER TABLE video_qualities DROP COLUMN IF EXISTS file_size;
  ```

