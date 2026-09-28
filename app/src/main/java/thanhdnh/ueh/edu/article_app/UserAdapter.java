package thanhdnh.ueh.edu.article_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserAdapter extends BaseAdapter {
  private ArrayList<UserProfile> user_list;
  private Context context;

  public UserAdapter(ArrayList<UserProfile> user_list, Context context) {
    this.user_list = user_list;
    this.context = context;
  }

  @Override
  public int getCount() {
    return user_list != null ? user_list.size() : 0;
  }

  @Override
  public Object getItem(int position) {
    return user_list.get(position);
  }

  @Override
  public long getItemId(int position) {
    return user_list.get(position).getId();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final ViewHolder holder;
    if (convertView == null) {
      LayoutInflater inflater = LayoutInflater.from(context);
      convertView = inflater.inflate(R.layout.user_disp_tpl, parent, false);
      holder = new ViewHolder();
      holder.imv_avatar = convertView.findViewById(R.id.imv_avatar);
      holder.tv_username = convertView.findViewById(R.id.tv_username);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    UserProfile user = user_list.get(position);
    holder.tv_username.setText(user.getUsername());

    if (user.getAvatar_url() != null && !user.getAvatar_url().isEmpty()) {
      Picasso.get()
          .load(user.getAvatar_url())
          .resize(250, 250)
          .centerCrop()
          .into(holder.imv_avatar);
    }

    return convertView;
  }

  private static class ViewHolder {
    ImageView imv_avatar;
    TextView tv_username;
  }
}
