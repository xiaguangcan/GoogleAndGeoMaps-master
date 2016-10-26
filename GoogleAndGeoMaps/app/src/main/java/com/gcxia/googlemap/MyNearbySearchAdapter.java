package com.gcxia.googlemap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gcxia.googlemap.domain.NearbyPoi;

import java.util.ArrayList;

/**
 * @author gcxia
 * @date 2016/8/31
 * @description
 */
public class MyNearbySearchAdapter extends BaseAdapter {

    private ArrayList<NearbyPoi> list;
    private Context context;

    public MyNearbySearchAdapter(Context context, ArrayList<NearbyPoi> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(context, R.layout.item_search_result, null);
            viewHolder.text1 = (TextView) convertView.findViewById(R.id.text1);
            viewHolder.text2 = (TextView) convertView.findViewById(R.id.text2);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.text1.setText(list.get(position).name);
        viewHolder.text2.setText(list.get(position).vicinity);
        return convertView;
    }

    class ViewHolder {
        TextView text1;
        TextView text2;
    }
}
