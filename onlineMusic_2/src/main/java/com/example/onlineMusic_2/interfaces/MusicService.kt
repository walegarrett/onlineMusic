package com.example.onlineMusic_2.interfaces

import com.example.onlineMusic_2.bean.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * 网络请求接口，分功能发起网络数据请求
 */
interface MusicService {

    /**
     * 根据关键字搜索：
     * 必须：keywords : 关键词
     * limit : 返回数量 , 默认为 30
     * offset : 偏移数量，用于分页 , 如 : 如 :( 页数 -1)*30, 其中 30 为 limit 的值 , 默认为 0
     * type: 搜索类型；默认为 1 即单曲 , 取值意义 : 1: 单曲, 10: 专辑, 100: 歌手, 1000: 歌单, 1002: 用户, 1004: MV, 1006: 歌词, 1009: 电台, 1014: 视频, 1018:综合
     */
    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36")
    @GET("/search")// /search 或者 /cloudsearch(更全)
    fun getSearchMusicsBean(@Query("keywords") keywords:String, @Query("type") type:Int = 1, @Query("limit") limit:Int = 30): Call<SongInfo>

    /**
     * 根据id搜索歌曲url
     */
    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36")
    @GET("/song/url")// /search 或者 /cloudsearch(更全)
    fun getMusic(@Query("id") id:Int): Call<MusicUrl>

    /**
     * 根据歌曲id获取歌词
     */
    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36")
    @GET("/lyric")
    fun getMusicLyric(@Query("id") id:Int): Call<Lyric>

    /**
     * 获取所有榜单
     */
    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36")
    @GET("/toplist")
    fun getTopList(): Call<TopListBean>

    /**
     * 获取某个榜单:传入榜单的id
     */
    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36")
    @GET("/top/list")
    fun getOneTopList(@Query("id") id: String): Call<TopListItemBean>

    /**
     * 获取歌曲详情
     */
    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36")
    @GET("/song/detail")
    fun getSongsDetail(@Query("ids") ids:String): Call<SongInfo>
}
