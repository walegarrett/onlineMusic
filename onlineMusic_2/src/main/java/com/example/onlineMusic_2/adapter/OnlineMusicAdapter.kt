package com.example.onlineMusic_2.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.activity.SearchActivity
import com.example.onlineMusic_2.bean.ArtistsBean
import com.example.onlineMusic_2.bean.MusicUrl
import com.example.onlineMusic_2.bean.SongInfo
import com.example.onlineMusic_2.bean.SongsBean
import com.example.onlineMusic_2.interfaces.MusicService
import com.example.onlineMusic_2.receiver.FragmentBroadCastReceiver
import com.example.onlineMusic_2.utils.OnlinePlaying
import kotlinx.android.synthetic.main.activity_search.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class OnlineMusicAdapter(val onlineMusicList:List<SongsBean>):RecyclerView.Adapter<OnlineMusicAdapter.ViewHolder>() {
    var context:Context?= null
    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val musicImage: ImageView =view.findViewById(R.id.musicImage)
        val musicName: TextView =view.findViewById(R.id.musicName)
        val playerName: TextView =view.findViewById(R.id.playerName)
        val like: ImageView = view.findViewById(R.id.likeImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.musics,parent,false)
        val viewHolder=ViewHolder(view)
        //注册子项的点击事件
        viewHolder.musicImage.setOnClickListener {
            //当前播放的列表为在线列表
            OnlinePlaying.onlinemusiclist = onlineMusicList as ArrayList<SongsBean>

            val songBean=onlineMusicList[viewHolder.adapterPosition]
            val position=viewHolder.adapterPosition
            val songId:Int = songBean.id
            val baseUrl1 = "http://123.57.176.198:3000"//http://10.101.58.109/    http://123.57.176.198:3000/     http://10.101.11.130:3000
            val retrofit = createRetrofit(baseUrl1)
            val musicService = retrofit.create(MusicService::class.java)
            musicService.getMusic(id = songId)
                .enqueue(object: Callback<MusicUrl> {
                    override fun onResponse(call: Call<MusicUrl>, response: Response<MusicUrl>) {
                        val musicurl = response.body()
                        if(musicurl != null){
                            val urllist: List<MusicUrl.DataBean>? = musicurl.data
                            if(urllist!=null&&urllist.size>0)
                                urllist[0].url?.let { it1 -> OnlinePlaying.changeMusic(pos = position, musicId = songId, musicUrl = it1, type = 1) }
                        }
                    }
                    override fun onFailure(call: Call<MusicUrl>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
        }
        //点击我喜欢按钮
        viewHolder.like.setOnClickListener {
            Log.d("TopListItemAdapter", "点击我喜欢！收藏歌曲！")

            val position=viewHolder.adapterPosition
            val songBean=onlineMusicList[position]

            //发送点击我喜欢广播
            val intent = Intent(FragmentBroadCastReceiver.ACTION_FRAGMENT_LIKE_CLICKED)
            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            intent.putExtra("position", position)
            context?.sendBroadcast(intent)

        }
        context = parent.context
        return viewHolder
    }

    override fun getItemCount(): Int=onlineMusicList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val songBean: SongsBean = onlineMusicList[position]
        holder.musicName.text=songBean.name
        val picUrl = songBean.al?.picUrl

        //得到可用的图片
        if (picUrl != null) {
            //得到可用的图片
//            artistsBean.img1v1Url?.let { Log.d("img1v1Url", it) }
            //加载网络图片
//            holder.musicImage.setImageBitmap(bitmap)
            context?.let { Glide.with(it).load(picUrl).into(holder.musicImage) }
        }else{
            holder.musicImage.setImageResource(R.mipmap.pic1)
        }
        holder.playerName.text = songBean.playerName

        //判断歌曲的类型
        val m_type = songBean.musicType
        if(m_type == 3){
            //喜欢
            holder.like.setImageResource(R.mipmap.heart)
        }else{
            //不喜欢
            holder.like.setImageResource(R.drawable.love)
        }
    }
    /**
     * 根据指定的基IP地址获取retrofit
     */
    fun createRetrofit(baseURL: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseURL)//如果使用雷电模拟器，不能使用10.0.2.2地址，需要使用实际的ip地址
            .addConverterFactory(GsonConverterFactory.create())//当需要使用gson解析时调用该方法
            .build()
    }
}