package com.example.myapplication.models

import java.io.Serializable

data class Wheater (
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
) : Serializable