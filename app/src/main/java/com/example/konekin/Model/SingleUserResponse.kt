package com.example.konekin.Model

import com.example.konekin.model.Data
import com.google.gson.annotations.SerializedName

data class SingleUserResponse(
    @SerializedName("data")
    val data: Data
)
