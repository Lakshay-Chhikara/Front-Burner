package com.apprevelations.frontburner;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class XmlAdapter extends BaseAdapter {

    private final List<XmlItem> items;
    private final Context context;

    public XmlAdapter(Context context, List<XmlItem> items) {
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
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.itemTitle.setText(items.get(position).getTitle());
        holder.itemHash.setText(items.get(position).getHash());
        return convertView;
    }

    static class ViewHolder {
        TextView itemTitle, itemHash;
    }
}
