package edu.rose_hulman.quickpay;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.uepaa.p2pkit.discovery.Peer;

/**
 * Created by Zane on 9/27/2015.
 */
public class UserAdapter extends BaseAdapter {
    private final Activity activity;
    private List<User> users = new ArrayList<User>();

    public UserAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public User getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);

            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(android.R.id.text1);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User user = getItem(position);
        holder.textView.setText(user.name);
        return convertView;
    }

    private class ViewHolder {
        private TextView textView;
    }

    public void put(User user) {
        if(users.contains(user)) {
            users.remove(user);
        }

        users.add(user);
        notifyDataSetChanged();
    }

    public void remove(User user) {
        users.remove(user);
        notifyDataSetChanged();
    }
}
