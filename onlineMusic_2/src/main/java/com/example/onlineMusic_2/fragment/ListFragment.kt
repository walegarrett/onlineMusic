package com.example.onlineMusic_2.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineMusic_2.R

import com.example.onlineMusic_2.bean.Music
import com.example.onlineMusic_2.utils.OnlinePlaying

import kotlinx.android.synthetic.main.fragment_list.*


/**
 * A simple [Fragment] subclass.
 * Use the [ListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListFragment : BaseFragment() {
    private var adapter: MusicAdapter? =null
    private val musiclist=OnlinePlaying.musiclist
    val updatingClickReceiver=UpdatingClickReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ListFragment","onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ListFragment","onCreateView")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d("ListFragment","onActivityCreated")
        //设置音乐的recycleView
        val layoutManager= LinearLayoutManager(context)
        musicRecyclerView2.layoutManager=layoutManager
        //设置音乐列表的适配器
        adapter= MusicAdapter(musiclist)
        musicRecyclerView2.adapter=adapter

        //设置悬浮按钮的点击事件
        fab2.setOnClickListener {
            musicRecyclerView2.smoothScrollToPosition(0)
        }

        //监听开始播放等按钮
        musicPlayImageView.setOnClickListener {
            if(OnlinePlaying.playBinder.getPlayingStatus()){
                musicPlayImageView.setImageResource(R.mipmap.play)
            }else{
                musicPlayImageView.setImageResource(R.mipmap.pause)
            }
            OnlinePlaying.startPlayMusic()
        }

        //监听点击下一首
        musicNextImageView.setOnClickListener {
            OnlinePlaying.playNextMusic()
            musicPlayImageView.setImageResource(R.mipmap.pause)
        }

        //监听点击上一首
        musicLastImageView.setOnClickListener {
            OnlinePlaying.playPreMusic()
            musicPlayImageView.setImageResource(R.mipmap.pause)
        }
        //点击音乐图像
        musicImageView.setOnClickListener{
            //打开音乐播放界面
            val manager: FragmentManager ?= activity?.getSupportFragmentManager()
            val fragmentTransaction = manager?.beginTransaction()
            fragmentTransaction?.replace(R.id.replace_layout, DetailFragment())
            fragmentTransaction?.commit()
        }
    }

    inner class MusicAdapter(val filmList:List<Music>): RecyclerView.Adapter<MusicAdapter.ViewHolder>() {
        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
            val musicImage: ImageView =view.findViewById(R.id.musicImage)
            val musicName: TextView =view.findViewById(R.id.musicName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view= LayoutInflater.from(parent.context).inflate(R.layout.musics,parent,false)
            val viewHolder=ViewHolder(view)
            //注册子项的点击事件
            viewHolder.itemView.setOnClickListener {
                val music=filmList[viewHolder.adapterPosition]
                OnlinePlaying.changeMusic(viewHolder.adapterPosition)
            }

            return viewHolder
        }

        override fun getItemCount(): Int=filmList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int){
            val film: Music = filmList[position]
            holder.musicName.text=film.filmName
            holder.musicImage.setImageResource(film.filmImageId)
        }
    }



    //接收广播: 当前音乐是否播放
    inner class UpdatingClickReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
//            Log.i("TAG", "userClick:我被点击啦！！！ ")
            val action: String? =intent.action
            if(action.equals("isPlaying")){
                Log.i("TAG", "当前音乐被播放了 ")
                musicTitle.text= OnlinePlaying.currentMusic.substringBefore('-')//设置标题
                musicPerson.text= OnlinePlaying.currentMusic.substringAfter('-')
                musicPlayImageView.setImageResource(R.mipmap.pause)
                //设置音乐图片
                musicImageView.setImageResource(OnlinePlaying.music.filmImageId)
            }else if(action.equals("isPaused")){
                Log.i("TAG", "没有音乐在播放")
                musicPlayImageView.setImageResource(R.mipmap.play)
                //设置音乐图片
                musicImageView.setImageResource(OnlinePlaying.music.filmImageId)
                musicTitle.text= OnlinePlaying.currentMusic.substringBefore('-')  //设置标题
                musicPerson.text= OnlinePlaying.currentMusic.substringAfter('-')
            }else if(action.equals("finished")){
//                OnlinePlaying.playNextMusic()
            }
        }
    }
    fun registeUpdatingBroadCast(){
        val intentFilter= IntentFilter()
        intentFilter.addAction("isPlaying")
        intentFilter.addAction("isPaused")
        intentFilter.addAction("finished")
        activity?.registerReceiver(updatingClickReceiver,intentFilter)
    }
    override fun onResume() {
        super.onResume()
        //注册监听事件
        registeUpdatingBroadCast()
        //判断当前是否在播放音乐
        if(OnlinePlaying.playBinder.getPlayingStatus()){
            val intent= Intent("isPlaying")
            activity?.sendBroadcast(intent)
        }else{
            val intent= Intent("isPaused")
            activity?.sendBroadcast(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        //取消注册广播
        activity?.unregisterReceiver(updatingClickReceiver)
    }
}