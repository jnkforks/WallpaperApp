package com.georgcantor.wallpaperapp.network;

import com.georgcantor.wallpaperapp.BuildConfig;
import com.georgcantor.wallpaperapp.model.Pic;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("?key=" + BuildConfig.API_KEY + "&q=mercedes-benz")
    Call<Pic> getPopularPic(@Query("page") int index);

    @GET("?key=" + BuildConfig.API_KEY)
    Call<Pic> getCatPic(@Query("category") String category,
                        @Query("page") int index);

    @GET("?key=" + BuildConfig.API_KEY + "&q=bmw")
    Call<Pic> getLatestPic(@Query("page") int index);

    @GET("?key=" + BuildConfig.API_KEY + "&q=porsche")
    Call<Pic> getEditorPic(@Query("page") int index);

    @GET("?key=" + BuildConfig.API_KEY + "&q=audi")
    Call<Pic> getRedPic(@Query("page") int index);

    @GET("?key=" + BuildConfig.API_KEY + "&q=bugatti")
    Call<Pic> getBluePic(@Query("page") int index);

    @GET("?key=" + BuildConfig.API_KEY + "&q=ferrari")
    Call<Pic> getBlackPic(@Query("page") int index);

    @GET("?key=" + BuildConfig.API_KEY + "&q=lamborghini")
    Call<Pic> getYellowPic(@Query("page") int index);
}
