package com.apprevelations.frontburner;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomQuesAdapter extends BaseAdapter {

    private final List<Item> items;
    private final Context context;

    public CustomQuesAdapter(Context context, List<Item> items) {
        this.items = items;
        this.context = context;
    }

    public int getCount() {
        return items.size();
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int id) {
        return id;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.xml_item, null);
            holder = new ViewHolder();
            holder.itemTitle = (TextView) convertView.findViewById(R.id.itemTitle);
            holder.itemHash = (TextView) convertView.findViewById(R.id.itemHash);
            holder.itemDate = (TextView) convertView.findViewById(R.id.itemDate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.itemTitle.setText(items.get(position).getName());
        holder.itemHash.setText((String) (items.get(position).getObject("category")));
        //holder.itemDate.setText(items.get(position).);
        return convertView;
    }

    static class ViewHolder {
        TextView itemTitle, itemHash, itemDate;
    }
}
