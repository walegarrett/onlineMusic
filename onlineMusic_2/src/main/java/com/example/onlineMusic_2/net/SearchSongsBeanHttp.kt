package com.example.onlineMusic_2.net

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineMusic_2.adapter.OnlineMusicAdapter
import com.example.onlineMusic_2.bean.SongInfo
import com.example.onlineMusic_2.bean.SongsBean
import com.example.onlineMusic_2.interfaces.MusicService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.HashMap

object SearchSongsBeanHttp {

    fun getSearchSongsResult(context: Context, keywords: String, type:Int, limit:Int): HttpResult{
        val retrofit = HttpUtil.createRetrofit()
        val musicService = retrofit.create(MusicService::class.java)

        val httpResult:HttpResult = HttpResult()
        //返回结果
        val returnresult: MutableMap<String, Any?> = HashMap<String, Any?>()

        //获取搜索结果
        musicService.getSearchMusicsBean(keywords = keywords,type = 1, limit = 30)
            .enqueue(object: Callback<SongInfo> {
                @SuppressLint("LongLogTag")
                override fun onResponse(call: Call<SongInfo>, response: Response<SongInfo>) {
                    val songInfo = response.body()
                    if(songInfo != null){
                        val songList: List<SongsBean>? = songInfo.result?.songs
                        httpResult.status = 0
                        returnresult["songlist"] = songList
                        Log.d("returnresult['songlist']", songList.toString())
                    }else{
                        httpResult.status = -1
                        httpResult.errorMsg = "歌曲数据请求失败"
                        returnresult["songlist"] = null
                    }
                }
                override fun onFailure(call: Call<SongInfo>, t: Throwable) {
                    httpResult.status = -1
                    httpResult.errorMsg = "歌曲数据请求失败"
                    returnresult["songlist"] = null
                    t.printStackTrace()
                }
            })
        httpResult.result = returnresult
        return httpResult
    }
}