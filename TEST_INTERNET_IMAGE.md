# Hướng dẫn hiển thị Poster từ URL Internet

## Đã hoàn thiện

✅ **MovieApi.java** - Đã cải thiện với các tính năng:
- ✅ Tải ảnh từ URL internet (http/https)
- ✅ Cache ảnh để tránh tải lại
- ✅ Resize ảnh về kích thước 160x220
- ✅ Timeout 5 giây cho mỗi request
- ✅ User-Agent header để tránh bị block
- ✅ Xử lý lỗi và fallback về placeholder

✅ **CardMovie.java** - Đã update:
- ✅ Tải ảnh bất đồng bộ (không làm đơ UI)
- ✅ Hiển thị placeholder khi đang tải
- ✅ Tự động cập nhật UI khi ảnh tải xong

## Cách hoạt động

### 1. Khi Dashboard load
```java
Dashboard -> MovieApi.getMovies()
  -> Lấy danh sách movies từ API
  -> Mỗi movie có posterUrl (URL internet)
  -> Set placeholder icon tạm thời
  -> Trả về danh sách movies
```

### 2. Khi hiển thị CardMovie
```java
CardMovie constructor
  -> Hiển thị placeholder
  -> Gọi loadPosterAsync()
  -> MovieApi.loadPosterAsync()
    -> SwingWorker chạy background thread
    -> loadImageFromUrl(posterUrl)
      -> Kiểm tra cache
      -> Nếu chưa có: download từ internet
      -> Resize về 160x220
      -> Lưu vào cache
    -> Callback: cập nhật UI với ảnh mới
```

## Các tính năng chính

### Image Caching
```java
private static final Map<String, ImageIcon> imageCache = new HashMap<>();
```
- Lưu ảnh đã tải để không phải tải lại
- Tăng tốc độ hiển thị khi scroll

### Connection Timeout
```java
connection.setConnectTimeout(5000); // 5 giây
connection.setReadTimeout(5000);
```
- Tránh treo app khi URL không phản hồi

### User-Agent Header
```java
connection.setRequestProperty("User-Agent", "Mozilla/5.0");
```
- Một số website chặn request không có User-Agent
- Giả làm browser để tránh bị chặn

### Image Resizing với Quality cao
```java
g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
```
- Resize ảnh với chất lượng tốt
- Kích thước chuẩn: 160x220 pixels

### Async Loading với SwingWorker
```java
SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
    @Override
    protected ImageIcon doInBackground() {
        return loadImageFromUrl(movie.getPosterUrl());
    }
    
    @Override
    protected void done() {
        // Cập nhật UI trên EDT thread
        SwingUtilities.invokeLater(onComplete);
    }
};
```
- Tải ảnh trong background thread
- Không làm đơ UI
- Tự động cập nhật UI khi xong

## Ví dụ URL hợp lệ

### URL trực tiếp đến ảnh
```
https://example.com/posters/movie1.jpg
https://image.tmdb.org/t/p/w500/abc123.jpg
http://192.168.1.7:8080/images/poster.png
```

### Định dạng được hỗ trợ
- ✅ JPG/JPEG
- ✅ PNG
- ✅ GIF
- ✅ BMP
- ✅ WBMP

## Test thử

### 1. Test với URL thực tế
Thay đổi posterUrl trong database thành URL thực:
```sql
UPDATE movies 
SET poster_url = 'https://image.tmdb.org/t/p/w500/abc123.jpg'
WHERE id = 1;
```

### 2. Test với nhiều movies
API sẽ:
- Load danh sách movies nhanh (không đợi ảnh)
- Hiển thị placeholder ngay lập tức
- Từ từ tải ảnh từng cái một trong background
- Cập nhật UI khi mỗi ảnh tải xong

### 3. Kiểm tra console
Sẽ thấy log:
```
Đang tải ảnh từ: https://...
Đã tải thành công ảnh: https://...
```

Nếu lỗi:
```
Lỗi khi tải ảnh từ: https://...
Chi tiết lỗi: ...
```

