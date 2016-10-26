package com.gcxia.googlemap.api;

import com.gcxia.googlemap.domain.NearbyResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author gcxia
 * @date 2016/8/30
 * @description
 */
public interface NearbySearch {
    @GET("maps/api/place/nearbysearch/json")
    Call<NearbyResponse> getNearbySearch(@Query("location") String location,
                                         @Query("radius") String radius,
                                         @Query("types") String types,
                                         @Query("key") String sensor,
                                         @Query("language") String language);
}
