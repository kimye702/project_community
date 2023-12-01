package com.moo.fighting;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherInterface {

    // 초단기 예보
    @GET("VilageFcstInfoService_2.0/getUltraSrtFcst?serviceKey="+ BuildConfig.AIR_KOREA_SERVICE_KEY)
    Call<WEATHER> GetVeryShortTermWeather(@Query("numOfRows") int num_of_rows,
                            @Query("pageNo") int page_no,
                            @Query("dataType") String data_type,
                            @Query("base_date") String base_date,
                            @Query("base_time") String base_time,
                            @Query("nx") String nx,
                            @Query("ny") String ny);

    // 단기 예보
    @GET("VilageFcstInfoService_2.0/getVilageFcst?serviceKey="+ BuildConfig.AIR_KOREA_SERVICE_KEY)
    Call<WEATHER> GetShortTermWeather(@Query("numOfRows") int num_of_rows,
                            @Query("pageNo") int page_no,
                            @Query("dataType") String data_type,
                            @Query("base_date") String base_date,
                            @Query("base_time") String base_time,
                            @Query("nx") String nx,
                            @Query("ny") String ny);
}

class WEATHER {
    public RESPONSE response;
}

class RESPONSE {
    public HEADER header;
    public BODY body;
}

class HEADER {
    public int resultCode;
    public String resultMsg;
}

class BODY {
    public String dataType;
    public ITEMS items;
    public int totalCount;
}

class ITEMS {
    public List<ITEM> item;
}

class ITEM {
    String baseDate;
    String baseTime;
    String category;
    String fcstDate;
    String fcstTime;
    String fcstValue;
}

class ApiObject {
    public static WeatherInterface retrofitService = new Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/1360000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherInterface.class);
}
