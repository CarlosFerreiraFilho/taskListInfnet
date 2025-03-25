package br.edu.infnet.atv.one.tasklist.data.models

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Float,
    val humidity: Int
)

data class Weather(
    val description: String
)
