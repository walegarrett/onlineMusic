package com.example.onlineMusic_2.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri.parse
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.bean.Music
import com.example.onlineMusic_2.bean.TopListBean
import com.example.onlineMusic_2.receiver.FragmentBroadCastReceiver
import com.example.onlineMusic_2.utils.OnlinePlaying
import okhttp3.HttpUrl.parse
import java.net.URL

class TopListAdapter(val context: Context, val detailMusicList:List<TopListBean.ListBean>): RecyclerView.Adapter<TopListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val item_icon: ImageView =view.findViewById(R.id.item_icon)
        val rankTitle: TextView =view.findViewById(R.id.rankTitle)
        val songName1: TextView =view.findViewById(R.id.songName1)
        val songName2: TextView =view.findViewById(R.id.songName2)
        val songName3: TextView =view.findViewById(R.id.songName3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //注意这里需要设置子项的布局文件，否则报错
        val view= LayoutInflater.from(parent.context).inflate(R.layout.layout_toplist_item, parent,false)
        val viewHolder=ViewHolder(view)

        //注册子项的点击事件
        viewHolder.itemView.setOnClickListener {
            Log.d("ToplistAdapter", "点击排行榜item")
            //发送打开item的广播
            val intent = Intent(FragmentBroadCastReceiver.ACTION_FRAGMENT_OPEN_RANKSONG)
            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            val netMusicBean=detailMusicList[viewHolder.adapterPosition]
            Log.d("ToplistAdapter", netMusicBean.toString())
            //传递榜单的id
            intent.putExtra("rankId",netMusicBean.id)
            context.sendBroadcast(intent)
        }
        return viewHolder
    }

    override fun getItemCount(): Int= detailMusicList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val topListItemInfo: TopListBean.ListBean = detailMusicList[position]
        context.let { Glide.with(it).load(topListItemInfo.coverImgUrl).into(holder.item_icon) }
        holder.rankTitle.text = topListItemInfo.name

        val top3list = topListItemInfo.top3MusicList

        if (top3list != null && top3list.size >= 3) {

            holder.songName1.text = top3list[0].name
            holder.songName2.text = top3list[1].name
            holder.songName3.text = top3list[2].name
        }else{
            holder.songName1.text = ""
            holder.songName2.text = ""
            holder.songName3.text = ""
        }
    }
}