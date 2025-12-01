# Cập nhật hiển thị chất lượng video cho Client Monitor

## Vấn đề
Cột "Chất lượng" trong giao diện ServerUI luôn hiển thị "N/A" vì segment path không bao gồm thông tin về folder chất lượng.

## Nguyên nhân
Trong `HlsController.java`, khi gọi `clientMonitor.trackClient()`, chỉ truyền tên file (ví dụ: `avatar220.ts`) mà không truyền folder (ví dụ: `720p`).

## Giải pháp đã áp dụng

### 1. HlsController.java
Cập nhật để truyền đường dẫn đầy đủ bao gồm folder:

**Trước:**
```java
clientMonitor.trackClient(clientIp, file);
```

**Sau:**
```java
String segmentPath = folder + "/" + file;
clientMonitor.trackClient(clientIp, segmentPath);
```

### 2. ClientInfo.java
Method `extractQualityFromSegment()` sẽ tự động trích xuất chất lượng từ path:
- Input: `720p/avatar220.ts`
- Output: `currentQuality = "720p"`

## Cách hoạt động

1. Client request: `GET /hls/movie1/720p/avatar220.ts`
2. HlsController nhận:
   - `folder = "720p"`
   - `file = "avatar220.ts"`
3. Tạo segmentPath: `"720p/avatar220.ts"`
4. Gọi `clientMonitor.trackClient(clientIp, "720p/avatar220.ts")`
5. ClientInfo.extractQualityFromSegment() tìm pattern `\d+p` → tìm thấy "720p"
6. Lưu vào `currentQuality = "720p"`
7. ServerUI hiển thị "720p" trong cột "Chất lượng"

## Kết quả

Bây giờ khi client xem video, giao diện sẽ hiển thị:

```
Địa chỉ IP: 192.168.83.123
Chất lượng: 720p           ← Hiển thị đúng chất lượng
Tổng Request: 2
Hoạt động cuối: 21:06:56
Trạng thái: Đang hoạt động ●
```

## Cấu trúc đường dẫn hỗ trợ

Hệ thống hỗ trợ các pattern chất lượng sau:
- `/hls/movie1/360p/segment.ts` → 360p
- `/hls/movie1/480p/segment.ts` → 480p
- `/hls/movie1/720p/segment.ts` → 720p
- `/hls/movie1/1080p/segment.ts` → 1080p
- `/hls/movie1/4K/segment.ts` → N/A (không match pattern `\d+p`)

Nếu muốn hỗ trợ "4K", có thể cập nhật regex trong `ClientInfo.extractQualityFromSegment()`:
```java
if (part.matches("\\d+p|4K|8K")) { // Hỗ trợ 4K, 8K
    this.currentQuality = part;
    return;
}
```

