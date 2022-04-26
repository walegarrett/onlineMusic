package com.example.onlineMusic_2.net

import com.example.onlineMusic_2.interfaces.MusicService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object MusicNetwork {
    private val musicService = ServiceCreator.create(MusicService::class.java)

    suspend fun getSearchMusicsBean(keywords:String, type:Int, limit:Int) = musicService.getSearchMusicsBean(keywords = keywords,type = type, limit = limit).await()

    private suspend fun <T> Call<T>.await():T{
        return suspendCoroutine { continuation ->
            enqueue(object: Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
                //请求成功
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if(body != null)
                        continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("response body is null"))
                }

            })
        }
    }
}