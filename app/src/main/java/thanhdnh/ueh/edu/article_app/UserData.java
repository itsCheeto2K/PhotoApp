package thanhdnh.ueh.edu.article_app;

import android.app.Activity;
import android.content.Context;
import android.widget.GridView;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserData {
  public static UserList data;
  private Context context;
  private GridView gridview;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public UserData(Context context, GridView gridview) {
    this.context = context;
    this.gridview = gridview;
  }

  public static UserProfile getUserFromId(int id) {
    if (data == null || data.getUsers() == null) {
      return null;
    }
    for (int i = 0; i < data.getUsers().size(); i++) {
      if (data.getUsers().get(i).getId() == id) {
        return data.getUsers().get(i);
      }
    }
    return null;
  }

  public void loadData(String url, Activity activity) {
    executor.execute(() -> {
      Gson gson = new Gson();
      UserList loadedData = null;

      // Try downloading from URL if provided
      if (url != null && !url.isEmpty()) {
        File file = Downloader.downloadFile(url, context.getCacheDir());
        if (file != null) {
          try {
            String jsonContent = readTextFromFile(file);
            loadedData = gson.fromJson(jsonContent, (Type) UserList.class);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

      // Fallback to local asset if remote download failed or empty
      if (loadedData == null || loadedData.getUsers() == null || loadedData.getUsers().isEmpty()) {
        try {
          InputStream is = context.getAssets().open("users.json");
          String jsonContent = readTextFromStream(is);
          loadedData = gson.fromJson(jsonContent, (Type) UserList.class);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      final UserList finalData = loadedData;
      activity.runOnUiThread(() -> {
        data = finalData;
        if (data != null && data.getUsers() != null) {
          UserAdapter adapter = new UserAdapter(data.getUsers(), context);
          gridview.setAdapter(adapter);
        }
      });
    });
  }

  public String readTextFromFile(File file) {
    try (InputStream stream = new FileInputStream(file)) {
      return readTextFromStream(stream);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public String readTextFromStream(InputStream stream) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
      StringBuilder buffer = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        buffer.append(line).append("\n");
      }
      return buffer.toString();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }
}
