package com.example.myapplication.models

import java.io.Serializable

data class WheaterResponse(
    val coord: Coord,
    val wheater: List<Wheater>,
    val base: String,
    val main: Main,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Int,
    val sys: Sys,
    val id: Int,
    val name: String,
    val cod: Int
): Serializable