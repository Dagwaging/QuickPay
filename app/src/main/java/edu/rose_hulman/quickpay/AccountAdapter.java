package edu.rose_hulman.quickpay;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

import me.figo.models.Account;

/**
 * Created by Zane on 9/27/2015.
 */
public class AccountAdapter extends BaseAdapter {
    private final Activity activity;
    private List<Account> accounts = new ArrayList<Account>();

    public AccountAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return accounts.size();
    }

    @Override
    public Account getItem(int position) {
        return accounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.list_item_account, parent, false);

            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.textView);
            holder.imageView = (NetworkImageView) convertView.findViewById(R.id.imageView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Account account = getItem(position);
        holder.textView.setText(account.getName());
        holder.imageView.setImageUrl(account.getIconUrl(), RequestQueueProvider.getImageLoader(activity));

        return convertView;
    }

    public void setItems(List<Account> accounts) {
        this.accounts = accounts;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        private TextView textView;
        private NetworkImageView imageView;
    }
}
