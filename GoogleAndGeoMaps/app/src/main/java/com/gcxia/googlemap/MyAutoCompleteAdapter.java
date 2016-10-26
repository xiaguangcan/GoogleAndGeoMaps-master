package com.gcxia.googlemap;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompletePrediction;

import java.util.ArrayList;

/**
 * @author gcxia
 * @date 2016/8/30
 * @description
 */
public class MyAutoCompleteAdapter extends BaseAdapter {
    private ArrayList<AutocompletePrediction> list;
    private Context context;
    private final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    public MyAutoCompleteAdapter(Context context, ArrayList<AutocompletePrediction> list) {
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


        viewHolder.text1.setText(list.get(position).getPrimaryText(STYLE_BOLD));
        viewHolder.text2.setText(list.get(position).getSecondaryText(STYLE_BOLD));
        return convertView;
    }

    class ViewHolder {
        TextView text1;
        TextView text2;
    }
}
