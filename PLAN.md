# Kế Hoạch Chi Tiết Refactor PhotoApp (Article -> User Profile)

Tài liệu này mô tả chi tiết kế hoạch thực hiện chuyển đổi dự án từ ứng dụng hiển thị bài viết (`Article`) sang ứng dụng quản lý thông tin người dùng (`UserProfile`), tích hợp cơ chế tải ảnh với thanh tiến trình (`DownloadWithProgress`) và thiết kế lại giao diện Home - Detail theo đúng hình minh họa trong `example/example.jpg`.

---

## 1. Mục Tiêu & Yêu Cầu

1. **Thay đổi Model (`Article` $\rightarrow$ `UserProfile` & `ArticleList` $\rightarrow$ `UserList`)**:
   - Model `UserProfile` bao gồm các thuộc tính:
     - `id` (`int`): ID người dùng.
     - `username` (`String`): Tên tài khoản / Tên người dùng.
     - `email` (`String`): Địa chỉ email.
     - `desc` / `description` (`String`): Mô tả bản thân / tiểu sử.
     - `avatar_url` (`String`): Đường dẫn URL ảnh đại diện.
     - `hobby` (`String`): Sở thích.
   - Model `UserList`: Chứa danh sách `ArrayList<UserProfile> users`.
   - Hỗ trợ serialization/deserialization qua Gson với `@SerializedName` (hỗ trợ cả `desc` và `description`).

2. **Cập nhật màn hình danh sách (Home Screen - `MainActivity`)**:
   - Đổi `ArticleAdapter` thành `UserAdapter`.
   - Hiển thị danh sách user dạng lưới (`GridView`), mỗi ô bao gồm:
     - Ảnh đại diện (`avatar`).
     - Tên người dùng (`username`).
   - Khi click vào một item user $\rightarrow$ Chuyển sang màn hình Detail (`ViewUserActivity`) kèm theo `id` của user.

3. **Cập nhật màn hình chi tiết (Detail Screen - `ViewUserActivity`)**:
   - Thiết kế giao diện chi tiết theo bản vẽ trên bảng (`example/example.jpg`):
     - **Cột bên trái**: Hiển thị thông tin chi tiết: `User ID`, `Username`, `Email`, `Description`, `Hobby`.
     - **Cột bên phải**: Hiển thị Ảnh đại diện (`avatar`) và thanh tiến trình (`ProgressBar`).
   - Sử dụng phương thức `Downloader.downloadWithProgress(...)` để tải ảnh avatar từ URL về bộ nhớ cache cục bộ, cập nhật phần trăm tiến trình trên `ProgressBar`, sau khi tải xong thì hiển thị ảnh lên `ImageView` và ẩn `ProgressBar`.

4. **Cơ chế nạp dữ liệu (`UserData`)**:
   - Đổi `ArticleData` thành `UserData`.
   - Cung cấp dữ liệu mẫu JSON (hỗ trợ nạp từ URL hoặc fallback qua file JSON trong local `assets`/`raw`) để đảm bảo app luôn chạy mượt mà kể cả khi không có mạng hoặc URL ngoài bị lỗi.

---

## 2. Thiết Kế Giao Diện (UI/UX) Theo `example.jpg`

```
+-------------------------------------------------------------+
|                          HOME SCREEN                        |
+-------------------------------------------------------------+
|  +---------------+  +---------------+  +---------------+    |
|  |    [Avatar]   |  |    [Avatar]   |  |    [Avatar]   |    |
|  |     User 1    |  |     User 2    |  |     User 3    |    |
|  +---------------+  +---------------+  +---------------+    |
|  +---------------+  +---------------+  +---------------+    |
|  |    [Avatar]   |  |    [Avatar]   |  |    [Avatar]   |    |
|  |     User 4    |  |     User 5    |  |     User 6    |    |
|  +---------------+  +---------------+  +---------------+    |
+-------------------------------------------------------------+
                               | Click on User Item
                               v
+-------------------------------------------------------------+
|                         DETAIL SCREEN                       |
+-------------------------------------------------------------+
|  +-----------------------------+  +----------------------+  |
|  | Username: User 1            |  |    [ProgressBar]     |  |
|  | Email: user1@example.com    |  |                      |  |
|  |                             |  |      [ Avatar ]      |  |
|  | Description:                |  |                      |  |
|  | Senior Android Developer... |  +----------------------+  |
|  |                             |                            |
|  | Hobby:                      |                            |
|  | Photography, Coding, Travel |                            |
|  +-----------------------------+                            |
+-------------------------------------------------------------+
```

