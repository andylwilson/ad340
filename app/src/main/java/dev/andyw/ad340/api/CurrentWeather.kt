package dev.andyw.ad340.api

import com.squareup.moshi.Json

data class Forecast(val temp: Float)
data class Coordinates(val lat: Float, val lon: Float)

data class CurrentWeather(
    val name: String,
    val coord: Coordinates,
    @field:Json(name = "main") val forecast: Forecast
)