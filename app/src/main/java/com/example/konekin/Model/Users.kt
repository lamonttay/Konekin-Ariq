package com.example.konekin.model

import com.google.gson.annotations.SerializedName

data class Users(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("data")
    val data: List<Data>
)