package dev.andyw.ad340

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.andyw.ad340.api.CurrentWeather
import dev.andyw.ad340.api.WeeklyForecast
import dev.andyw.ad340.api.createOpenWeatherMapService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*

import kotlin.random.Random

// loads data and provides data out to activity
class ForecastRepository {

    private val weatherService = createOpenWeatherMapService()
    private val _currentWeather = MutableLiveData<CurrentWeather>()
    val currentWeather: LiveData<CurrentWeather> = _currentWeather

    private val _weeklyForecast = MutableLiveData<WeeklyForecast>()
    val weeklyForecast: LiveData<WeeklyForecast> = _weeklyForecast

    fun loadWeeklyForecast(zipcode: String){

        val call = createOpenWeatherMapService().currentWeather(zipcode, "imperial", BuildConfig.OPEN_WEATHER_MAP_API_KEY)
        call.enqueue(object : Callback<CurrentWeather>{
            override fun onFailure(call: Call<CurrentWeather>, t: Throwable) {
                Log.e(ForecastRepository::class.java.simpleName, "error loading location for weekly forecast", t)
            }

            override fun onResponse(call: Call<CurrentWeather>, response: Response<CurrentWeather>) {
                val weatherResponse = response.body()
                if (weatherResponse != null) {
                    // load 7 day forecast
                    val forecastCall = createOpenWeatherMapService().sevenDayForecast(
                        lat = weatherResponse.coord.lat,
                        lon = weatherResponse.coord.lon,
                        exclude = "current, minutely, hourly",
                        units = "imperial",
                        apiKey = BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    )
                    forecastCall.enqueue(object: Callback<WeeklyForecast>{
                        override fun onFailure(call: Call<WeeklyForecast>, t: Throwable) {
                            Log.e(ForecastRepository::class.java.simpleName, "error loading weekly forecast")
                        }

                        override fun onResponse(call: Call<WeeklyForecast>, response: Response<WeeklyForecast>) {
                            val weeklyForecastResponse = response.body()
                            if (weeklyForecastResponse != null) {
                                _weeklyForecast.value = weeklyForecastResponse
                            }
                        }
                    })
                }
            }

        })
    }

    fun loadCurrentForecast(zipcode: String) {
        val call = createOpenWeatherMapService().currentWeather(zipcode, "imperial", BuildConfig.OPEN_WEATHER_MAP_API_KEY)
        call.enqueue(object : Callback<CurrentWeather>{
            override fun onFailure(call: Call<CurrentWeather>, t: Throwable) {
                Log.e(ForecastRepository::class.java.simpleName, "error loading current weather", t)
            }

            override fun onResponse(call: Call<CurrentWeather>, response: Response<CurrentWeather>) {
                val weatherResponse = response.body()
                if (weatherResponse != null) {
                    _currentWeather.value = weatherResponse
                }
            }

        })
    }

    private fun getTempDescription(temp: Float) : String {
        return when (temp){
            in Float.MIN_VALUE.rangeTo(0f) -> "Anything below 0 doesn't make sense"
            in 0f.rangeTo(32f) -> "Way too cold"
            in 55f.rangeTo(65f) -> "Getting better"
            in 65f.rangeTo(80f) -> "That's the sweet spot!"
            in 80f.rangeTo(90f) -> "Getting a little warm"
            in 90f.rangeTo(100f) -> "Where's the A/C?"
            in 100f.rangeTo(Float.MAX_VALUE) -> "What is this, Florida?"
            else -> "Does not compute"
        }
    }
}