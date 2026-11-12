package com.example.konekin.network

import com.example.konekin.model.CrudResponse
import com.example.konekin.model.UserDetailResponse
import com.example.konekin.model.UserRequest
import com.example.konekin.model.Users
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("employees")
    fun getAllUsers(): Call<Users>

    @GET("employee/{id}")
    fun getUserById(@Path("id") id: Int): Call<UserDetailResponse>

    @POST("create")
    fun createUser(@Body request: UserRequest): Call<CrudResponse>

    @PUT("update/{id}")
    fun updateUser(
        @Path("id") id: Int,
        @Body request: UserRequest
    ): Call<CrudResponse>

    @DELETE("delete/{id}")
    fun deleteUser(@Path("id") id: Int): Call<CrudResponse>
}