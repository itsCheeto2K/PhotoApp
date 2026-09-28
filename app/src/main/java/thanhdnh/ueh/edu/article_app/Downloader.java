package thanhdnh.ueh.edu.article_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class Downloader {
  public static String cached_file_path = "";

  public static File downloadFile(String url, File cached) {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(url).build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) return null;
      String contentType = response.header("Content-Type", "");
      String extension = getExtensionFromMimeType(contentType);
      File file = File.createTempFile("downloaded_file", extension, cached);
      if (response.body() != null) {
        BufferedSink sink = Okio.buffer(Okio.sink(file));
        sink.writeAll(response.body().source());
        sink.close();
        return file;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void downloadWithProgress(String inputurl, Handler mainHandler, Context context, File where2store, ProgressBar progressBar, ImageView imageView) {
    if (progressBar != null) {
      mainHandler.post(() -> {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
      });
    }

    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(inputurl).build();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        mainHandler.post(() -> {
          if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
          }
        });
      }

      @Override
      public void onResponse(Call call, Response response) {
        if (!response.isSuccessful() || response.body() == null) {
          mainHandler.post(() -> {
            if (progressBar != null) {
              progressBar.setVisibility(View.GONE);
            }
          });
          return;
        }

        long totalBytes = response.body().contentLength();
        InputStream inputStream = response.body().byteStream();
        String contentType = response.header("Content-Type", "");
        String extension = getExtensionFromMimeType(contentType);
        if (extension.isEmpty()) {
          extension = ".jpg";
        }

        File targetFile = new File(where2store, "avatar_" + Math.abs(inputurl.hashCode()) + extension);

        try (OutputStream outputStream = new FileOutputStream(targetFile)) {
          byte[] buffer = new byte[4096];
          long downloadedBytes = 0;
          int bytesRead;

          while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            downloadedBytes += bytesRead;
            if (totalBytes > 0 && progressBar != null) {
              int progress = (int) ((downloadedBytes * 100) / totalBytes);
              mainHandler.post(() -> progressBar.setProgress(progress));
            }
          }
          outputStream.flush();

          cached_file_path = targetFile.getAbsolutePath();
          Bitmap bitmap = BitmapFactory.decodeFile(cached_file_path);

          mainHandler.post(() -> {
            if (bitmap != null && imageView != null) {
              imageView.setImageBitmap(bitmap);
            } else if (imageView != null) {
              imageView.setImageURI(Uri.fromFile(targetFile));
            }
            if (progressBar != null) {
              progressBar.setVisibility(View.GONE);
            }
          });
        } catch (Exception e) {
          e.printStackTrace();
          mainHandler.post(() -> {
            if (progressBar != null) {
              progressBar.setVisibility(View.GONE);
            }
          });
        }
      }
    });
  }

  private static String getExtensionFromMimeType(String mimeType) {
    Map<String, String> mimeMap = new HashMap<>();
    mimeMap.put("image/jpeg", ".jpg");
    mimeMap.put("image/png", ".png");
    mimeMap.put("image/webp", ".webp");
    mimeMap.put("application/json", ".json");
    return mimeMap.getOrDefault(mimeType, "");
  }
}
