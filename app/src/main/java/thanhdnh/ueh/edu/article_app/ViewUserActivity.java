package thanhdnh.ueh.edu.article_app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ViewUserActivity extends AppCompatActivity {
  private ImageView iv_detail_avatar;
  private TextView tv_detail_username, tv_detail_id, tv_detail_email, tv_detail_hobby, tv_detail_description, tv_download_status;
  private ProgressBar pb_download_progress;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_user);
    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    iv_detail_avatar = findViewById(R.id.iv_detail_avatar);
    tv_detail_username = findViewById(R.id.tv_detail_username);
    tv_detail_id = findViewById(R.id.tv_detail_id);
    tv_detail_email = findViewById(R.id.tv_detail_email);
    tv_detail_hobby = findViewById(R.id.tv_detail_hobby);
    tv_detail_description = findViewById(R.id.tv_detail_description);
    tv_download_status = findViewById(R.id.tv_download_status);
    pb_download_progress = findViewById(R.id.pb_download_progress);

    int id = (int) getIntent().getLongExtra("id", 1);
    if (id == 0) {
      id = getIntent().getIntExtra("id", 1);
    }

    UserProfile user = UserData.getUserFromId(id);
    if (user != null) {
      tv_detail_username.setText(user.getUsername());
      tv_detail_id.setText("Mã ID: " + user.getId());
      tv_detail_email.setText(user.getEmail());
      tv_detail_hobby.setText(user.getHobby());
      tv_detail_description.setText(user.getDesc());

      Handler mainHandler = new Handler(Looper.getMainLooper());
      if (user.getAvatar_url() != null && !user.getAvatar_url().isEmpty()) {
        tv_download_status.setText("Đang tải ảnh đại diện...");
        Downloader.downloadWithProgress(
            user.getAvatar_url(),
            mainHandler,
            this,
            getCacheDir(),
            pb_download_progress,
            iv_detail_avatar
        );
        // Observe completion to hide status text
        mainHandler.postDelayed(() -> {
          if (pb_download_progress.getVisibility() == View.GONE) {
            tv_download_status.setVisibility(View.GONE);
          }
        }, 1500);
      } else {
        pb_download_progress.setVisibility(View.GONE);
        tv_download_status.setVisibility(View.GONE);
      }
    }
  }
}
