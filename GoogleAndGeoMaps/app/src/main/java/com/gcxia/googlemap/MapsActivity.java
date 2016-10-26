package com.gcxia.googlemap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.gcxia.googlemap.api.Key;
import com.gcxia.googlemap.domain.NearbyPoi;
import com.gcxia.googlemap.domain.NearbyResponse;
import com.gcxia.googlemap.network.NearbyPoiSearchModule;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnCameraChangeListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, TextWatcher,
        ResultCallback<AutocompletePredictionBuffer>, AdapterView.OnItemClickListener, RadioGroup.OnCheckedChangeListener, GoogleMap.OnMarkerClickListener, AMapLocationListener {

    private GoogleMap mMap;
    private EditText et_keyword;
    private ImageView ivCenterImage;
    private RadioGroup rgRadioGroup;
    private RadioButton rbAll;
    private ListView lvResult;
    private ListView lvKeyworks;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<AutocompletePrediction> autocompleteList = new ArrayList<>();
    private MyAutoCompleteAdapter myAdapter;
    private boolean flag = false;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(31.223228056886292, 121.34625054895876), new LatLng(31.513228056886292, 121.82625054895876));
    private NearbyPoiSearchModule nearbyPoiSearchModule;
    private ArrayList<NearbyPoi> poiList = new ArrayList<>();
    private MyNearbySearchAdapter myNearbySearchAdapter;
    private Spinner spinner;
    private String language;
    private CameraPosition currentCameraCenter;

    //高德地图
    public AMapLocationClient mLocationClient = null;
    private AMapLocation myCurrentLocation;
    private boolean isFirst = true;
    private MarkerOptions myLocationMarker;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            sendEmptyMessageAtTime(0, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        initView();
        initMyLocation();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void initMyLocation() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        //高德初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //启动定位
        mLocationClient.startLocation();
    }

    private void initView() {
        et_keyword = (EditText) findViewById(R.id.et_keyword);
        et_keyword.addTextChangedListener(this);

        ivCenterImage = (ImageView) findViewById(R.id.iv_image);
        rgRadioGroup = (RadioGroup) findViewById(R.id.rg_radiogroup);
        rgRadioGroup.setOnCheckedChangeListener(this);
        rbAll = (RadioButton) findViewById(R.id.rb_all);
        rbAll.setChecked(true);
        lvResult = (ListView) findViewById(R.id.lv_result);
        lvKeyworks = (ListView) findViewById(R.id.lv_keyworks);
        lvKeyworks.setOnItemClickListener(this);

        spinner = (Spinner) findViewById(R.id.spinner1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {

                String[] languages = getResources().getStringArray(R.array.languages);
                language = languages[pos];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        nearbyPoiSearchModule = new NearbyPoiSearchModule();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(this);

    }

    public void goToActivity(View v) {
        startActivity(new Intent(this, GeoActivity.class));
    }

    public void clearText(View v) {
        et_keyword.setText("");
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.e("xgc", "latitude : " + latLng.latitude + "longitude : " + latLng.longitude);
    }

    @Override
    public void onCameraChange(final CameraPosition cameraPosition) {
        Log.e("xgc", "onCameraChange : " + cameraPosition);
        if (currentCameraCenter != null && currentCameraCenter.target.latitude == cameraPosition.target.latitude && currentCameraCenter.target.longitude == cameraPosition.target.longitude) {
            return;
        }
        currentCameraCenter = cameraPosition;
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.center_location_anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                getPoiSearchFromNet(cameraPosition.target.latitude, cameraPosition.target.longitude, Key.NEARBYALL);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ivCenterImage.startAnimation(animation);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length() <= 0) {
            flag = true;
            lvKeyworks.setVisibility(View.GONE);
            return;
        } else {
            flag = false;
        }
        getAutocomplete(charSequence);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
//        Google的定位
//        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        getPoiSearchFromNet(lastLocation.getLatitude(), lastLocation.getLongitude(), Key.NEARBYALL);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
//        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
    }

    private void getPoiSearchFromNet(double latitude, double longitude, final String types) {
//        "lodging|food|school"
        nearbyPoiSearchModule.getNearbySearchResult(latitude + "," + longitude, "500", types, language, new Callback<NearbyResponse>() {
            @Override
            public void onResponse(Call<NearbyResponse> call, Response<NearbyResponse> response) {
                NearbyResponse nearbyResult = response.body();
                poiList.clear();
                poiList.addAll(nearbyResult.results);
                mMap.clear();
                if (myCurrentLocation != null) {
                    myLocationMarker = new MarkerOptions().position(new LatLng(myCurrentLocation.getLatitude(), myCurrentLocation.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_flight_book_stop));
                    mMap.addMarker(myLocationMarker);
                }
                for (NearbyPoi poi : poiList) {
                    Log.e("xgc", poi.name);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(poi.geometry.location.lat, poi.geometry.location.lng)).title(poi.name).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_hotel)));
                }

                if (myNearbySearchAdapter == null) {
                    myNearbySearchAdapter = new MyNearbySearchAdapter(getApplicationContext(), poiList);
                }
                lvResult.setAdapter(myNearbySearchAdapter);
            }

            @Override
            public void onFailure(Call<NearbyResponse> call, Throwable t) {
                Log.e("xgc", "失败");
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void getAutocomplete(CharSequence constraint) {
        if (mGoogleApiClient.isConnected()) {
            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                                    BOUNDS_GREATER_SYDNEY, null);

            results.setResultCallback(this);

        }
    }

    @Override
    public void onResult(@NonNull AutocompletePredictionBuffer autocompletePredictions) {
        final Status status = autocompletePredictions.getStatus();
        autocompleteList.clear();
        if (!status.isSuccess() || flag) {
            lvKeyworks.setVisibility(View.GONE);
            autocompletePredictions.release();
            return;
        }

        lvKeyworks.setVisibility(View.VISIBLE);

        autocompleteList.addAll(DataBufferUtils.freezeAndClose(autocompletePredictions));
        for (AutocompletePrediction prediction : autocompleteList) {
            Log.e("xgc", prediction.getPlaceId());
        }

        if (myAdapter == null) {
            myAdapter = new MyAutoCompleteAdapter(getApplicationContext(), autocompleteList);
        }
        lvKeyworks.setAdapter(myAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                .getPlaceById(mGoogleApiClient, autocompleteList.get(i).getPlaceId());
        placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }
            final Place place = places.get(0);
            et_keyword.setText("");
            mMap.addMarker(new MarkerOptions().position(place.getLatLng()).draggable(true));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
            places.release();
        }
    };

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        String type = null;
        switch (i) {
            case R.id.rb_all:
                type = Key.NEARBYALL;
                break;
            case R.id.rb_hotel:
                type = Key.NEARBYLODGING;
                break;
            case R.id.rb_restaurant:
                type = Key.NEARBYFOOD;
                break;
            case R.id.rb_company:
                type = Key.NEARBYSCHOOL;
                break;
        }

        if (currentCameraCenter != null) {
            getPoiSearchFromNet(currentCameraCenter.target.latitude, currentCameraCenter.target.longitude, type);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        Log.e("xgc-gaode", aMapLocation.getLatitude() + "*******" + aMapLocation.getLongitude());
//        高德的定位
        if (isFirst) {
            isFirst = false;
            getPoiSearchFromNet(aMapLocation.getLatitude(), aMapLocation.getLongitude(), Key.NEARBYALL);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
            myLocationMarker = new MarkerOptions().position(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_flight_book_stop));
            mMap.addMarker(myLocationMarker);
        }

        if (myCurrentLocation != null && aMapLocation.getLatitude() == myCurrentLocation.getLatitude() && aMapLocation.getLongitude() == myCurrentLocation.getLongitude()) {
            return;
        }
        myCurrentLocation = aMapLocation;
        myLocationMarker.position(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_flight_book_stop));
    }
}