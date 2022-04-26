package com.example.onlineMusic_2.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.bean.MusicUrl
import com.example.onlineMusic_2.bean.SongsBean
import com.example.onlineMusic_2.db.MusicInfoDB
import com.example.onlineMusic_2.interfaces.MusicService
import com.example.onlineMusic_2.net.HttpUtil
import com.example.onlineMusic_2.receiver.FragmentBroadCastReceiver
import com.example.onlineMusic_2.utils.OnlinePlaying
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopListItemAdapter(val context: Context, val detailMusicList:List<SongsBean>): RecyclerView.Adapter<TopListItemAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val musicImage: ImageView =view.findViewById(R.id.musicImage)
        var musicName: TextView =view.findViewById(R.id.musicName)
        val playerName: TextView =view.findViewById(R.id.playerName)
        val like: ImageView = view.findViewById(R.id.likeImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //注意这里需要设置子项的布局文件，否则报错
        val view= LayoutInflater.from(parent.context).inflate(R.layout.musics, parent,false)
        val viewHolder=ViewHolder(view)

        //注册子项的点击事件
        viewHolder.musicImage.setOnClickListener {
            //当前播放的列表为在线列表
            OnlinePlaying.onlinemusiclist = detailMusicList as ArrayList<SongsBean>

            val songBean=detailMusicList[viewHolder.adapterPosition]
            val position=viewHolder.adapterPosition
            val songId:Int = songBean.id
            val retrofit = HttpUtil.createRetrofit()
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
            val songBean=detailMusicList[position]

            //发送点击我喜欢广播
            val intent = Intent(FragmentBroadCastReceiver.ACTION_FRAGMENT_LIKE_CLICKED)
            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            intent.putExtra("position", position)
            context.sendBroadcast(intent)

        }
        return viewHolder
    }

    override fun getItemCount(): Int= detailMusicList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val song: SongsBean = detailMusicList[position]
        context.let { Glide.with(it).load(song.musicUrl).into(holder.musicImage) }
        holder.musicName.text = song.name
        holder.playerName.text = song.playerName

        //判断歌曲的类型
        val m_type = song.musicType
        if(m_type == 3){
            //喜欢
            holder.like.setImageResource(R.mipmap.heart)
        }else{
            //不喜欢
            holder.like.setImageResource(R.drawable.love)
        }
    }
}