package com.gcxia.googlemap;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Simon
 * @date 16/8/28
 * @description
 * @Version
 */
public class GeoActivity extends FragmentActivity implements InputtipsListener, AMap.OnCameraChangeListener, PoiSearch.OnPoiSearchListener, AMapLocationListener, AdapterView.OnItemClickListener, RadioGroup.OnCheckedChangeListener {

    private EditText editText;
    private ListView lv_content;
    private List<Tip> tipList = new ArrayList<>();
    private MyAdapter myAdapter;
    private AMap aMap;
    private boolean flag = false;
    private boolean first = true;
    private MapView mapView;

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    private ImageView ivImage;
    private LatLng MyLatLng;
    private Marker addMarker;
    private PoiSearch.Query query;
    private PoiResult poiResult;
    private RadioGroup rgRadioGroup;
    private RadioButton rbAll;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        initView();
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();
        aMap.setOnCameraChangeListener(this);

        initMyLocation();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    flag = true;
                    tipList.clear();
                    lv_content.setVisibility(View.GONE);
                    if (myAdapter != null)
                        myAdapter.notifyDataSetChanged();
                } else {
                    flag = false;
                    lv_content.setVisibility(View.VISIBLE);
                }
                queryKeyWord();
            }
        });

        rbAll.setChecked(true);
        rgRadioGroup.setOnCheckedChangeListener(this);

        lv_content.setOnItemClickListener(this);
    }

    private void initMyLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //启动定位
        mLocationClient.startLocation();
    }

    private void initView() {
        mapView = (MapView) findViewById(R.id.map);
        editText = (EditText) findViewById(R.id.et_keyword);
        lv_content = (ListView) findViewById(R.id.lv_content);
        ivImage = (ImageView) findViewById(R.id.iv_image);
        rgRadioGroup = (RadioGroup) findViewById(R.id.rg_radiogroup);
        rbAll = (RadioButton) findViewById(R.id.rb_all);
    }

    public void queryKeyWord() {
        //第二个参数默认代表全国，也可以为城市区号
        InputtipsQuery inputquery = new InputtipsQuery(editText.getText().toString(), null);
        inputquery.setCityLimit(true);


        Inputtips inputTips = new Inputtips(this, inputquery);

        inputTips.setInputtipsListener(this);
        inputTips.requestInputtipsAsyn();
    }


    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        if (!flag) {
            tipList.clear();
            tipList.addAll(list);

            if (myAdapter == null) {
                myAdapter = new MyAdapter();
                lv_content.setAdapter(myAdapter);
            } else {
                myAdapter.notifyDataSetChanged();
            }
        }
        Log.e("xgc", list.toString());

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
    }

    @Override
    public void onCameraChangeFinish(final CameraPosition cameraPosition) {
        Log.e("xgc CameraChangeFinish", cameraPosition.toString());
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.center_location_anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                query = new PoiSearch.Query("", "住宿服务|餐饮服务", "");
                //keyWord表示搜索字符串，
                //第二个参数表示POI搜索类型，二者选填其一，
                //POI搜索类型共分为以下20种：汽车服务|汽车销售|
                //汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|
                //住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|
                //金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施
                //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
                query.setPageSize(10);// 设置每页最多返回多少条poiitem
                query.setPageNum(0);//设置查询页码

                PoiSearch poiSearch = new PoiSearch(GeoActivity.this, query);
                poiSearch.setOnPoiSearchListener(GeoActivity.this);

                poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(cameraPosition.target.latitude,
                        cameraPosition.target.longitude), 1000));
                poiSearch.searchPOIAsyn();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ivImage.startAnimation(animation);

    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        if (rCode == 1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        aMap.clear();
                        addMarker = aMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),
                                        R.drawable.icon_flight_book_stop))).
                                position(MyLatLng).
                                snippet("DefaultMarker"));
                        MyPoiOverlay poiOverlay = new MyPoiOverlay(getApplicationContext(), aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
//                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                    } else {
                        Log.e("xgc", "NoResult");
                    }
                }
            } else {
                Log.e("xgc", "NoResult");
            }
        } else {
            Log.e("xgc", rCode + "");
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //解析定位结果
                Log.e("xgc", amapLocation.toString());
                if (MyLatLng != null && MyLatLng.latitude == amapLocation.getLatitude() && MyLatLng.longitude == amapLocation.getLongitude()) {
                    return;
                }
                if (addMarker != null) {
                    addMarker.remove();
                }
                MyLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                if (first) {
                    aMap.moveCamera(CameraUpdateFactory.newLatLng(MyLatLng));
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    first = false;
                }
                addMarker = aMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),
                                R.drawable.icon_flight_book_stop))).
                        position(MyLatLng).
                        snippet("DefaultMarker"));
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Tip tip = tipList.get(i);
        LatLng lng = new LatLng(tip.getPoint().getLatitude(), tip.getPoint().getLongitude());
        aMap.moveCamera(CameraUpdateFactory.newLatLng(lng));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stopLocation();
        mLocationClient.onDestroy();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return tipList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(GeoActivity.this, R.layout.item_search_result, null);
                viewHolder.text1 = (TextView) convertView.findViewById(R.id.text1);
                viewHolder.text2 = (TextView) convertView.findViewById(R.id.text2);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            viewHolder.text1.setText(tipList.get(position).getName());
            if (tipList.get(position).getPoint() != null) {
                viewHolder.text2.setText("Latitude : " + tipList.get(position).getPoint().getLatitude() + "-----Longitude : " + tipList.get(position).getPoint().getLongitude());
            } else {
                viewHolder.text2.setText("没有坐标");
            }
            return convertView;
        }
    }

    class ViewHolder {
        TextView text1;
        TextView text2;
    }
}
