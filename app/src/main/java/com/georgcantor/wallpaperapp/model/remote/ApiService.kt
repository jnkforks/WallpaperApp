package com.georgcantor.wallpaperapp.model.remote

import com.georgcantor.wallpaperapp.BuildConfig
import com.georgcantor.wallpaperapp.model.Pic
import com.georgcantor.wallpaperapp.model.unsplash.UnsplashResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {

    @GET("?key=" + BuildConfig.API_KEY)
    fun getPictures(
        @Query("q") query: String,
        @Query("page") index: Int
    ): Observable<Pic>

    @GET
    fun getUnsplashPictures(@Url url: String,
                            @Query("query") query: String,
                            @Query("page") page: Int): Observable<UnsplashResponse>

}
