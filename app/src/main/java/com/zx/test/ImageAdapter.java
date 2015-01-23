package com.zx.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Abner on 15/1/21.
 * QQ 230877476
 * Email nimengbo@gmail.com
 */
public class ImageAdapter extends BaseAdapter {

    private List<Integer> items;
    private Context context;
    private LayoutInflater layoutInflater;

    public ImageAdapter(Context context, List<Integer> item) {
        items = item;
        this.context = context;
        layoutInflater = LayoutInflater.from(this.context);
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    class ViewHolder {
        CircleImageView imageView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.hlist_item, null);
            holder.imageView = (CircleImageView) convertView.findViewById(R.id.cricle_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.imageView.setImageResource(items.get(position));
        return convertView;
    }
}
