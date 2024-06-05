package com.example.wetherapp;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.*;
import android.view.View;
import android.widget.Button;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.Manifest;

import com.example.wetherapp.models.WeatherModel;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

private TextView latLanTextVeiw , addressTextView , timetextView, tempTextView,humidtyTextView,DescTextView,city_Name;
Button Srch;
private OkHttpClient client;
private Gson gson;
String url;

class getWeather extends AsyncTask<String, Void, String>{
    @Override
    protected String doInBackground(String...urls){
        StringBuilder result = new StringBuilder();
        try{
            URL url = new URL(urls[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line = "";
            while((line = reader.readLine()) != null){
                result.append(line).append("\n");

            }
            return result.toString();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onPostExecute(String result){
        super.onPostExecute(result);
        try{
            JSONObject jsonObject = new JSONObject(result);
            String weatherInfo = jsonObject.getString("main");
            weatherInfo = weatherInfo.replace("temp","Temperature");
            weatherInfo = weatherInfo.replace("feels_like","Feels Like");
            weatherInfo = weatherInfo.replace("temp_max","Temperature Max");
            weatherInfo = weatherInfo.replace("temp_min","Temperature Min");
            weatherInfo = weatherInfo.replace("pressure","Pressure");
            weatherInfo = weatherInfo.replace("humidity","Humidity");
            weatherInfo = weatherInfo.replace("{","");
            weatherInfo = weatherInfo.replace("}","");
            weatherInfo = weatherInfo.replace(",","\n");
            weatherInfo = weatherInfo.replace(":"," : ");
            tempTextView.setText(weatherInfo);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
final  String[] temp= {""};
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        latLanTextVeiw = findViewById(R.id.textView_LL);
        addressTextView = findViewById(R.id.textView_Geo);
        timetextView = findViewById(R.id.textView_Time);
        tempTextView = findViewById(R.id.textView_temp);
        humidtyTextView = findViewById(R.id.textView_humid);
        DescTextView = findViewById(R.id.textView_Desc);
        city_Name = findViewById(R.id.city_name);
        Srch = findViewById(R.id.search);


        Srch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(MainActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();
                String city = city_Name.getText().toString();
                tempTextView.setText("");
                humidtyTextView.setText("");
                DescTextView.setText("");
//                latLanTextVeiw.setText("");
//                addressTextView.setText("");

                try {
                    if(city!=null){
                        url = "https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid=1ce3d946da6f30a875fc2d595184cd4c";
                    }else {
                        Toast.makeText(MainActivity.this, "Enter the city", Toast.LENGTH_SHORT).show();
                    }
                    getWeather task = new getWeather();
                    temp[0] = task.execute(url).get();
                }catch(ExecutionException e ){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                if(temp[0]==null){
                    tempTextView.setText("Cannot able to find weather");
                }

            }
        });


        // Create a runnable to update the time
                runnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000); // Update every second
            }
        };

        // Start the runnable
        handler.post(runnable);

        int fine = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int coarse = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if(fine != PackageManager.PERMISSION_GRANTED){
            String[] ss = new String[1];
            ss[0] = Manifest.permission.ACCESS_FINE_LOCATION;
            requestPermissions(ss ,  999);

        }
        if(fine != PackageManager.PERMISSION_GRANTED){
            String[] ss = new String[1];
            ss[0] = Manifest.permission.ACCESS_COARSE_LOCATION;
            requestPermissions(ss ,  999);

        }

        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        double lat = loc.getLatitude();
        double lng = loc.getLongitude();

        latLanTextVeiw.setText("Lat : "+lat+", "+"Lan : "+lng);
        getAddressFromLatLng(lat,lng);
        fetchWeatherData();


    }
    private void updateTime() {
        // Get the current time
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        // Set the current time to the TextView
        timetextView.setText("Time : "+currentTime);
    }

    private void getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressString = address.getAddressLine(0); // If you need a single line address
                // You can also use address.getLocality(), address.getPostalCode(), etc.
                addressTextView.setText("Geo Address : "+addressString);
            } else {
                addressTextView.setText("Geo Address : No address found for the location");
            }
        } catch (IOException e) {
            e.printStackTrace();
            addressTextView.setText("Geo Address : Unable to get address for the location");
        }
    }

    private void fetchWeatherData() {
        String url = "https://api.openweathermap.org/data/2.5/forecast?id=524901&appid=1ce3d946da6f30a875fc2d595184cd4c";

        client = new OkHttpClient();
        gson = new Gson();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    Log.d("CALL", "onResponse: " + myResponse);
                    final WeatherModel weatherModel = gson.fromJson(myResponse, WeatherModel.class);

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            com.example.wetherapp.models.List item = weatherModel.getList().get(0);
                            tempTextView.setText("Temp : "+item.getMain().getTemp());
                            humidtyTextView.setText("Humidity : "+item.getMain().getHumidity());

                            DescTextView.setText("Descr iption : "+item.getWeather().get(0).getDescription());
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the callbacks to prevent memory leaks
        handler.removeCallbacks(runnable);
    }


}