---

## 3. Chi Tiết Các Thay Đổi & File Cần Chỉnh Sửa

### 3.1. Models
- **`UserProfile.java`** (thay thế hoặc cập nhật từ `Article.java`):
  - Khai báo các trường: `id`, `username`, `email`, `desc`, `avatar_url`, `hobby`.
  - Getter/Setter, Constructor đầy đủ.
  - Sử dụng annotation Gson `@SerializedName` và `@Expose`.
- **`UserList.java`** (thay thế từ `ArticleList.java`):
  - Chứa danh sách `ArrayList<UserProfile> users`.
  - Annotation `@SerializedName("users")`.

### 3.2. Data Manager & Helper
- **`UserData.java`** (thay thế từ `ArticleData.java`):
  - Lưu trữ biến `public static UserList data`.
  - Phương thức tìm kiếm user theo ID: `public static UserProfile getUserFromId(int id)`.
  - Phương thức `loadData(String url, Activity activity)` tải file JSON và parse vào `UserList`, sau đó gán `UserAdapter` cho `GridView`.
- **`Downloader.java`**:
  - Tối ưu hóa hàm `downloadWithProgress`:
    - Nhận vào `url`, `Handler`, `Context`, `where2store`, `ProgressBar`, `ImageView`.
    - Sử dụng OkHttpClient thực hiện tải bất đồng bộ (enqueue).
    - Tính toán chính xác `progress = (int) ((downloadedBytes * 100) / totalBytes)`.
    - Đưa callback về UI Thread thông qua `mainHandler.post(...)` để set progress và hiển thị ảnh bằng `imageView.setImageURI(...)` hoặc `BitmapFactory.decodeFile(...)`.
    - Đảm bảo xử lý lỗi kết nối mạng an toàn (ẩn progress bar nếu lỗi, không crash app).

### 3.3. Adapters & Views
- **`UserAdapter.java`** (thay thế từ `ArticleAdapter.java`):
  - Đổ dữ liệu từ `ArrayList<UserProfile>` vào template layout `user_disp_tpl.xml`.
  - Sử dụng Picasso hoặc Downloader để load avatar thumbnail dạng lưới và gán `username` vào `TextView`.
- **`MainActivity.java`**:
  - Khởi tạo `GridView`, gọi `UserData.loadData(...)`.
  - Sự kiện `onItemClick`: Lấy `id` của user và truyền qua `Intent` sang `ViewUserActivity`.
- **`ViewUserActivity.java`** (thay thế từ `ViewArticleActivity.java`):
  - Nhận `id` từ `Intent`.
  - Lấy thông tin user tương ứng qua `UserData.getUserFromId(id)`.
  - Hiển thị các trường `username`, `email`, `desc`, `hobby` lên các `TextView`.
  - Khởi tạo `ProgressBar` hiển thị tiến trình.
  - Gọi `Downloader.downloadWithProgress(...)` để tải ảnh `avatar_url` với thanh tiến trình thực tế.

### 3.4. Layout Resources
- **`res/layout/user_disp_tpl.xml`**:
  - Layout hiển thị từng ô user trên GridView (ảnh avatar + username textview).
