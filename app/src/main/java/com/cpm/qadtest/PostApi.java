package com.cpm.qadtest;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


/**
 * Created by jeevanp on 19-05-2017.
 */


//using interface for post data
public interface PostApi {
    @retrofit2.http.POST("DownloadAll")
    retrofit2.Call<ResponseBody> downloadAllQuestionData(@retrofit2.http.Body RequestBody questionjsonData);

    @retrofit2.http.POST("UploadJsonDetail")
    retrofit2.Call<ResponseBody> getUploadJsonDetail(@retrofit2.http.Body okhttp3.RequestBody request);
}

