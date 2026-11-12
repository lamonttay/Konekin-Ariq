package com.example.konekin.model

import com.google.gson.annotations.SerializedName

data class UserDetailResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("data")
    val data: Data? = null
)