package com.example.curs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final String OPENWEATHERMAP_API_BASE = "https://api.openweathermap.org/data/2.5/weather?q=";
    private static final String WEATHERAPI_API_BASE = "https://api.weatherapi.com/v1/current.json?key=019f4b8df8bc4f2c9a485826241412&q=";
    private static final String FORECAST_API_BASE = "https://api.openweathermap.org/data/2.5/forecast?q=";

    private TextView weatherApiTemperatureView;
    private TextView weatherApiHumidityView;
    private TextView weatherApiWindSpeedView;
    private TextView weatherApiPressureView;
    private TextView openWeatherTemperatureView;
    private TextView openWeatherHumidityView;
    private TextView openWeatherWindSpeedView;
    private TextView openWeatherPressureView;
    private TextView temperatureView;
    private TextView humidityView;
    private TextView precipitationView;
    private TextView windSpeedView;
    private TextView pressureView;
    private TextView feelsLikeView;
    private TextView cloudinessView;
    private TextView sunriseView;
    private TextView sunsetView;
    private TextView forecastDateView;
    private TextView forecastTemperatureView;
    private TextView forecastHumidityView;
    private TextView forecastWindSpeedView;
    private ImageView weatherIcon;
    private ProgressBar progressBar;
    private EditText cityInput;
    private Button loadWeatherButton;
    private RadioGroup temperatureRadioGroup;

    private List<Forecast> forecastList = new ArrayList<>();
    private ForecastAdapter forecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        loadWeatherButton.setOnClickListener(v -> {
            String city = cityInput.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeatherData(city);
            } else {
                Toast.makeText(MainActivity.this, "Введите название города", Toast.LENGTH_SHORT).show();
            }
        });

        temperatureRadioGroup.setOnCheckedChangeListener((group, checkedId) -> fetchWeatherData(cityInput.getText().toString().trim()));
    }

    private void initializeViews() {
        weatherApiTemperatureView = findViewById(R.id.weatherApiTemperatureView);
        weatherApiHumidityView = findViewById(R.id.weatherApiHumidityView);
        weatherApiWindSpeedView = findViewById(R.id.weatherApiWindSpeedView);
        weatherApiPressureView = findViewById(R.id.weatherApiPressureView);
        openWeatherTemperatureView = findViewById(R.id.openWeatherTemperatureView);
        openWeatherHumidityView = findViewById(R.id.openWeatherHumidityView);
        openWeatherWindSpeedView = findViewById(R.id.openWeatherWindSpeedView);
        openWeatherPressureView = findViewById(R.id.openWeatherPressureView);
        temperatureView = findViewById(R.id.temperatureView);
        humidityView = findViewById(R.id.humidityView);
        precipitationView = findViewById(R.id.precipitationView);
        windSpeedView = findViewById(R.id.windSpeedView);
        pressureView = findViewById(R.id.pressureView);
        feelsLikeView = findViewById(R.id.feelsLikeView);
        cloudinessView = findViewById(R.id.cloudinessView);
        sunriseView = findViewById(R.id.sunriseView);
        sunsetView = findViewById(R.id.sunsetView);
        forecastDateView = findViewById(R.id.forecastDateView);
        forecastTemperatureView = findViewById(R.id.forecastTemperatureView);
        forecastHumidityView = findViewById(R.id.forecastHumidityView);
        forecastWindSpeedView = findViewById(R.id.forecastWindSpeedView);
        weatherIcon = findViewById(R.id.weatherIcon);
        progressBar = findViewById(R.id.progressBar);
        cityInput = findViewById(R.id.textInputEditText);
        loadWeatherButton = findViewById(R.id.loadWeatherButton);
        temperatureRadioGroup = findViewById(R.id.radioGroupTemperature);

        // Инициализация адаптера для отображения прогноза
        forecastAdapter = new ForecastAdapter(forecastList);

        RecyclerView forecastRecyclerView = findViewById(R.id.recyclerViewForecast);
        forecastRecyclerView.setAdapter(forecastAdapter);
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));}

    private void fetchWeatherData(String city) {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                String openWeatherData = fetchDataFromApi(OPENWEATHERMAP_API_BASE + city + "&appid=697ea4656bbef87c8247169d52dfef54&units=metric");
                String weatherApiData = fetchDataFromApi(WEATHERAPI_API_BASE + city);
                fetchForecastData(city);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    updateUI(openWeatherData, weatherApiData);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Ошибка получения данных. Пожалуйста, попробуйте снова.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void fetchForecastData(String city) {
        new Thread(() -> {
            try {
                String forecastData = fetchDataFromApi(FORECAST_API_BASE + city + "&appid=697ea4656bbef87c8247169d52dfef54&units=metric");
                runOnUiThread(() -> updateForecastUI(forecastData));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Ошибка получения данных прогноза.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String fetchDataFromApi(String apiUrl) throws Exception {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("HTTP Error: " + responseCode + " " + connection.getResponseMessage());
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            return result.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void updateUI(String openWeatherData, String weatherApiData) {
        try {
            Log.d("WeatherData", "OpenWeather Data: " + openWeatherData);

            JSONObject openWeatherJson = new JSONObject(openWeatherData);
            JSONObject weatherApiJson = new JSONObject(weatherApiData);

            int timezoneOffset = openWeatherJson.getInt("timezone");

            double temp1 = openWeatherJson.getJSONObject("main").optDouble("temp", Double.NaN);
            double temp2 = weatherApiJson.getJSONObject("current").optDouble("temp_c", Double.NaN);
            double avgTemp = (temp1 + temp2) / 2;

            if (temperatureRadioGroup.getCheckedRadioButtonId() == R.id.radioFahrenheit) {
                avgTemp = avgTemp * 9 / 5 + 32;
                temp1 = temp1 * 9 / 5 + 32;
                temp2 = temp2 * 9 / 5 + 32;
                temperatureView.setText(String.format("Температура: %.1f °F", avgTemp));
                openWeatherTemperatureView.setText(String.format("Температура: %.1f °F", temp1));
                weatherApiTemperatureView.setText(String.format("Температура: %.1f °F", temp2));
            } else {
                temperatureView.setText(String.format("Температура: %.1f °C", avgTemp));
                openWeatherTemperatureView.setText(String.format("Температура: %.1f °C", temp1));
                weatherApiTemperatureView.setText(String.format("Температура: %.1f °C", temp2));
            }

            double humidity1 = openWeatherJson.getJSONObject("main").optDouble("humidity", 0);
            double humidity2 = weatherApiJson.getJSONObject("current").optDouble("humidity", 0);
            double avgHumidity = (humidity1 + humidity2) / 2;
            humidityView.setText(String.format("Влажность: %.1f%%", avgHumidity));
            openWeatherHumidityView.setText(String.format("Влажность: %.1f%%", humidity1));
            weatherApiHumidityView.setText(String.format("Влажность: %.1f%%", humidity2));

            double precip1 = openWeatherJson.has("rain") ? openWeatherJson.getJSONObject("rain").optDouble("1h", 0) : 0;
            double precip2 = weatherApiJson.getJSONObject("current").optDouble("precip_mm", 0);
            double avgPrecip = (precip1 + precip2) / 2;
            precipitationView.setText(String.format("Осадки: %.1f мм", avgPrecip));

            // Оценка вероятности осадков
            double rainProbability1 = openWeatherJson.has("rain") ? openWeatherJson.getJSONObject("rain").optDouble("1h", 0) : 0;
            double rainProbability2 = weatherApiJson.getJSONObject("current").optDouble("precip_mm", 0);
            double avgRainProbability = (rainProbability1 + rainProbability2) / 2;
            precipitationView.setText(String.format("Осадки: %.1f мм (Вероятность: %.1f%%)", avgPrecip, avgRainProbability * 100));

            double windSpeed1 = openWeatherJson.getJSONObject("wind").optDouble("speed", 0);
            double windSpeed2 = weatherApiJson.getJSONObject("current").optDouble("wind_kph", 0);
            windSpeed2 = windSpeed2 / 3.6;
            double avgWindSpeed = (windSpeed1 + windSpeed2) / 2;
            windSpeedView.setText(String.format("Скорость ветра: %.1f м/с", avgWindSpeed));
            openWeatherWindSpeedView.setText(String.format("Скорость ветра: %.1f м/с", windSpeed1));
            weatherApiWindSpeedView.setText(String.format("Скорость ветра: %.1f м/с", windSpeed2));

            double pressure1 = openWeatherJson.getJSONObject("main").optDouble("pressure", 0);
            double pressure2 = weatherApiJson.getJSONObject("current").optDouble("pressure_mb", 0);
            double avgPressure = (pressure1 + pressure2) / 2;
            pressureView.setText(String.format("Давление: %.1f гПа", avgPressure));
            openWeatherPressureView.setText(String.format("Давление: %.1f гПа", pressure1));
            weatherApiPressureView.setText(String.format("Давление: %.1f гПа", pressure2));

            double feelsLike1 = openWeatherJson.getJSONObject("main").optDouble("feels_like", 0);
            double feelsLike2 = weatherApiJson.getJSONObject("current").optDouble("feelslike_c", 0);
            double avgFeelsLike = (feelsLike1 + feelsLike2) / 2;
            feelsLikeView.setText(String.format("Ощущается как: %.1f °C", avgFeelsLike));

            cloudinessView.setText(String.format("Облачность: %.1f%%", openWeatherJson.getJSONObject("clouds").optDouble("all", 0)));

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            sunriseView.setText("Восход: " + sdf.format(openWeatherJson.getJSONObject("sys").getLong("sunrise") * 1000));
            sunsetView.setText("Закат: " + sdf.format(openWeatherJson.getJSONObject("sys").getLong("sunset") * 1000));

            String iconCode = openWeatherJson.getJSONArray("weather").getJSONObject(0).getString("icon");
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            Glide.with(this).load(iconUrl).into(weatherIcon);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void updateForecastUI(String forecastData) {
        try {
            JSONObject forecastJson = new JSONObject(forecastData);

            // Очищаем старые данные перед добавлением новых
            forecastList.clear();

            for (int i = 0; i < forecastJson.getJSONArray("list").length(); i++) {
                JSONObject forecast = forecastJson.getJSONArray("list").getJSONObject(i);
                String date = forecast.getString("dt_txt");
                double temperature = forecast.getJSONObject("main").getDouble("temp");
                double humidity = forecast.getJSONObject("main").getDouble("humidity");
                double windSpeed = forecast.getJSONObject("wind").getDouble("speed");

                forecastList.add(new Forecast(date, temperature, humidity, windSpeed));
            }

            // Уведомляем адаптер, что данные обновлены
            forecastAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private static class Forecast {
        String date;
        double temperature;
        double humidity;
        double windSpeed;

        public Forecast(String date, double temperature, double humidity, double windSpeed) {
            this.date = date;
            this.temperature = temperature;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
        }
    }

    private static class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {
        private List<Forecast> forecastList;

        public ForecastAdapter(List<Forecast> forecastList) {
            this.forecastList = forecastList;
        }

        @Override
        public ForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast, parent, false);
            return new ForecastViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ForecastViewHolder holder, int position) {
            Forecast forecast = forecastList.get(position);
            holder.dateTextView.setText(forecast.date);
            holder.temperatureTextView.setText(String.format("Температура: %.1f °C", forecast.temperature));
            holder.humidityTextView.setText(String.format("Влажность: %.1f%%", forecast.humidity));
            holder.windSpeedTextView.setText(String.format("Скорость ветра: %.1f м/с", forecast.windSpeed));
        }

        @Override
        public int getItemCount() {
            return forecastList.size();
        }

        public static class ForecastViewHolder extends RecyclerView.ViewHolder {
            TextView dateTextView, temperatureTextView, humidityTextView, windSpeedTextView;

            public ForecastViewHolder(View itemView) {
                super(itemView);
                dateTextView = itemView.findViewById(R.id.forecastDateView);
                temperatureTextView = itemView.findViewById(R.id.forecastTemperatureView);
                humidityTextView = itemView.findViewById(R.id.forecastHumidityView);
                windSpeedTextView = itemView.findViewById(R.id.forecastWindSpeedView);
            }
        }
    }
}