- **`res/layout/activity_view_user.xml`**:
  - Layout 2 cột theo đúng hình vẽ:
    - Cột trái: `LinearLayout` chứa các `TextView` cho `Username`, `Email`, `Description`, `Hobby` (với styling đẹp mắt, dễ đọc).
    - Cột phải: `FrameLayout` / `RelativeLayout` chứa `ImageView` (ảnh đại diện) và `ProgressBar` (ở chính giữa ảnh).
- **`res/values/strings.xml`**, **`res/values/colors.xml`**:
  - Bổ sung các chuỗi tiêu đề (như Label Email, Hobby, Description...) và màu sắc tương phản đẹp.

### 3.5. Manifest & Permissions
- **`AndroidManifest.xml`**:
  - Khai báo activity `ViewUserActivity`.
  - Đảm bảo quyền `android.permission.INTERNET` và `android.permission.ACCESS_NETWORK_STATE` được kích hoạt.

---

## 4. Kế Hoạch Triển Khai Từng Bước (Step-by-Step Implementation)

| Bước | Nội Dung | Chi Tiết Thực Hiện |
| :--- | :--- | :--- |
| **Bước 1** | Tạo Data Source JSON | Tạo file `users.json` trong `assets` hoặc hosting data mẫu chuẩn 6 trường (`id`, `username`, `email`, `desc`, `avatar_url`, `hobby`). |
| **Bước 2** | Triển khai Model Classes | Tạo `UserProfile.java` và `UserList.java` với đầy đủ Gson annotations. |
| **Bước 3** | Cập nhật `Downloader.java` | Hoàn thiện hàm `downloadWithProgress` hỗ trợ tải ảnh và cập nhật UI mượt mà, an toàn. |
| **Bước 4** | Triển khai `UserData.java` | Viết logic nạp dữ liệu và chuyển đổi JSON sang danh sách `UserProfile`. |
| **Bước 5** | Triển khai `UserAdapter.java` & Layout Template | Thiết kế `user_disp_tpl.xml` và code `UserAdapter.java`. |
| **Bước 6** | Cập nhật `MainActivity.java` | Kết nối `UserData` và `UserAdapter` với `GridView` trong `activity_main.xml`. |
| **Bước 7** | Thiết kế `activity_view_user.xml` | Thiết kế layout 2 cột (thông tin bên trái, avatar + progress bar bên phải) chuẩn theo `example.jpg`. |
| **Bước 8** | Triển khai `ViewUserActivity.java` | Viết logic load thông tin chi tiết và tải ảnh bằng `downloadWithProgress`. |
| **Bước 9** | Cập nhật `AndroidManifest.xml` | Đăng ký Activity mới, dọn dẹp các tham chiếu cũ (`Article`). |
| **Bước 10** | Build & Kiểm Thử | Chạy lệnh `./gradlew assembleDebug` để kiểm tra compile không lỗi, kiểm tra luồng click và hiển thị. |

---

## 5. Kế Hoạch Kiểm Thử (Verification Plan)

1. **Kiểm thử biên dịch (Build Verification)**:
   - Chạy Gradle build: `.\gradlew assembleDebug` để đảm bảo code Java và XML layout không có lỗi cú pháp hoặc thiếu tài nguyên.
2. **Kiểm thử chức năng (Functional Verification)**:
   - Màn hình Home: Kiểm tra `GridView` nạp đúng danh sách user, hiển thị avatar và username.
   - Chuyển màn hình: Click vào từng user $\rightarrow$ mở đúng `ViewUserActivity` với dữ liệu chính xác của user đó.
   - Tải ảnh có tiến trình: `ProgressBar` xoay/tăng % tiến trình trong lúc tải và tự động ẩn khi ảnh avatar hiển thị lên `ImageView`.
   - Layout chi tiết: Các thông tin `id`, `username`, `email`, `desc`, `hobby` và `avatar` được căn chỉnh đúng vị trí như trong ảnh phác thảo `example.jpg`.
