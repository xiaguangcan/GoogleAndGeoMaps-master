package com.gcxia.googlemap;

import android.content.Context;
import android.graphics.BitmapFactory;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.overlay.PoiOverlay;
import com.amap.api.services.core.PoiItem;

import java.util.List;

/**
 * @author gcxia
 * @date 2016/8/29
 * @description
 */
public class MyPoiOverlay extends PoiOverlay {
    private Context context;

    public MyPoiOverlay(Context context, AMap aMap, List<PoiItem> list) {
        super(aMap, list);
        this.context = context;
    }

    @Override
    protected BitmapDescriptor getBitmapDescriptor(int i) {
        return BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(context.getResources(),
                        R.drawable.map_marker_hotel));
    }
}
