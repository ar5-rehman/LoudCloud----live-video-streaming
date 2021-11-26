package com.tech.loudcloud.UserPackage.ArchivedVideosService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WebServices {
    String BASE_URL="http://3.236.184.191:5080/LiveApp/rest/";
    Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();


    @GET("v2/vods/count")
    Call<CountVideosPojo> getCount();

    @GET("v2/vods/list/{offset}/{size}")
    Call<List<VideosPojo>> getVideosList(@Path("offset") Integer offset,
                                         @Path("size") Integer size);

}