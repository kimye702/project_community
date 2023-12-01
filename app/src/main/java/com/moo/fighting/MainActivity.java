package com.moo.fighting;

import static java.lang.Math.pow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.moo.fighting.Calendar.MainActivity2;
import com.moo.fighting.Walk.Walk;
import com.moo.fighting.weather.model.ShortWeatherModel;
import com.moo.fighting.weather.model.VeryShortWeatherModel;
import com.moo.fighting.weather.util.Conversion;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1981;
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 2981;
    private static final String[] PERMISSIONS = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;

    private TextView tv_today_weather;
    private TextView tv_now_temp;
    private TextView tv_highest_temp;
    private TextView tv_lowest_temp;
    private TextView tv_perceived_temp;
    private Point curPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        checkLocation();
        requestLocation();

        tv_today_weather = findViewById(R.id.tv_today_weather);
        tv_now_temp = findViewById(R.id.tv_now_temp);
        tv_highest_temp = findViewById(R.id.tv_highest_temp);
        tv_lowest_temp = findViewById(R.id.tv_lowest_temp);
        tv_perceived_temp = findViewById(R.id.tv_perceived_temp);

        // 현재 시간을 가져옵니다.
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        ImageView community = (ImageView) findViewById(R.id.main_button1);
        ImageView calendar = (ImageView) findViewById(R.id.main_button2);
        ImageView map = (ImageView) findViewById(R.id.main_button3);
        ImageView profile = (ImageView) findViewById(R.id.main_button4);

        community.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), y_communityMain.class);
                startActivity(intent);
            }

        });

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                startActivity(intent);
            }
        });

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Walk.class);
                startActivity(intent);
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), profileactivity.class);
                startActivity(intent);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setWeather(String nx, String ny) {
        // 현재 날짜와 시간
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDate currentDate = currentDateTime.toLocalDate();

        // 날짜 형식 지정
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault());
        String base_date = currentDate.format(dateFormatter);

        // 시간 형식 지정
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH", Locale.getDefault());
        String timeH = currentDateTime.format(timeFormatter);
        String timeM = currentDateTime.format(timeFormatter);
        String base_time = getBaseTime(timeH, timeM);

        if ("00".equals(timeH) && "2330".equals(base_time)) {
            currentDate = currentDate.minusDays(1);
            base_date = currentDate.format(dateFormatter);
        }

        String base_date2 = currentDate.format(dateFormatter);
        String base_time2 = getBaseTime2();

        if (("00".equals(timeH) || "01".equals(timeH) || "02".equals(timeH)) && "2300".equals(base_time2)) {
            currentDate = currentDate.minusDays(1);
            base_date2 = currentDate.format(dateFormatter);
        }

        Call<WEATHER> very_short_call = ApiObject.retrofitService.GetVeryShortTermWeather(60, 1, "JSON", base_date, base_time, nx, ny);
        Call<WEATHER> short_call = ApiObject.retrofitService.GetShortTermWeather(1000, 1, "JSON", base_date2, base_time2, nx, ny);

        System.out.println("very short base date : " + base_date + "base time : " + base_time);
        System.out.println("short base date : " + base_date2 + "base time2 : " + base_time2);

        // 초단기 예보 정보 받아오기
        very_short_call.enqueue(new retrofit2.Callback<WEATHER>() {
            @Override
            public void onResponse(@NonNull Call<WEATHER> call, @NonNull Response<WEATHER> response) {
                if (response.isSuccessful()) {
                    List<ITEM> items = Objects.requireNonNull(response.body()).response.body.items.item;

                    VeryShortWeatherModel[] weatherArr = new VeryShortWeatherModel[6];

                    for (int i = 0; i < 6; i++) {
                        weatherArr[i] = new VeryShortWeatherModel();
                    }

                    int index = 0;
                    int totalCount = response.body().response.body.totalCount - 1;
//                    System.out.println("초단기예보 totalCount : "+totalCount);
                    for (int i = 0; i <= totalCount; i++) {
                        index %= 6;
                        switch (items.get(i).category) {
                            case "PTY": // 강수형태
                                weatherArr[index].setRainType(items.get(i).fcstValue);
                                break;
                            case "REH": // 습도
                                weatherArr[index].setHumidity(items.get(i).fcstValue);
                                break;
                            case "SKY": // 하늘상태
                                weatherArr[index].setSky(items.get(i).fcstValue);
                                break;
                            case "T1H": // 기온
                                weatherArr[index].setTemp(items.get(i).fcstValue);
                                break;
                            case "RN1": // 1시간 강수량
                                weatherArr[index].setHourRain(items.get(i).fcstValue);
                                break;
                            case "WSD": // 풍속
                                weatherArr[index].setWindSpeed(items.get(i).fcstValue);
                                break;
                            default:
                                break;
                        }
                        index++;
                    }

                    for (int i = 0; i <= 5; i++) {
                        String fcstTime = items.get(i).fcstTime;
                        String formattedTime = fcstTime.substring(0, 2) + ":" + fcstTime.substring(2, 4);
                        weatherArr[i].setFcstTime(formattedTime);
                    }

                    String sky = weatherArr[0].getSky();
                    String weather;
                    switch (sky) {
                        case "1":
                            weather = "맑음";
                            break;
                        case "3":
                            weather = "구름 많음";
                            break;
                        case "4":
                            weather = "흐림";
                            break;
                        default:
                            weather = "오류 rainType : " + sky;
                            break;
                    }

                    tv_today_weather.setText(weather);
                    tv_now_temp.setText(weatherArr[0].getTemp());

                    double T = Double.parseDouble(weatherArr[0].getTemp());
                    double V = Double.parseDouble(weatherArr[0].getWindSpeed());
                    double rst = 13.12 + 0.6251 * T - 11.37 * pow(V, 0.16) + 0.3965 * pow(V, 0.16) * T;

                    // 체감온도를 소수점 첫째 자리까지 형식화
                    String perceived_temp = String.format("%.1f", rst);
                    tv_perceived_temp.setText(perceived_temp + "°");


                    Toast.makeText(getApplicationContext(), "초단기 예보 로드 성공", Toast.LENGTH_SHORT).show();
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<WEATHER> call, @NonNull Throwable t) {
                Log.e("api fail", Objects.requireNonNull(t.getMessage()));
            }
        });

        // 단기예보 api 정보 받아오기
        short_call.enqueue(new retrofit2.Callback<WEATHER>() {
            @Override
            public void onResponse(@NonNull Call<WEATHER> call, @NonNull Response<WEATHER> response) {
                if (response.isSuccessful()) {
                    WEATHER weather = response.body();
                    System.out.println("response : " + response);
                    if (weather != null) {
                        List<ITEM> items = Objects.requireNonNull(response.body()).response.body.items.item;

                        int now_hour = Integer.parseInt(timeH);
                        int base_hour = Integer.parseInt(base_time2.substring(0, 2));
                        int diff = now_hour - base_hour;

                        if (diff < 0)
                            diff = 0;

                        System.out.println("now_hour : " + now_hour + " base_hour : " + base_hour + " diff : " + diff);

                        int cnt = 0;
                        int index = 0;
                        int totalCount = response.body().response.body.totalCount - 1;

                        int num = totalCount / 12 - diff;

                        ShortWeatherModel[] weatherArr = new ShortWeatherModel[num];

                        for (int i = 0; i < num; i++) {
                            weatherArr[i] = new ShortWeatherModel();
                        }

//                    System.out.println("단기예보 totalCount : " + totalCount);
                        for (int i = diff; i <= totalCount; i++) {
                            switch (items.get(i).category) {
                                case "POP": // 강수확률
                                    weatherArr[index].setRainPercent(items.get(i).fcstValue);
                                    break;
                                case "PTY": // 강수형태
                                    weatherArr[index].setRainType(items.get(i).fcstValue);
                                    break;
                                case "PCP": // 1시간 강수량
                                    weatherArr[index].setHourRain(items.get(i).fcstValue);
                                    break;
                                case "REH": // 습도
                                    weatherArr[index].setHumidity(items.get(i).fcstValue);
                                    break;
                                case "SKY": // 하늘상태
                                    weatherArr[index].setSky(items.get(i).fcstValue);
                                    break;
                                case "TMP": // 1시간 기온
                                    weatherArr[index].setHourTemp(items.get(i).fcstValue);
                                    break;
                                case "TMN": // 일 최저기온
                                    weatherArr[index].setLowTemp(items.get(i).fcstValue);
                                    cnt--;
                                    break;
                                case "TMX": // 일 최고기온
                                    weatherArr[index].setHighTemp(items.get(i).fcstValue);
                                    cnt--;
                                    break;
                                case "WSD": // 풍속
                                    weatherArr[index].setWindSpeed(items.get(i).fcstValue);
                                    break;
                                default:
                                    break;
                            }
                            cnt++;

                            if (cnt % 12 == 0 && !items.get(i).category.equals("TMX") && !items.get(i).category.equals("TMN")) {
//                            System.out.println("fcstTime : "+items.get(i).fcstTime+"category : "+ items.get(i).category);
                                String fcstTime = items.get(i).fcstTime;
                                String formattedTime = fcstTime.substring(0, 2) + ":" + fcstTime.substring(2, 4);
                                weatherArr[index].setFcstTime(formattedTime);
                                index++;
                            }
                        }

                        String low_temp = null, high_temp = null;

                        for (int i = 0; i < num; i++) {
                            if (weatherArr[i].getLowTemp() == null)
                                continue;
                            else {
                                low_temp = weatherArr[i].getLowTemp();
                            }
                        }

                        for (int i = 0; i < num; i++) {
                            if (weatherArr[i].getHighTemp() == null)
                                continue;
                            else {
                                high_temp = weatherArr[i].getHighTemp();
                            }
                        }

                        if (low_temp != null)
                            tv_lowest_temp.setText(low_temp + "°");
                        else
                            tv_lowest_temp.setText("error");

                        if (high_temp != null)
                            tv_highest_temp.setText(high_temp + "°");
                        else
                            tv_highest_temp.setText("error");
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<WEATHER> call, @NonNull Throwable t) {
                Log.e("api fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"MissingPermission", "SetTextI18n"})
    private void requestLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        curPoint = Conversion.dfs_xy_conv(location.getLatitude(), location.getLongitude());
                        setWeather(String.valueOf(curPoint.x), String.valueOf(curPoint.y));
                        List<Address> addressList = getAddress(location.getLatitude(), location.getLongitude());
                    }
                })
                .addOnFailureListener(e -> Log.e("requestLocation", "fail"));
    }

    private List<Address> getAddress(double lat, double lng) {
        List<Address> address = null;

        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.KOREA);
            address = geocoder.getFromLocation(lat, lng, 10);
        } catch (IOException e) {
            Toast.makeText(this, "주소를 가져 올 수 없습니다", Toast.LENGTH_SHORT).show();
        }

        return address;
    }

    private String getBaseTime(String h, String m) {
        String result;

        if (Integer.parseInt(m) < 45) {
            if (h.equals("00")) {
                result = "2330";
            } else {
                int resultH = Integer.parseInt(h) - 1;
                if (resultH < 10) {
                    result = "0" + resultH + "30";
                } else {
                    result = resultH + "30";
                }
            }
        } else {
            result = h + "30";
        }

        return result;
    }

        private String getBaseTime2() {
        int hour = Integer.parseInt(new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis())));
        int minute = Integer.parseInt(new SimpleDateFormat("mm").format(new Date(System.currentTimeMillis())));

        int baseHour = (hour / 3) * 3 + 2;

        if (hour % 3 == 2 && minute >= 10) {
            baseHour = (baseHour + 3) % 24;
        }

        return String.format("%02d00", baseHour);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Toast.makeText(MainActivity.this, "Result OK", Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(MainActivity.this, "Result Cancel", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    private void init() {
        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20 * 1000);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void checkLocation() {
        if (isPermissionGranted()) {
            startLocationUpdates();
        } else {
            requestPermissions();
        }
    }

    private boolean isPermissionGranted() {
        for (String permission : PERMISSIONS) {
            if (permission.equals(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                continue;
            }
            final int result = ContextCompat.checkSelfPermission(this, permission);

            if (PackageManager.PERMISSION_GRANTED != result) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    resolveLocationSettings(e);
                } else {
                }
            }
        });
    }

    public void resolveLocationSettings(Exception exception) {
        ResolvableApiException resolvable = (ResolvableApiException) exception;
        try {
            resolvable.startResolutionForResult(this, REQUEST_CODE_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                for (String permission : permissions) {
                    if ("android.permission.ACCESS_FINE_LOCATION".equals(permission)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("알림");
                        builder.setMessage("위치 정보 권한이 필요합니다.\n\n[설정]->[권한]에서 '위치' 항목을 사용으로 설정해 주세요.");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(MainActivity.this, "Cancel Click", Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                }
            }
        }
    }
}