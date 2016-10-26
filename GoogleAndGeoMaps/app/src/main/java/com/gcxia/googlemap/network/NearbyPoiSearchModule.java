package com.gcxia.googlemap.network;

import com.gcxia.googlemap.api.Key;
import com.gcxia.googlemap.api.NearbySearch;
import com.gcxia.googlemap.domain.NearbyResponse;
import com.gcxia.googlemap.util.RetrofitUtil;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author gcxia
 * @date 2016/8/31
 * @description
 */
public class NearbyPoiSearchModule {
    public void getNearbySearchResult(String location, String radius, String types, String language,Callback<NearbyResponse> callback) {
        NearbySearch api = RetrofitUtil.createApi(NearbySearch.class);
        Call<NearbyResponse> nearbySearch = api.getNearbySearch(location, radius, types, Key.APIKEY,language);
        nearbySearch.enqueue(callback);
    }
}
