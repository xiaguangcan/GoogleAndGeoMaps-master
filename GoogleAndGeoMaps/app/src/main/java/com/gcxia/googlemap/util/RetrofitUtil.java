package com.gcxia.googlemap.util;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * @author gcxia
 * @date 2016/8/30
 * @description
 */
public class RetrofitUtil {
    private static Retrofit singleton;

    public static <T> T createApi(Class<T> clazz) {
        if (singleton == null) {
            synchronized (RetrofitUtil.class) {
                if (singleton == null) {
                    singleton = new Retrofit.Builder()
                            .baseUrl("https://maps.googleapis.com/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .build();
                }
            }
        }
        return singleton.create(clazz);
    }

}
