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
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.activity.SearchActivity
import com.example.onlineMusic_2.bean.*
import com.example.onlineMusic_2.interfaces.MusicService
import com.example.onlineMusic_2.receiver.FragmentBroadCastReceiver
import com.example.onlineMusic_2.receiver.MediaBroadCastReceiver
import com.example.onlineMusic_2.utils.OnlinePlaying
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.fragment_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MusicDetailListAdapter(val context: Context, val detailMusicList:List<Music>):RecyclerView.Adapter<MusicDetailListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val musicImage: ImageView =view.findViewById(R.id.musicDetailImage)
        val musicName: TextView =view.findViewById(R.id.musicDetailName)
        val musicPlayer: TextView =view.findViewById(R.id.playerNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //注意这里需要设置子项的布局文件，否则报错
        val view= LayoutInflater.from(parent.context).inflate(R.layout.music_list_details,parent,false)
        val viewHolder=ViewHolder(view)
        //注册子项的点击事件
        viewHolder.itemView.setOnClickListener {
            //设置当前播放的音乐
            val music=detailMusicList[viewHolder.adapterPosition]
            OnlinePlaying.changeMusic(viewHolder.adapterPosition)

            //发送打开item的广播
            val intent = Intent(FragmentBroadCastReceiver.ACTION_FRAGMENT_PLAY_DETAILSONG)
            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            context.sendBroadcast(intent)

        }

        return viewHolder
    }

    override fun getItemCount(): Int=detailMusicList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val music: Music = detailMusicList[position]
        holder.musicName.text=music.filmName.substringBefore('-')
        holder.musicImage.setImageResource(music.filmImageId)
        holder.musicPlayer.text=music.filmName.substringAfter('-')
    }
}