## Xử lý lỗi

### URL không hợp lệ
- ✅ Hiển thị placeholder "No Image"

### Timeout
- ✅ Sau 5 giây tự động fallback về placeholder

### Website chặn request
- ✅ Có User-Agent header
- ✅ Nếu vẫn bị chặn: hiển thị placeholder

### Ảnh quá lớn
- ✅ Tự động resize về 160x220
- ✅ Không làm treo app

## Performance

### Memory
- Cache giữ ảnh đã tải (tiết kiệm bandwidth)
- Mỗi ảnh ~50-100KB sau khi resize

### Network
- Tải song song nhiều ảnh cùng lúc
- Timeout 5 giây mỗi request
- Retry không tự động (có thể thêm nếu cần)

### UI Responsiveness
- UI không bao giờ bị đơ
- Tải ảnh trong background
- Cập nhật từng card khi ảnh sẵn sàng

## Lưu ý quan trọng

### 1. CORS và Security
- ✅ Java URLConnection không bị CORS (khác browser)
- ✅ Hỗ trợ HTTP và HTTPS
- ⚠️ Một số CDN có thể chặn hotlinking

### 2. Image Format
- ✅ ImageIO hỗ trợ hầu hết format phổ biến
- ⚠️ SVG không được hỗ trợ trực tiếp

### 3. Cache Management
- ⚠️ Cache giữ trong memory (không persist)
- ⚠️ Restart app sẽ mất cache
- 💡 Có thể thêm disk cache nếu cần

### 4. Error Recovery
- ✅ Lỗi tải ảnh không crash app
- ✅ Hiển thị placeholder nếu lỗi
- 💡 Có thể thêm retry mechanism nếu cần

## Code example

### Cách sử dụng trong Dashboard
```java
public Dashboard() {
    // ... setup UI ...
    loadMovies(); // Tự động load và hiển thị ảnh
}

private void loadMovies() {
    SwingWorker<List<Movie>, Void> worker = new SwingWorker<>() {
        @Override
        protected List<Movie> doInBackground() throws Exception {
            return MovieApi.getMovies(); // Lấy movies với placeholder
        }
        
        @Override
        protected void done() {
            List<Movie> movies = get();
            displayMovies(movies); // CardMovie sẽ tự tải ảnh
        }
    };
    worker.execute();
}
```

### Cách sử dụng trong CardMovie
```java
public CardMovie(Movie movie, Consumer<Movie> event) {
    this.movie = movie;
    init(); // Setup UI với placeholder
    loadPosterAsync(); // Tải ảnh thật trong background
}

private void loadPosterAsync() {
    MovieApi.loadPosterAsync(movie, () -> {
        // Callback: cập nhật UI khi ảnh ready
        imageLabel.setIcon(new AvatarIcon(movie.getPoster(), 160, 220, 20));
        imageLabel.revalidate();
        imageLabel.repaint();
    });
}
```

## Troubleshooting

### Ảnh không hiển thị
1. ✅ Kiểm tra URL có đúng không
2. ✅ Kiểm tra console có lỗi không
3. ✅ Test URL trong browser
4. ✅ Kiểm tra network connection
5. ✅ Thử URL khác để test

### Ảnh tải chậm
1. ✅ Normal - ảnh tải từ internet
2. ✅ Cache sẽ giúp lần sau nhanh hơn
3. 💡 Có thể giảm timeout nếu cần
4. 💡 Có thể pre-load ảnh nếu cần

### Ảnh bị méo
- ✅ Đã có resize logic với aspect ratio
- ✅ Kích thước fix 160x220
- 💡 Có thể điều chỉnh POSTER_WIDTH/HEIGHT

## Kết luận

✅ **Hoàn tất!** Code đã sẵn sàng để:
- Tải ảnh từ URL internet (http/https)
- Hiển thị trong CardMovie với UI mượt mà
- Cache và optimize performance
- Xử lý lỗi gracefully

🚀 **Chạy app và test ngay!**

