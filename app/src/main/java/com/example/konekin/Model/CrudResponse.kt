package com.example.konekin.model

import com.google.gson.annotations.SerializedName

data class CrudResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null
)