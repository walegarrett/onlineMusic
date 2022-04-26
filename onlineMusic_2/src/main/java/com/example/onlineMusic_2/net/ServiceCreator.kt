package com.example.onlineMusic_2.net

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceCreator {
    private const val BASE_URL = ""
    private val retrofit = Retrofit.Builder()
        .baseUrl(HttpUtil.baseUrl1)//如果使用雷电模拟器，不能使用10.0.2.2地址，需要使用实际的ip地址
        .addConverterFactory(GsonConverterFactory.create())//当需要使用gson解析时调用该方法
        .build()

    //创建Servicee
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    inline fun <reified T> create(): T = create(T::class.java)
}